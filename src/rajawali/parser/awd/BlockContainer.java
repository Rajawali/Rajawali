package rajawali.parser.awd;

import rajawali.Object3D;
import rajawali.parser.LoaderAWD.AWDLittleEndianDataInputStream;
import rajawali.parser.LoaderAWD.BlockHeader;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockContainer extends ABaseObjectBlockParser {

	protected Object3D mBaseObject;
	protected SceneGraphBlock mSceneGraphBlock;
	protected String mLookupName;

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		mBaseObject = new Object3D();

		mSceneGraphBlock = new SceneGraphBlock();
		mSceneGraphBlock.readGraphData(blockHeader, dis);

		// TODO need to 'getAssetByID' which appears to be the implemented method for referencing other parsed objects

		// Container properties did not exist until 2.1, as such the container will be default 0, 0, 0
		if (blockHeader.awdVersion == 2 && blockHeader.awdRevision == 1) {
			// FIXME will have to implement this
			dis.readProperties();
		} else {
			dis.readProperties();
		}

		mBaseObject.setVisible(dis.readByte() != 0);
		mBaseObject.isContainer(true);
	}

	@Override
	public Object3D getBaseObject3D() {
		return mBaseObject;
	}

}
