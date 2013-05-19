package rajawali.parser.awd;

import java.util.Arrays;

import rajawali.Geometry3D;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;

public class BlockTriangleGeometry extends ABlockParser {

	protected Geometry3D[] geoms;
	protected String lookupName;
	protected int subGeometryCount;

	public void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		// Lookup name, not sure why this is useful.
		final int lookupNameLength = dis.readUnsignedShort();
		lookupName = dis.readString(lookupNameLength);
		RajLog.d("  Lookup Name: " + lookupName);

		// Count of sub geometries
		subGeometryCount = dis.readUnsignedShort();
		geoms = new Geometry3D[subGeometryCount];
		RajLog.d("  Sub Geometry Count: " + subGeometryCount);

		// Read properties
		readProperties(dis);

		// Read the mesh
		int parsedSubs = 0;
		while (parsedSubs < subGeometryCount) {
			long subMeshLength = dis.readUnsignedInt();

			// Geometry
			float[] vertices = null;
			float[] indices = null;
			float[] uvs = null;
			float[] normals = null;

			// Read properties
			readProperties(dis);

			long bytesRead = 0;
			while (bytesRead < subMeshLength) {
				int idx = 0;
				int type = dis.readUnsignedByte();
				int typeF = dis.readUnsignedByte();
				long subLength = dis.readUnsignedInt();
				RajLog.d("   mesh block: " + type + " " + typeF + " " + subLength);

				// Process the mesh data by type
				switch ((int) type) {
				case 1: // Vertices
					vertices = new float[(int) (subLength / 4)];
					while (idx < vertices.length) {
						// X, Y, Z
						vertices[idx++] = dis.readFloat();
						vertices[idx++] = dis.readFloat();
						vertices[idx++] = dis.readFloat();
						bytesRead += 12;
					}
					break;
				default:
					// Nothing to do
					bytesRead += subLength;
					dis.skip(subLength);
				}
			}

			geoms[parsedSubs] = new Geometry3D();
			geoms[parsedSubs].setVertices(vertices);
			RajLog.d(Arrays.toString(vertices));

			parsedSubs++;
		}
	}

}
