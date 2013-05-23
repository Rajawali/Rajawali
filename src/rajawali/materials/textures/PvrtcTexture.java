package rajawali.materials.textures;

import java.nio.ByteBuffer;

public class PvrtcTexture extends ACompressedTexture {

	// PowerVR Texture compression constants
	private static final int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
	private static final int GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG = 0x8C01;
	private static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
	private static final int GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;

	public enum PvrtcFormat {
		RGB_2BPP,
		RGB_4BPP,
		RGBA_2BPP,
		RGBA_4BPP
	};

	/**
	 * PVRCT Texture Compression format. See {@link PvrtcFormat}.
	 */
	protected PvrtcFormat mPvrtcFormat;

	public PvrtcTexture(PvrtcTexture other)
	{
		super(other);
		setPvrtcFormat(other.getPvrtcFormat());
	}

	public PvrtcTexture(String textureName, ByteBuffer byteBuffer, PvrtcFormat pvrtcFormat)
	{
		this(textureName, new ByteBuffer[] { byteBuffer }, pvrtcFormat);
	}

	public PvrtcTexture(String textureName, ByteBuffer[] byteBuffers, PvrtcFormat pvrtcFormat)
	{
		super(textureName, byteBuffers);
		setCompressionType(CompressionType.PVRTC);
		setPvrtcFormat(pvrtcFormat);
	}

	/**
	 * Copies every property from another PvrtcTexture object
	 * 
	 * @param other
	 *            another PvrtcTexture object to copy from
	 */
	public void setFrom(PvrtcTexture other)
	{
		super.setFrom(other);
		mPvrtcFormat = other.getPvrtcFormat();
	}

	@Override
	public PvrtcTexture clone() {
		return new PvrtcTexture(this);
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
		switch (pvrtcFormat) {
		case RGB_2BPP:
			mCompressionFormat = GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
			break;
		case RGB_4BPP:
			mCompressionFormat = GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
			break;
		case RGBA_2BPP:
			mCompressionFormat = GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG;
			break;
		case RGBA_4BPP:
		default:
			mCompressionFormat = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
			break;
		}
	}
}
