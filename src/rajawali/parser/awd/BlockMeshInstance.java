package rajawali.parser.awd;

import rajawali.BaseObject3D;
import rajawali.materials.AMaterial;
import rajawali.math.Matrix4;
import rajawali.parser.AWDParser.AWDLittleEndianDataInputStream;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.parser.ParsingException;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockMeshInstance extends AExportableBlockParser {

	protected BaseObject3D mGeometry;
	protected SceneGraphBlock mSceneGraphBlock;
	protected long mGeometryID;

	@Override
	public BaseObject3D getBaseObject3D() {
		return mGeometry;
	}

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Parse scene block
		mSceneGraphBlock = new SceneGraphBlock();
		mSceneGraphBlock.readGraphData(blockHeader, dis);

		// Block id for geometry
		mGeometryID = dis.readUnsignedInt();

		// Lookup the geometry or create it if it does not exist.
		final BlockHeader geomHeader = blockHeader.blockHeaders.get((short) mGeometryID);
		if (geomHeader == null) {
			mGeometry = new BaseObject3D();
		} else {
			if (geomHeader.parser == null
					|| !(geomHeader.parser instanceof ABaseObjectBlockParser))
				throw new ParsingException("Invalid block reference.");

			mGeometry = ((ABaseObjectBlockParser) geomHeader.parser).getBaseObject3D().clone();
		}

		// Apply the materials
		final int materialCount = dis.readUnsignedShort();
		final AMaterial[] materials = new AMaterial[materialCount];
		for (int i = 0; i < materialCount; ++i) {
			final long materialID = dis.readUnsignedInt();
			if (materialID == 0) {
				materials[i] = getDefaultMaterial();
				materials[i].addTexture(getDefaultTexture());
			} else {
				final BlockHeader materialHeader = blockHeader.blockHeaders.get((short) materialID);
				if (materialHeader == null || materialHeader.parser == null
						|| !(materialHeader.parser instanceof ATextureBlockParser))
					throw new ParsingException("Invalid block reference " + materialID);

				materials[i] = ((ATextureBlockParser) materialHeader.parser).getMaterial();
			}
		}

		final float[] m = mSceneGraphBlock.transformMatrix;
		final Matrix4 matrix = new Matrix4(
				m[0], m[4], m[8], m[12],
				m[1], m[5], m[9], m[13],
				m[2], m[6], m[10], m[14],
				m[3], m[7], m[11], m[15]
				);

		// Set translation
		mGeometry.setPosition(matrix.getTranslation());
		
		// Set scale
		mGeometry.setScale(matrix.getScale());
		
		// Set rotation
		mGeometry.setOrientation(matrix.getRotation());

		mGeometry.setMaterial(materials[0]);

		dis.skip(blockHeader.blockEnd - dis.getPosition());
	}

}
