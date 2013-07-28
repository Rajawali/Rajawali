package rajawali.parser.awd;

import rajawali.BaseObject3D;
import rajawali.parser.AWDParser.AWDLittleEndianDataInputStream;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.util.LittleEndianDataInputStream;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockContainer extends AExportableBlockParser {

	protected BaseObject3D mBaseObject;
	protected SceneGraphBlock mSceneGraphBlock;
	protected String mLookupName;

	public void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		final AWDLittleEndianDataInputStream awdDis = (AWDLittleEndianDataInputStream) dis;

		mBaseObject = new BaseObject3D();

		mSceneGraphBlock = new SceneGraphBlock();
		mSceneGraphBlock.readGraphData(blockHeader, awdDis);

		// TODO need to 'getAssetByID' which appears to be the implemented method for referencing other parsed objects

		// Container properties did not exist until 2.1, as such the container will be default 0, 0, 0
		if (blockHeader.awdVersion == 2 && blockHeader.awdRevision == 1) {
			// FIXME will have to implement this
			awdDis.readProperties();
		} else {
			awdDis.readProperties();
		}

		mBaseObject.setVisible(awdDis.readByte() != 0);
		mBaseObject.isContainer(true);
	}

	@Override
	public BaseObject3D getBaseObject3D() {
		return mBaseObject;
	}

}
