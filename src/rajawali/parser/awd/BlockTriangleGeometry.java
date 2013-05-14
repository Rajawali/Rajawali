package rajawali.parser.awd;

import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;

public class BlockTriangleGeometry extends ABlockParser {
	
	protected short headerSubGeometryCount;

	public void parseBlock(LittleEndianDataInputStream dis, int blockLength, boolean debug) throws Exception {
		if(debug) {
			RajLog.i(getClass().getSimpleName() + "paserBlock()");
		}
		
		dis.skip(blockLength);
	}

}
