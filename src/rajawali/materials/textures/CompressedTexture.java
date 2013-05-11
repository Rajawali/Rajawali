package rajawali.materials.textures;


public class CompressedTexture extends ASingleTexture {
	// Paletted texture constants
	// Referenced from OpenGL ES 2.0 extension C header from Khronos Group
	// http://www.khronos.org/registry/gles/api/2.0/gl2ext.h
	private static final int GL_PALETTE4_RGB8_OES = 0x8B90;
	private static final int GL_PALETTE4_RGBA8_OES = 0x8B91;
	private static final int GL_PALETTE4_R5_G6_B5_OES = 0x8B92;
	private static final int GL_PALETTE4_RGBA4_OES = 0x8B93;
	private static final int GL_PALETTE4_RGB5_A1_OES = 0x8B94;
	private static final int GL_PALETTE8_RGB8_OES = 0x8B95;
	private static final int GL_PALETTE8_RGBA8_OES = 0x8B96;
	private static final int GL_PALETTE8_R5_G6_B5_OES = 0x8B97;
	private static final int GL_PALETTE8_RGBA4_OES = 0x8B98;
	private static final int GL_PALETTE8_RGB5_A1_OES = 0x8B99;

	/**
	 * Texture compression type. Texture compression can significantly increase the performance by reducing memory
	 * requirements and making more efficient use of memory bandwidth.
	 */
	public enum CompressionType {
		NONE,
		ETC1,
		PALETTED,
		THREEDC,
		ATC,
		DXT1,
		PVRTC
	};

	/**
	 * Texture palette format.
	 */
	public enum PaletteFormat {
		PALETTE4_RGB8,
		PALETTE4_RGBA8,
		PALETTE4_R5_G6_B5,
		PALETTE4_RGBA4,
		PALETTE4_RGB5_A1,
		PALETTE8_RGB8,
		PALETTE8_RGBA8,
		PALETTE8_R5_G6_B5,
		PALETTE8_RGBA4,
		PALETTE8_RGB5_A1
	};

	/**
	 * 3DC Texture compression format.
	 * 
	 */
	public enum ThreeDcFormat {
		X,
		XY
	};

	/**
	 * ATC Texture compression format.
	 * 
	 */
	public enum AtcFormat {
		RGB,
		RGBA_EXPLICIT,
		RGBA_INTERPOLATED
	};

	/**
	 * DXT1 Texture compression format.
	 * 
	 */
	public enum Dxt1Format {
		RGB,
		RGBA
	};

	public enum PvrtcFormat {
		RGB_2BPP,
		RGB_4BPP,
		RGBA_2BPP,
		RGBA_4BPP
	};
	
	/**
	 * Texture compression type
	 */
	private CompressionType mCompressionType;
	/**
	 * Texture palette format. See {@link PaletteFormat}.
	 */
	private PaletteFormat mPaletteFormat;
	/**
	 * 3DC Texture Compression format. See {@link ThreeDcFormat}.
	 */
	private ThreeDcFormat mThreeDcFormat;
	/**
	 * ATC Texture Compression format. See {@link AtcFormat}.
	 */
	private AtcFormat mAtcFormat;
	/**
	 * DXT1 Texture Compression format. See {@link Dxt1Format}.
	 */
	private Dxt1Format mDxt1Format;
	/**
	 * PVRCT Texture Compression format. See {@link PvrtcFormat}.
	 */
	private PvrtcFormat mPvrtcFormat;
	/**
	 * Bitmap compression format. Use together with {@link CompressionType}
	 */
	private int mInternalFormat;

	protected CompressedTexture() { super(); }
	
	/**
	 * Copies every property from another TextureConfig object
	 * 
	 * @param other
	 *            another TextureConfig object to copy from
	 */
	public void setFrom(CompressedTexture other)
	{
		super.setFrom(other);
		mCompressionType = other.getCompressionType();
		mPaletteFormat = other.getPaletteFormat();
		mThreeDcFormat = other.getThreeDcFormat();
		mAtcFormat = other.getAtcFormat();
		mDxt1Format = other.getDxt1Format();
		mPvrtcFormat = other.getPvrtcFormat();
		mInternalFormat = other.getInternalFormat();
	}

	/**
	 * @return the texture compression type
	 */
	public CompressionType getCompressionType() {
		return mCompressionType;
	}

	/**
	 * @param compressionType
	 *            the texture compression type
	 */
	public void setCompressionType(CompressionType compressionType) {
		this.mCompressionType = compressionType;
	}

	/**
	 * @return the texture palette format
	 */
	public PaletteFormat getPaletteFormat() {
		return mPaletteFormat;
	}

	/**
	 * @param paletteFormat
	 *            the texture palette format
	 */
	public void setPaletteFormat(PaletteFormat paletteFormat) {
		this.mPaletteFormat = paletteFormat;
		checkPaletteFormat();
	}

	/**
	 * @return the 3DC Texture Compression format. See {@link ThreeDcFormat}.
	 */
	public ThreeDcFormat getThreeDcFormat() {
		return mThreeDcFormat;
	}

	/**
	 * @param threeDcFormat
	 *            the 3DC Texture Compression format. See {@link ThreeDcFormat}.
	 */
	public void setThreeDcFormat(ThreeDcFormat mThreeDcFormat) {
		this.mThreeDcFormat = mThreeDcFormat;
	}

	/**
	 * @return the ATC Texture Compression format. See {@link AtcFormat}.
	 */
	public AtcFormat getAtcFormat() {
		return mAtcFormat;
	}

	/**
	 * @param atcFormat
	 *            ATC Texture Compression format. See {@link AtcFormat}.
	 */
	public void setAtcFormat(AtcFormat atcFormat) {
		this.mAtcFormat = atcFormat;
	}

	/**
	 * @return the DXT1 Texture Compression format. See {@link Dxt1Format}.
	 */
	public Dxt1Format getDxt1Format() {
		return mDxt1Format;
	}

	/**
	 * @param dxt1Format
	 *            the DXT1 Texture Compression format. See {@link Dxt1Format}.
	 */
	public void setDxt1Format(Dxt1Format dxt1Format) {
		this.mDxt1Format = dxt1Format;
	}

	/**
	 * @return the PVRCT Texture Compression format. See {@link PvrtcFormat}.
	 */
	public PvrtcFormat getPvrtcFormat() {
		return mPvrtcFormat;
	}

	/**
	 * @param pvrtcFormat
	 *            the PVRCT Texture Compression format. See {@link PvrtcFormat}.
	 */
	public void setPvrtcFormat(PvrtcFormat pvrtcFormat) {
		this.mPvrtcFormat = pvrtcFormat;
	}
	
	/**
	 * @return the Bitmap compression format
	 */
	public int getInternalFormat() {
		return mInternalFormat;
	}

	/**
	 * @param internalFormat
	 *            the Bitmap compression format
	 */
	public void setInternalFormat(int internalFormat) {
		this.mInternalFormat = internalFormat;
	}
	
	/**
	 * Adds and binds paletted texture. Pass in multiple buffer corresponding to different mipmap levels.
	 */
	private void checkPaletteFormat()
	{
		switch (mPaletteFormat) {
		case PALETTE4_RGB8:
			mInternalFormat = GL_PALETTE4_RGB8_OES;
			break;
		case PALETTE4_RGBA8:
			mInternalFormat = GL_PALETTE4_RGBA8_OES;
			break;
		case PALETTE4_R5_G6_B5:
			mInternalFormat = GL_PALETTE4_R5_G6_B5_OES;
			break;
		case PALETTE4_RGBA4:
			mInternalFormat = GL_PALETTE4_RGBA4_OES;
			break;
		case PALETTE4_RGB5_A1:
			mInternalFormat = GL_PALETTE4_RGB5_A1_OES;
			break;
		case PALETTE8_RGB8:
			mInternalFormat = GL_PALETTE8_RGB8_OES;
			break;
		case PALETTE8_RGBA8:
		default:
			mInternalFormat = GL_PALETTE8_RGBA8_OES;
			break;
		case PALETTE8_R5_G6_B5:
			mInternalFormat = GL_PALETTE8_R5_G6_B5_OES;
			break;
		case PALETTE8_RGBA4:
			mInternalFormat = GL_PALETTE8_RGBA4_OES;
			break;
		case PALETTE8_RGB5_A1:
			mInternalFormat = GL_PALETTE8_RGB5_A1_OES;
			break;
		}
	}

	@Override
	public ASingleTexture clone() {
		// TODO Auto-generated method stub
		return null;
	}
}
