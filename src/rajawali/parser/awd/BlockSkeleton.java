package rajawali.parser.awd;

import rajawali.parser.LoaderAWD.AWDLittleEndianDataInputStream;
import rajawali.parser.LoaderAWD.BlockHeader;
import rajawali.parser.awd.exceptions.NotImplementedParsingException;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockSkeleton extends ABlockParser {

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		throw new NotImplementedParsingException();
	}

}
