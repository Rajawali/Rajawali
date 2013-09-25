package rajawali.parser.awd;

import java.util.HashMap;

import rajawali.materials.Material;
import rajawali.materials.textures.Texture;
import rajawali.parser.LoaderAWD.AWDLittleEndianDataInputStream;
import rajawali.parser.LoaderAWD.AwdProperties;
import rajawali.parser.LoaderAWD.BlockHeader;
import rajawali.parser.ParsingException;
import rajawali.parser.awd.exceptions.NotParsableException;
import rajawali.util.RajLog;
import android.util.SparseArray;

/**
 * FIXME Implement 'materialMode' as described by Away3D materialMode block comment
 * <p>
 * MaterialMode defines, if the Parser should create SinglePass or MultiPass Materials<br>
 * Options:<br>
 * 0 (Default / undefined) - All Parsers will create SinglePassMaterials, but the AWD2.1parser will create Materials as
 * they are defined in the file<br>
 * 1 (Force SinglePass) - All Parsers create SinglePassMaterials<br>
 * 2 (Force MultiPass) - All Parsers will create MultiPassMaterials<br>
 */

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockSimpleMaterial extends ATextureBlockParser {

	public static final byte MATERIAL_TYPE_COLOR = 0x1;
	public static final byte MATERIAL_TYPE_TEXTURE = 0x2;

	// TYPES
	protected static final int TYPE_COLOR = 1;
	protected static final int TYPE_TEXTURE = 2;

	// PROPERTY IDENTIFIERS
	protected static final short PROP_COLOR = 1;
	protected static final short PROP_TEXTURE = 2;
	protected static final short PROP_NORMAL_TEXTURE = 3;
	protected static final short PROP_SPEZIAL_ID = 4;
	protected static final short PROP_SMOOTH = 5;
	protected static final short PROP_MIPMAP = 6;
	protected static final short PROP_BOTH_SIDES = 7;
	protected static final short PROP_PRE_MULTIPLIED = 8;
	protected static final short PROP_BLEND_MODE = 9;
	protected static final short PROP_ALPHA = 10;
	protected static final short PROP_ALPHA_BLENDING = 11;
	protected static final short PROP_BINARY_ALPHA_THRESHOLD = 12;
	protected static final short PROP_REPEAT = 13;
	protected static final short PROP_DIFFUSE_LEVEL = 14;
	protected static final short PROP_AMBIENT_LEVEL = 15;
	protected static final short PROP_AMBIENT_COLOR = 16;
	protected static final short PROP_AMBIENT_TEXTURE = 17;
	protected static final short PROP_SPECULAR_LEVEL = 18;
	protected static final short PROP_SPECULAR_GLOSS = 19;
	protected static final short PROP_SPECULAR_COLOR = 20;
	protected static final short PROP_SPECULAR_TEXTURE = 21;
	protected static final short PROP_LIGHT_PICKER = 22;

	private static final SparseArray<Short> EXPECTED_PROPS;

	static {
		EXPECTED_PROPS = new SparseArray<Short>();
		EXPECTED_PROPS.put(PROP_COLOR, AWDLittleEndianDataInputStream.TYPE_UINT32);
		EXPECTED_PROPS.put(PROP_TEXTURE, AWDLittleEndianDataInputStream.TYPE_BADDR);
		// EXPECTED_PROPS.put(PROP_NORMAL_TEXTURE, AWDLittleEndianDataInputStream.TYPE_BADDR);
		// EXPECTED_PROPS.put(PROP_SPEZIAL_ID, AWDLittleEndianDataInputStream.TYPE_UINT8);
		// EXPECTED_PROPS.put(PROP_SMOOTH, AWDLittleEndianDataInputStream.TYPE_BOOL);
		// EXPECTED_PROPS.put(PROP_MIPMAP, AWDLittleEndianDataInputStream.TYPE_BOOL);
		// EXPECTED_PROPS.put(PROP_BOTH_SIDES, AWDLittleEndianDataInputStream.TYPE_BOOL);
		// EXPECTED_PROPS.put(PROP_PRE_MULTIPLIED, AWDLittleEndianDataInputStream.TYPE_BOOL);
		// EXPECTED_PROPS.put(PROP_BLEND_MODE, AWDLittleEndianDataInputStream.TYPE_UINT8);
		EXPECTED_PROPS.put(PROP_ALPHA, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_ALPHA_BLENDING, AWDLittleEndianDataInputStream.TYPE_BOOL);
		EXPECTED_PROPS.put(PROP_BINARY_ALPHA_THRESHOLD, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_REPEAT, AWDLittleEndianDataInputStream.TYPE_BOOL);
		// EXPECTED_PROPS.put(PROP_DIFFUSE_LEVEL, AWDLittleEndianDataInputStream.TYPE_NR);
		// EXPECTED_PROPS.put(PROP_AMBIENT_LEVEL, AWDLittleEndianDataInputStream.TYPE_NR);
		// EXPECTED_PROPS.put(PROP_AMBIENT_COLOR, AWDLittleEndianDataInputStream.TYPE_UINT32);
		// EXPECTED_PROPS.put(PROP_AMBIENT_TEXTURE, AWDLittleEndianDataInputStream.TYPE_BADDR);
		// EXPECTED_PROPS.put(PROP_SPECULAR_LEVEL, AWDLittleEndianDataInputStream.TYPE_NR);
		// EXPECTED_PROPS.put(PROP_SPECULAR_GLOSS, AWDLittleEndianDataInputStream.TYPE_NR);
		// EXPECTED_PROPS.put(PROP_SPECULAR_COLOR, AWDLittleEndianDataInputStream.TYPE_UINT32);
		// EXPECTED_PROPS.put(PROP_SPECULAR_TEXTURE, AWDLittleEndianDataInputStream.TYPE_BADDR);
		// EXPECTED_PROPS.put(PROP_LIGHT_PICKER, AWDLittleEndianDataInputStream.TYPE_BADDR);
	}

	protected Material mMaterial;
	protected String mLookupName;
	protected byte mMaterialType;
	protected byte mShadingMethodCount;
	protected int mSpezialType;

	@Override
	public Material getMaterial() {
		return mMaterial;
	}

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// Material type
		mMaterialType = dis.readByte();

		// Shading method count
		mShadingMethodCount = dis.readByte();

		// Read properties
		final AwdProperties properties = dis.readProperties(EXPECTED_PROPS);
		mSpezialType = (Integer) properties.get((short) 4, 0);

		// Spezial type 2 or higher is not supported in the specification
		if (mSpezialType >= 2)
			throw new NotParsableException("Spezial type " + mSpezialType + " is not currently supported.");

		// Debug
		RajLog.d("  Lookup Name: " + mLookupName);
		RajLog.d("  Material Type: " + mMaterialType);
		RajLog.d("  Shading Methods: " + mShadingMethodCount);
		RajLog.d("  Spezial Type: " + mSpezialType);

		// Parse the methods
		for (int i = 0; i < mShadingMethodCount; ++i) {
			// TODO Looking at the AWD source, this appears to be completely unused?
			dis.readUnsignedShort();
			dis.readProperties();
			dis.readUserAttributes(null);
		}

		final HashMap<String, Object> attributes = new HashMap<String, Object>();
		dis.readUserAttributes(attributes);

		mMaterial = new Material();

		switch (mMaterialType) {
		case TYPE_COLOR:
			// default to 0xcccccc per AWD implementation
			final long color = (Long) properties.get((short) 1, 0xcccccc);
			final float[] colorFloat = new float[4];
			colorFloat[0] = ((color >> 16) & 0xff) / 255.0f;
			colorFloat[1] = ((color >> 8) & 0xff) / 255.0f;
			colorFloat[2] = (color & 0xff) / 255.0f;
			colorFloat[3] = (((int) ((Double) properties.get(PROP_ALPHA, 1.0d) * 0xff)) & 0xff) / 255.0f;
			mMaterial.setColor(colorFloat);
			break;
		case TYPE_TEXTURE:
			final long textureId = (Long) properties.get(PROP_TEXTURE, 0L);
			if (textureId == 0)
				throw new ParsingException("Texture ID can not be 0, document corrupt or unsupported version.");

			final BlockHeader lookupHeader = blockHeader.blockHeaders.get((short) textureId);
			if (lookupHeader == null || lookupHeader.parser == null
					|| !(lookupHeader.parser instanceof BlockBitmapTexture))
				throw new ParsingException("Invalid block reference.");

			mMaterial.addTexture(new Texture(mLookupName, ((BlockBitmapTexture) lookupHeader.parser).mBitmap));
			break;
		}

	}

}
