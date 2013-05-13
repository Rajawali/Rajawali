package rajawali.materials.textures;

import java.nio.ByteBuffer;

public class Dxt1Texture extends ACompressedTexture {

	// S3 texture compression constants
	private static final int GL_COMPRESSED_RGB_S3TC_DXT1_EXT = 0x83F0;
	private static final int GL_COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83F1;

	/**
	 * DXT1 Texture compression format.
	 * 
	 */
	public enum Dxt1Format {
		RGB,
		RGBA
	};

	/**
	 * DXT1 Texture Compression format. See {@link Dxt1Format}.
	 */
	protected Dxt1Format mDxt1Format;

	public Dxt1Texture(Dxt1Texture other)
	{
		super(other);
		setDxt1Format(other.getDxt1Format());
	}

	public Dxt1Texture(String textureName, ByteBuffer byteBuffer, Dxt1Format dxt1Format)
	{
		this(textureName, new ByteBuffer[] { byteBuffer }, dxt1Format);
	}

	public Dxt1Texture(String textureName, ByteBuffer[] byteBuffers, Dxt1Format dxt1Format)
	{
		super(textureName, byteBuffers);
		setCompressionType(CompressionType.DXT1);
		setDxt1Format(dxt1Format);
	}

	/**
	 * Copies every property from another Dxt1Texture object
	 * 
	 * @param other
	 *            another Dxt1Texture object to copy from
	 */
	public void setFrom(Dxt1Texture other)
	{
		super.setFrom(other);
		mDxt1Format = other.getDxt1Format();
	}

	@Override
	public Dxt1Texture clone() {
		return new Dxt1Texture(this);
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
		switch (dxt1Format) {
		case RGB:
			mCompressionFormat = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
			break;
		case RGBA:
		default:
			mCompressionFormat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
			break;
		}
	}
}
