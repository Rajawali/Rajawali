package rajawali.parser.awd;

import rajawali.BaseObject3D;
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
public class BlockTriangleGeometry extends ABlockParser {

	protected BaseObject3D[] baseObjects;
	protected String lookupName;
	protected int subGeometryCount;

	@Override
	public BaseObject3D getBaseObject3D() {
		if (baseObjects.length == 1)
			return baseObjects[0];

		final BaseObject3D container = new BaseObject3D();
		container.isContainer(true);
		for (int i = 0; i < baseObjects.length; i++)
			container.addChild(baseObjects[i]);

		return container;
	}

	public void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		// Lookup name, not sure why this is useful.
		final int lookupNameLength = dis.readUnsignedShort();
		lookupName = lookupNameLength == 0 ? "" : dis.readString(lookupNameLength);
		RajLog.d("  Lookup Name: " + lookupName);

		// Count of sub geometries
		subGeometryCount = dis.readUnsignedShort();
		baseObjects = new BaseObject3D[subGeometryCount];
		RajLog.d("  Sub Geometry Count: " + subGeometryCount);

		// Read properties
		readProperties(dis);

		// Read each sub mesh data
		int parsedSubs = 0;
		while (parsedSubs < subGeometryCount) {
			long subMeshLength = dis.readUnsignedInt();

			// Geometry
			float[] vertices = null;
			int[] indices = null;
			float[] uvs = null;
			float[] normals = null;

			// Read properties
			readProperties(dis);

			long bytesRead = 0;

			// Read each data type from the mesh
			while (bytesRead < subMeshLength) {
				int idx = 0;
				int type = dis.readUnsignedByte();
				int typeF = dis.readUnsignedByte();
				long subLength = dis.readUnsignedInt();
				RajLog.d("   Mesh Data: t:" + type + " tf:" + typeF + " l:" + subLength);

				// Process the mesh data by type
				switch ((int) type) {
				case 1: // Vertices
					vertices = new float[(int) (subLength / 4)];
					while (idx < vertices.length) {
						// X, Y, Z
						vertices[idx++] = dis.readFloat();
						vertices[idx++] = dis.readFloat();
						vertices[idx++] = dis.readFloat();
					}
					break;
				case 2: // Indices
					indices = new int[(int) (subLength / 2)];
					while (idx < indices.length)
						indices[idx++] = dis.readUnsignedShort();
					break;
				case 3: // Texture Coords
					uvs = new float[(int) (subLength / 4)];
					while (idx < uvs.length)
						uvs[idx++] = dis.readFloat();
					break;
				case 4: // Normals
					normals = new float[(int) (subLength / 4)];
					while (idx < normals.length)
						normals[idx++] = dis.readFloat();
					break;
				case 5: // Not Used?
				case 6: // Weight Indices
				case 7: // Weights
				default:
					// Unknown mesh data, skipping
					dis.skip(subLength);
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

			baseObjects[parsedSubs] = new BaseObject3D();
			baseObjects[parsedSubs].setData(vertices, normals, uvs, null, indices);

			parsedSubs++;
		}
	}

}
