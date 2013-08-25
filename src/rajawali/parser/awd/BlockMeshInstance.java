package rajawali.parser.awd;

import rajawali.BaseObject3D;
import rajawali.materials.AMaterial;
import rajawali.parser.AWDParser.AWDLittleEndianDataInputStream;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.parser.ParsingException;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockMeshInstance extends ABlockParser {

	protected BaseObject3D mGeometry;
	protected SceneGraphBlock mSceneGraphBlock;
	protected long mGeometryID;

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
					|| !(geomHeader.parser instanceof AExportableBlockParser))
				throw new ParsingException("Invalid block reference.");

			mGeometry = ((AExportableBlockParser) geomHeader.parser).getBaseObject3D();
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

		final float[] mMMatrix = mGeometry.getModelMatrix();
		System.arraycopy(mSceneGraphBlock.transformMatrix, 0, mMMatrix, 0, 16);

		mGeometry.setMaterial(materials[0]);

		dis.skip(blockHeader.blockEnd - dis.getPosition());
	}

}
