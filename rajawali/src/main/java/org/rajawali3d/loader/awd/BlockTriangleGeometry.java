package org.rajawali3d.loader.awd;

import java.util.ArrayList;

import android.util.SparseArray;

import org.apache.http.ParseException;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationChildObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationChildObject3D.BoneVertex;
import org.rajawali3d.animation.mesh.SkeletalAnimationChildObject3D.BoneWeight;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.util.RajLog;

/**
 * The TriangleGeometry block describes a single mesh of an AWD file. Multiple TriangleGeometry blocks may exists in a
 * single AWD file as part of a single model, multiple models, or scene.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockTriangleGeometry extends ABaseObjectBlockParser {

	protected Object3D finalObject = null;
	protected Object3D[] mBaseObjects;
	protected String mLookupName;
	protected int mSubGeometryCount;

	@Override
	public Object3D getBaseObject3D() {

		if(finalObject != null)
			return finalObject;

		if(mBaseObjects[0] instanceof SkeletalAnimationChildObject3D)
		{
			SkeletalAnimationObject3D container = new SkeletalAnimationObject3D();

			for(int i = 0; i < mBaseObjects.length; i++)
			{
				SkeletalAnimationChildObject3D child =
					(SkeletalAnimationChildObject3D)mBaseObjects[i];

				child.setSkeleton(container);

				container.addChild(child);
			}

			finalObject = container;
		}
		else if (mBaseObjects.length == 1)
			finalObject = mBaseObjects[0];
		else
		{
			final Object3D container = new Object3D(mLookupName);
			container.isContainer(true);
			for (int i = 0; i < mBaseObjects.length; i++)
				container.addChild(mBaseObjects[i]);

			finalObject = container;
		}

		return finalObject;
	}

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// Count of sub geometries
		mSubGeometryCount = dis.readUnsignedShort();

		// TODO Meshes need to be joined in some fashion. This might work. Need to test it I suppose.
		// One object for each sub geometry
		mBaseObjects = new Object3D[mSubGeometryCount];

		// Debug
        if (RajLog.isDebugEnabled()) {
            RajLog.d("  Lookup Name: " + mLookupName);
            RajLog.d("  Sub Geometry Count: " + mSubGeometryCount);
        }

		// Determine the precision for the block
		final boolean geoAccuracy = (blockHeader.flags & BlockHeader.FLAG_ACCURACY_GEO) ==
				BlockHeader.FLAG_ACCURACY_GEO;
		final short geoNr = geoAccuracy ? AWDLittleEndianDataInputStream.TYPE_FLOAT64
				: AWDLittleEndianDataInputStream.TYPE_FLOAT32;

		// Read the properties
		SparseArray<Short> properties = new SparseArray<Short>();
		// Scale Texture U
		properties.put(1, geoNr);
		// Scale Texture V
		properties.put(2, geoNr);
		// TODO Apply texture scales, need example of this working.
		dis.readProperties(properties);

		// Calculate the sizes
		final int geoPrecisionSize = blockHeader.globalPrecisionGeo ? 8 : 4;

		// Read each sub mesh data
		for (int parsedSub = 0; parsedSub < mSubGeometryCount; ++parsedSub) {
			long subMeshEnd = dis.getPosition() + dis.readUnsignedInt();

			// Geometry
			float[] vertices = null;
			int[] indices = null;
			float[] uvs = null;
			float[] normals = null;

			int[] joints = null;
			float[] weights = null;

			// Skip reading of mesh properties for now (per AWD implementation)
			dis.readProperties();

			// Read each data type from the mesh
			while (dis.getPosition() < subMeshEnd) {
				int idx = 0;
				int type = dis.readUnsignedByte();
				int typeF = dis.readUnsignedByte();
				long subLength = dis.readUnsignedInt();
				long subEnd = dis.getPosition() + subLength;

                if (RajLog.isDebugEnabled())
                    RajLog.d("   Mesh Data: t:" + type + " tf:" + typeF + " l:" + subLength + " ls:" + dis.getPosition() + " le:" + subEnd);

				// Process the mesh data by type
				switch ((int) type) {
				case 1: // Vertex positions
					vertices = new float[(int) (subLength / geoPrecisionSize)];
					while (idx < vertices.length) {
						// X, Y, Z
						vertices[idx++] = (float) dis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
						vertices[idx++] = (float) dis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
						vertices[idx++] = (float) dis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
					}
					break;
				case 2: // Face indices
					indices = new int[(int) (subLength / 2)];
					while (idx < indices.length)
						indices[idx++] = dis.readUnsignedShort();
					break;
				case 3: // UV coordinates
					uvs = new float[(int) (subLength / geoPrecisionSize)];
					while (idx < uvs.length)
						uvs[idx++] = (float) dis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
					break;
				case 4: // Vertex normals
					normals = new float[(int) (subLength / geoPrecisionSize)];
					while (idx < normals.length) {
						normals[idx++] = (float) dis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
					}
					break;
				case 6: // Joint index
					joints = new int[(int) (subLength / 2)];
					while (idx < joints.length)
						joints[idx++] = dis.readUnsignedShort();
					break;
				case 7: // Joint weight
					weights = new float[(int) (subLength / geoPrecisionSize)];
					while (idx < weights.length)
						weights[idx++] = (float) dis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
					break;
				case 5: // Vertex tangents
				default:
					// Unknown mesh data, skipping
					dis.skip(subLength);
				}

				// Validate each mesh data ending. This is a sanity check against precision flags.
				if (dis.getPosition() != subEnd)
					throw new ParseException("Unexpected ending. Expected " + subEnd + ". Got " + dis.getPosition());
			}

			dis.readUserAttributes(null);

			// Verify the arrays
			if (vertices == null)
				vertices = new float[0];
			if (normals == null)
				normals = new float[0];
			if (uvs == null)
				uvs = new float[0];
			if (indices == null)
				indices = new int[0];

			// FIXME This should be combining sub geometry not creating objects
			if(joints != null && joints.length > 0)
			{
				/*
				 * Prepares skeletal animation object as far as possible; setting mesh
				 * and skeletal weight data. The object will not yet have an actual
				 * skeleton applied to it.
				 */
				SkeletalAnimationChildObject3D obj = new SkeletalAnimationChildObject3D();
				obj.setData(vertices, normals, uvs, null, indices, false);

				int numVertices = vertices.length/3;

				// AWD stipulates all vertices have same # bindings, possibly 0 weighted
				int weightsPerVertex = weights.length/numVertices;

				// true WPV may be out of range, so clamp
				int clampWeightsPerVertex = Math.min(weightsPerVertex,
					SkeletalAnimationChildObject3D.MAX_WEIGHTS_PER_VERTEX);

				// one BoneVertex per actual vertex, maps to N weights & joint indices
				BoneVertex[] bvertices = new BoneVertex[numVertices];
				ArrayList<BoneWeight> bweights = new ArrayList<BoneWeight>();

				int maxWeightsPerVertex = 0;
				int vertexWeightIndex = 0;

				for(int vert = 0; vert < numVertices; vert++)
				{
					BoneVertex bone = new BoneVertex();

					bvertices[vert] = bone;

					// we may ignore weights, so map to our custom list
					bone.weightIndex = bweights.size();

					// true position in raw weight array
					vertexWeightIndex = vert * weightsPerVertex;

					// only add first [clamp] non-zero weights
					for(int wgt = 0; wgt < clampWeightsPerVertex; wgt++)
					{
						if(weights[vertexWeightIndex + wgt] == 0)
							continue;

						BoneWeight weight = new BoneWeight();

						// joints and weights are indexed together
						weight.jointIndex = joints[vertexWeightIndex + wgt];
						weight.weightValue = weights[vertexWeightIndex + wgt];

						bone.numWeights++;

						bweights.add(weight);
					}

					maxWeightsPerVertex = Math.max(maxWeightsPerVertex, bone.numWeights);
				}

				// extract the clean BoneWeight array
				BoneWeight[] boneweights = bweights.toArray(new BoneWeight[bweights.size()]);

				obj.setMaxBoneWeightsPerVertex(maxWeightsPerVertex);
				obj.setSkeletonMeshData(bvertices, boneweights);
				//obj.setInverseZScale(true);

				mBaseObjects[parsedSub] = obj;
			}
			else
			{
				mBaseObjects[parsedSub] = new Object3D();
				mBaseObjects[parsedSub].setData(vertices, normals, uvs, null, indices, false);
			}
		}

		dis.readUserAttributes(null);
	}
}
