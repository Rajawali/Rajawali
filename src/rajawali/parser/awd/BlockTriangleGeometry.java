package rajawali.parser.awd;

import org.apache.http.ParseException;

import rajawali.BaseObject3D;
import rajawali.parser.AWDParser.AWDLittleEndianDataInputStream;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;
import android.util.SparseArray;

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

		// Count of sub geometries
		mSubGeometryCount = awdDis.readUnsignedShort();

		// TODO Meshes need to be joined in some fashion. This might work. Need to test it I suppose.
		// One object for each sub geometry
		mBaseObjects = new BaseObject3D[mSubGeometryCount];

		// Debug
		RajLog.d("  Lookup Name: " + mLookupName);
		RajLog.d("  Sub Geometry Count: " + mSubGeometryCount);

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
		awdDis.readProperties(properties);

		// Calculate the sizes
		final int geoPrecisionSize = blockHeader.globalPrecisionGeo ? 8 : 4;

		// Read each sub mesh data
		for (int parsedSub = 0; parsedSub < mSubGeometryCount; ++parsedSub) {
			long subMeshEnd = awdDis.getPosition() + awdDis.readUnsignedInt();

			// Geometry
			float[] vertices = null;
			int[] indices = null;
			float[] uvs = null;
			float[] normals = null;

			// Skip reading of mesh properties for now (per AWD implementation)
			awdDis.readProperties();

			// Read each data type from the mesh
			while (awdDis.getPosition() < subMeshEnd) {
				int idx = 0;
				int type = awdDis.readUnsignedByte();
				int typeF = awdDis.readUnsignedByte();
				long subLength = awdDis.readUnsignedInt();
				long subEnd = awdDis.getPosition() + subLength;
				RajLog.d("   Mesh Data: t:" + type + " tf:" + typeF + " l:" + subLength + " ls:" + awdDis.getPosition()
						+ " le:" + subEnd);

				// Process the mesh data by type
				switch ((int) type) {
				case 1: // Vertex positions
					vertices = new float[(int) (subLength / geoPrecisionSize)];
					while (idx < vertices.length) {
						// X, Y, Z
						vertices[idx++] = (float) awdDis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
						vertices[idx++] = (float) awdDis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
						vertices[idx++] = (float) awdDis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
					}
					break;
				case 2: // Face indices
					indices = new int[(int) (subLength / 2)];
					while (idx < indices.length)
						indices[idx++] = awdDis.readUnsignedShort();
					break;
				case 3: // UV coordinates
					uvs = new float[(int) (subLength / geoPrecisionSize)];
					while (idx < uvs.length)
						uvs[idx++] = (float) awdDis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
					break;
				case 4: // Vertex normals
					normals = new float[(int) (subLength / geoPrecisionSize)];
					while (idx < normals.length)
						normals[idx++] = (float) awdDis.readPrecisionNumber(blockHeader.globalPrecisionGeo);
					break;
				case 5: // Vertex tangents
				case 6: // Joint index
				case 7: // Joint weight
				default:
					// Unknown mesh data, skipping
					awdDis.skip(subLength);
				}

				// Validate each mesh data ending. This is a sanity check against precision flags.
				if (awdDis.getPosition() != subEnd)
					throw new ParseException("Unexpected ending. Expected " + subEnd + ". Got " + awdDis.getPosition());
			}

			awdDis.readUserAttributes(null);

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
			mBaseObjects[parsedSub] = new BaseObject3D();
			mBaseObjects[parsedSub].setData(vertices, normals, uvs, null, indices);
		}

		awdDis.readUserAttributes(null);
	}
}
