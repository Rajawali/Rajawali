package rajawali.loader.awd;

import rajawali.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import rajawali.loader.LoaderAWD.BlockHeader;
import rajawali.loader.awd.exceptions.NotImplementedParsingException;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockNamespace extends ABlockParser {

	protected int mNamespace;
	protected String mUri;

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		throw new NotImplementedParsingException();
		/*final AWDLittleEndianDataInputStream awdDis = (AWDLittleEndianDataInputStream) dis;
		final long startPosition = awdDis.getPosition();
		
		mNamespace = dis.readUnsignedByte();
		mUri = awdDis.readVarString();
		
		awdDis.skip(blockHeader.dataLength - (awdDis.getPosition() - startPosition));*/
	}

}
