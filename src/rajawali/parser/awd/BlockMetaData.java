package rajawali.parser.awd;

import rajawali.parser.AWDParser.BlockHeader;
import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockMetaData extends ABlockParser {

	public void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		RajLog.e(this + ": Not yet implemented.");
		dis.skip(blockHeader.dataLength);
	}

}
