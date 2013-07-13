package rajawali.parser.awd;

import rajawali.BaseObject3D;
import rajawali.parser.AWDParser.AWDLittleEndianDataInputStream;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;

/**
 * The TriangleGeometry block describes a single mesh of an AWD file. Multiple TriangleGeometry blocks may exists in a
 * single AWD file as part of a single model, multiple models, or scene.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockTriangleGeometry extends AExportableBlockParser {

	protected BaseObject3D[] mBaseObjects;
	protected String mLookupName;
	protected int mSubGeometryCount;

	@Override
	public BaseObject3D getBaseObject3D() {
		if (mBaseObjects.length == 1)
			return mBaseObjects[0];

		final BaseObject3D container = new BaseObject3D();
		container.isContainer(true);
		for (int i = 0; i < mBaseObjects.length; i++)
			container.addChild(mBaseObjects[i]);

		return container;
	}

	public void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		final AWDLittleEndianDataInputStream awdDis = (AWDLittleEndianDataInputStream) dis;

		// Lookup name, not sure why this is useful.
		mLookupName = awdDis.readVarString();
		RajLog.d("  Lookup Name: " + mLookupName);

		// Count of sub geometries
		mSubGeometryCount = awdDis.readUnsignedShort();
		mBaseObjects = new BaseObject3D[mSubGeometryCount];
		RajLog.d("  Sub Geometry Count: " + mSubGeometryCount);

		// Read properties
		awdDis.readProperties();

		// Read each sub mesh data
		int parsedSubs = 0;
		while (parsedSubs < mSubGeometryCount) {
			long subMeshLength = awdDis.readUnsignedInt();

			// Geometry
			float[] vertices = null;
			int[] indices = null;
			float[] uvs = null;
			float[] normals = null;

			// Read properties
			awdDis.readProperties();

			long bytesRead = 0;

			// Read each data type from the mesh
			while (bytesRead < subMeshLength) {
				int idx = 0;
				int type = awdDis.readUnsignedByte();
				int typeF = awdDis.readUnsignedByte();
				long subLength = awdDis.readUnsignedInt();
				RajLog.d("   Mesh Data: t:" + type + " tf:" + typeF + " l:" + subLength);

				// Process the mesh data by type
				switch ((int) type) {
				case 1: // Vertex positions
					vertices = new float[(int) (subLength / 4)];
					while (idx < vertices.length) {
						// X, Y, Z
						vertices[idx++] = awdDis.readFloat();
						vertices[idx++] = awdDis.readFloat();
						vertices[idx++] = awdDis.readFloat();
					}
					break;
				case 2: // Face indices
					indices = new int[(int) (subLength / 2)];
					while (idx < indices.length)
						indices[idx++] = awdDis.readUnsignedShort();
					break;
				case 3: // UV coordinates
					uvs = new float[(int) (subLength / 4)];
					while (idx < uvs.length)
						uvs[idx++] = awdDis.readFloat();
					break;
				case 4: // Vertex normals
					normals = new float[(int) (subLength / 4)];
					while (idx < normals.length)
						normals[idx++] = awdDis.readFloat();
					break;
				case 5: // Vertex tangents
				case 6: // Joint index
				case 7: // Joint weight
				default:
					// Unknown mesh data, skipping
					awdDis.skip(subLength);
				}

				// Verify the arrays
				if (vertices == null)
					vertices = new float[0];
				if (normals == null)
					normals = new float[0];
				if (uvs == null)
					uvs = new float[0];
				if (indices == null)
					indices = new int[0];

				// Increment the bytes read by the size of the header and the sub block length
				bytesRead += 6 + subLength;
			}

			mBaseObjects[parsedSubs] = new BaseObject3D();
			mBaseObjects[parsedSubs].setData(vertices, normals, uvs, null, indices);

			parsedSubs++;
		}
	}

}
