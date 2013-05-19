package rajawali.parser.awd;

import java.io.IOException;

import rajawali.parser.AWDParser.IBlockParser;
import rajawali.util.LittleEndianDataInputStream;

public abstract class ABlockParser implements IBlockParser {

	protected final void readProperties(LittleEndianDataInputStream dis) throws IOException {
		// Determine the length of the properties
		final long propsLength = dis.readUnsignedInt();
		
		// TODO need to figure out what uses and needs this so I can better understand implementation

		// skip properties until an implementation can be determined
		dis.skip(propsLength);
	}

}
