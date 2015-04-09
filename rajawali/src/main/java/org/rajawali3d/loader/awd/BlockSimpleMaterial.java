package org.rajawali3d.loader.awd;

import java.util.HashMap;
import java.util.UUID;

import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.AwdProperties;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.awd.exceptions.NotParsableException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.NormalMapTexture;
import org.rajawali3d.materials.textures.SpecularMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.util.RajLog;

import android.graphics.Bitmap;
import android.graphics.Color;
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
		EXPECTED_PROPS.put(PROP_NORMAL_TEXTURE, AWDLittleEndianDataInputStream.TYPE_BADDR);
		EXPECTED_PROPS.put(PROP_SPEZIAL_ID, AWDLittleEndianDataInputStream.TYPE_UINT8);
		EXPECTED_PROPS.put(PROP_SMOOTH, AWDLittleEndianDataInputStream.TYPE_BOOL);
		EXPECTED_PROPS.put(PROP_MIPMAP, AWDLittleEndianDataInputStream.TYPE_BOOL);
		EXPECTED_PROPS.put(PROP_BOTH_SIDES, AWDLittleEndianDataInputStream.TYPE_BOOL);
		EXPECTED_PROPS.put(PROP_PRE_MULTIPLIED, AWDLittleEndianDataInputStream.TYPE_BOOL);
		EXPECTED_PROPS.put(PROP_BLEND_MODE, AWDLittleEndianDataInputStream.TYPE_UINT8);
		EXPECTED_PROPS.put(PROP_ALPHA, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_ALPHA_BLENDING, AWDLittleEndianDataInputStream.TYPE_BOOL);
		EXPECTED_PROPS.put(PROP_BINARY_ALPHA_THRESHOLD, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_REPEAT, AWDLittleEndianDataInputStream.TYPE_BOOL);
		EXPECTED_PROPS.put(PROP_DIFFUSE_LEVEL, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_AMBIENT_LEVEL, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_AMBIENT_COLOR, AWDLittleEndianDataInputStream.TYPE_UINT32);
		EXPECTED_PROPS.put(PROP_AMBIENT_TEXTURE, AWDLittleEndianDataInputStream.TYPE_BADDR);
		EXPECTED_PROPS.put(PROP_SPECULAR_LEVEL, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_SPECULAR_GLOSS, AWDLittleEndianDataInputStream.TYPE_NR);
		EXPECTED_PROPS.put(PROP_SPECULAR_COLOR, AWDLittleEndianDataInputStream.TYPE_UINT32);
		EXPECTED_PROPS.put(PROP_SPECULAR_TEXTURE, AWDLittleEndianDataInputStream.TYPE_BADDR);
		EXPECTED_PROPS.put(PROP_LIGHT_PICKER, AWDLittleEndianDataInputStream.TYPE_BADDR);
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
        if (RajLog.isDebugEnabled()) {
            RajLog.d("  Lookup Name: " + mLookupName);
            RajLog.d("  Material Type: " + mMaterialType);
            RajLog.d("  Shading Methods: " + mShadingMethodCount);
            RajLog.d("  Spezial Type: " + mSpezialType);
        }

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

		long	diffuseTexture = 0, ambientTexture = 0,
				diffuseColor = 0;

		// remove any chars that will break shader compile
		String cleanName = cleanName(mLookupName);

		switch (mMaterialType) {
		case TYPE_COLOR:
			// default to 0xcccccc per AWD implementation
			diffuseColor = (Long) properties.get((short) 1, 0xccccccL);
			final float[] colorFloat = new float[4];
			colorFloat[0] = ((diffuseColor >> 16) & 0xff) / 255.0f;
			colorFloat[1] = ((diffuseColor >> 8) & 0xff) / 255.0f;
			colorFloat[2] = (diffuseColor & 0xff) / 255.0f;
			colorFloat[3] = (((int) ((Double) properties.get(PROP_ALPHA, 1.0d) * 0xff)) & 0xff) / 255.0f;
			mMaterial.setColor(colorFloat);
			break;
		case TYPE_TEXTURE:
			diffuseTexture = (Long) properties.get(PROP_TEXTURE, 0L);
			ambientTexture = (Long) properties.get(PROP_AMBIENT_TEXTURE, 0L);

			if(diffuseTexture == 0 && ambientTexture == 0)
				throw new ParsingException("Texture ID can not be 0, document corrupt or unsupported version.");

			if(diffuseTexture > 0)
				mMaterial.addTexture(new Texture(cleanName + diffuseTexture, lookup(blockHeader, diffuseTexture)));

			if(ambientTexture > 0)
				mMaterial.addTexture(new Texture(cleanName + ambientTexture, lookup(blockHeader, ambientTexture)));

			mMaterial.setColorInfluence(0);

			break;
		}

		// either material type can have specular and/or normal maps
		long specularTexture = (Long) properties.get(PROP_SPECULAR_TEXTURE, 0L);
		long normalTexture = (Long) properties.get(PROP_NORMAL_TEXTURE, 0L);

		// either material type can have settings for diffuse, ambient, specular lighting
		double diffuseLevel = (Double) properties.get(PROP_DIFFUSE_LEVEL, 1.0d);

		long ambientColor = (Long) properties.get(PROP_AMBIENT_COLOR, (long)Color.WHITE);
		double ambientLevel = (Double) properties.get(PROP_AMBIENT_LEVEL, 1.0d);

		long specularColor = (Long) properties.get(PROP_SPECULAR_COLOR, (long)Color.WHITE);
		double specularGloss = (Double) properties.get(PROP_SPECULAR_GLOSS, 50.0D);
		double specularLevel = (Double) properties.get(PROP_SPECULAR_LEVEL, 1.0d);

		if(specularTexture > 0)
			mMaterial.addTexture(new SpecularMapTexture(cleanName + specularTexture, lookup(blockHeader, specularTexture)));

		if(normalTexture > 0)
			mMaterial.addTexture(new NormalMapTexture(cleanName + normalTexture, lookup(blockHeader, normalTexture)));

		// ambient 1.0 is default, washes-out object; assume < 1 is intended
		ambientLevel = (ambientLevel < 1.0 ? ambientLevel : 0.0);

		mMaterial.setAmbientIntensity(ambientLevel, ambientLevel, ambientLevel);
		mMaterial.setAmbientColor((int)ambientColor);

		if(diffuseLevel > 0) // always 1.0 in current AWD implementation
			mMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

		if(specularLevel > 0)
		{
			SpecularMethod.Phong phong = new SpecularMethod.Phong();

			phong.setSpecularColor((int)specularColor);
			phong.setShininess((float)specularGloss);
			phong.setIntensity((float)specularLevel);

			mMaterial.setSpecularMethod(phong);
		}

		// don't enable lighting if specular and diffuse are absent, otherwise enable
		if(diffuseLevel > 0 || specularLevel > 0)
			mMaterial.enableLighting(true);
	}

	private Bitmap lookup(BlockHeader blockHeader, long texref) throws ParsingException
	{
		final BlockHeader lookupHeader = blockHeader.blockHeaders.get((short) texref);

		if (lookupHeader == null || lookupHeader.parser == null
				|| !(lookupHeader.parser instanceof BlockBitmapTexture))
			throw new ParsingException("Invalid block reference.");

		return ((BlockBitmapTexture) lookupHeader.parser).mBitmap;
	}

	private final static String TEX_PREFIX = "TEX_";

	private String cleanName(String name)
	{
		// if null, force generation of a new and valid name
		String clean = (name == null ? "" : name.replaceAll("\\W", ""));

		if(clean.length() == 0 || Character.isDigit(clean.charAt(0)))
			clean = TEX_PREFIX + UUID.randomUUID().toString().replaceAll("\\W", "");

		return clean;
	}
}
