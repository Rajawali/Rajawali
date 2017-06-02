package org.rajawali3d.loader.awd;

import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.loader.awd.exceptions.NotImplementedParsingException;

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
