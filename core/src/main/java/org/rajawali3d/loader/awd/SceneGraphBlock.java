package org.rajawali3d.loader.awd;

import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.util.RajLog;

import java.io.IOException;

/**
 * @author Ian Thomas (toxicbakery@gmail.com)
 */
public class SceneGraphBlock {

    public final Matrix4 transformMatrix = new Matrix4();

    public int parentID;
    public String lookupName;

    public void readGraphData(BlockHeader blockHeader, AWDLittleEndianDataInputStream awddis) throws IOException,
            ParsingException {
        // parent id, reference to previously defined object
        parentID = awddis.readInt();

        // Transformation matrix
        awddis.readMatrix3D(transformMatrix, blockHeader.globalPrecisionMatrix, true);

        // Lookup name
        lookupName = awddis.readVarString();
        if (RajLog.isDebugEnabled())
            RajLog.d("  Lookup Name: " + lookupName);
    }

}
