package rajawali.parser.awd;

import rajawali.parser.AWDParser.BlockHeader;
import rajawali.util.LittleEndianDataInputStream;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockNamespace extends ABlockParser {

	protected int mNamespace;
	protected String mUri;

	public void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		throw new NotImplementedParsingException();
		/*final AWDLittleEndianDataInputStream awdDis = (AWDLittleEndianDataInputStream) dis;
		final long startPosition = awdDis.getPosition();
		
		mNamespace = dis.readUnsignedByte();
		mUri = awdDis.readVarString();
		
		awdDis.skip(blockHeader.dataLength - (awdDis.getPosition() - startPosition));*/
	}

}
