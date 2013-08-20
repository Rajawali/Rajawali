package rajawali.parser.awd;

import rajawali.parser.AWDParser.BlockHeader;
import rajawali.util.LittleEndianDataInputStream;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockMeshInstance extends ABlockParser {

	public void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		throw new NotImplementedParsingException();
	}

}
