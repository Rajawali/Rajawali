package org.rajawali3d.loader.awd;

import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.AwdProperties;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.util.RajLog;
import android.util.SparseArray;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockMetaData extends ABlockParser {

	private static final short PROP_TIMESTAMP = 1;
	private static final short PROP_ENCODER_NAME = 2;
	private static final short PROP_ENCODER_VERSION = 3;
	private static final short PROP_GENERATOR_NAME = 4;
	private static final short PROP_GENERATOR_VERSION = 5;

	private static final SparseArray<Short> EXPECTED_PROPS;

	static {
		EXPECTED_PROPS = new SparseArray<Short>();
		EXPECTED_PROPS.put(PROP_TIMESTAMP, AWDLittleEndianDataInputStream.TYPE_UINT32);
		EXPECTED_PROPS.put(PROP_ENCODER_NAME, AWDLittleEndianDataInputStream.TYPE_AWDSTRING);
		EXPECTED_PROPS.put(PROP_ENCODER_VERSION, AWDLittleEndianDataInputStream.TYPE_AWDSTRING);
		EXPECTED_PROPS.put(PROP_GENERATOR_NAME, AWDLittleEndianDataInputStream.TYPE_AWDSTRING);
		EXPECTED_PROPS.put(PROP_GENERATOR_VERSION, AWDLittleEndianDataInputStream.TYPE_AWDSTRING);
	}

	private long mTimeStamp;
	private String mEncoderName;
	private String mEncoderVersion;
	private String mGeneratorName;
	private String mGeneratorVersion;

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		final AwdProperties properties = dis.readProperties(EXPECTED_PROPS);
		mTimeStamp = (Long) properties.get(PROP_TIMESTAMP);
		mEncoderName = properties.get(PROP_ENCODER_NAME).toString();
		mEncoderVersion = properties.get(PROP_ENCODER_VERSION).toString();
		mGeneratorName = properties.get(PROP_GENERATOR_NAME).toString();
		mGeneratorVersion = properties.get(PROP_GENERATOR_VERSION).toString();

		if (RajLog.isDebugEnabled()) {
			RajLog.d("  Timestamp: " + mTimeStamp);
			RajLog.d("  Encoder Name: " + mEncoderName);
			RajLog.d("  Encoder Version: " + mEncoderVersion);
			RajLog.d("  Generator Name: " + mGeneratorName);
			RajLog.d("  Generator Version: " + mGeneratorVersion);
		}
	}

}
