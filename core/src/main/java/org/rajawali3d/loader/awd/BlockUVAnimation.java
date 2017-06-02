package org.rajawali3d.loader.awd;

import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.loader.awd.exceptions.NotImplementedParsingException;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockUVAnimation extends ABlockParser {

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		throw new NotImplementedParsingException();
	}

}
