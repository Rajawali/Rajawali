package rajawali.parser.awd;

import java.io.IOException;

import rajawali.parser.AWDParser.AWDLittleEndianDataInputStream;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.parser.ParsingException;
import rajawali.util.RajLog;

public class SceneGraphBlock {

	public int parentID;
	public float[] transformMatrix = new float[16];
	public String lookupName;

	public void readGraphData(BlockHeader blockHeader, AWDLittleEndianDataInputStream awddis) throws IOException, ParsingException {
		// parent id, reference to previously defined object
		parentID = awddis.readInt();
		
		// Transformation matrix
		awddis.readMatrix3D(transformMatrix, blockHeader.globalPrecisionMatrix);
		
		// Lookup name
		lookupName = awddis.readVarString();
		RajLog.d("  Lookup Name: " + lookupName);
	}

}
