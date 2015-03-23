package rajawali.loader.awd;

import rajawali.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import rajawali.loader.LoaderAWD.BlockHeader;
import rajawali.loader.awd.exceptions.NotImplementedParsingException;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockSkeletonAnimation extends ABlockParser {

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		throw new NotImplementedParsingException();
	}

}
