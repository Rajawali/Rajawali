package rajawali.parser.awd;

import rajawali.parser.AWDParser.AWDLittleEndianDataInputStream;
import rajawali.parser.AWDParser.AwdProperties;
import rajawali.parser.AWDParser.BlockHeader;
import rajawali.parser.awd.exceptions.NotParsableException;
import rajawali.util.RajLog;
import android.util.SparseArray;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockSimpleMaterial extends ABlockParser {

	public static final byte MATERIAL_TYPE_COLOR = 0x1;
	public static final byte MATERIAL_TYPE_TEXTURE = 0x2;

	protected String mLookupName;
	protected byte mMaterialType;
	protected byte mShadingMethodCount;
	protected int mSpezialType;

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// Material type
		mMaterialType = dis.readByte();

		// Shading method count
		mShadingMethodCount = dis.readByte();

		// Get properties
		final SparseArray<Short> expected = new SparseArray<Short>();
		expected.put(1, AWDLittleEndianDataInputStream.TYPE_UINT32);
		expected.put(2, AWDLittleEndianDataInputStream.TYPE_BADDR);
		expected.put(3, AWDLittleEndianDataInputStream.TYPE_BADDR);
		expected.put(4, AWDLittleEndianDataInputStream.TYPE_UINT8);
		expected.put(5, AWDLittleEndianDataInputStream.TYPE_BOOL);
		expected.put(6, AWDLittleEndianDataInputStream.TYPE_BOOL);
		expected.put(7, AWDLittleEndianDataInputStream.TYPE_BOOL);
		expected.put(8, AWDLittleEndianDataInputStream.TYPE_BOOL);
		expected.put(9, AWDLittleEndianDataInputStream.TYPE_UINT8);
		expected.put(10, AWDLittleEndianDataInputStream.TYPE_NR);
		expected.put(11, AWDLittleEndianDataInputStream.TYPE_BOOL);
		expected.put(12, AWDLittleEndianDataInputStream.TYPE_NR);
		expected.put(13, AWDLittleEndianDataInputStream.TYPE_BOOL);
		expected.put(15, AWDLittleEndianDataInputStream.TYPE_NR);
		expected.put(16, AWDLittleEndianDataInputStream.TYPE_UINT32);
		expected.put(17, AWDLittleEndianDataInputStream.TYPE_BADDR);
		expected.put(18, AWDLittleEndianDataInputStream.TYPE_NR);
		expected.put(19, AWDLittleEndianDataInputStream.TYPE_NR);
		expected.put(20, AWDLittleEndianDataInputStream.TYPE_UINT32);
		expected.put(21, AWDLittleEndianDataInputStream.TYPE_BADDR);
		expected.put(22, AWDLittleEndianDataInputStream.TYPE_BADDR);

		// Interpret properties
		final AwdProperties properties = dis.readProperties(expected);
		mSpezialType = (Integer) properties.get(4, 0);

		// Spezial type 2 or higher is not supported in the specification
		if (mSpezialType >= 2)
			throw new NotParsableException("Spezial type " + mSpezialType + " is not currently supported.");

		// Debug
		RajLog.d("  Lookup Name: " + mLookupName);
		RajLog.d("  Material Type: " + mMaterialType);
		RajLog.d("  Shading Methods: " + mShadingMethodCount);
		RajLog.d("  Spezial Type: " + mSpezialType);

		// Temporarily skip to end of block for dev purposes
		dis.skip(blockHeader.blockEnd - dis.getPosition());

	}

}
