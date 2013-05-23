package rajawali.materials.textures;

import java.nio.ByteBuffer;

import android.opengl.GLES11Ext;

public class ThreeDcTexture extends ACompressedTexture {

	/**
	 * 3DC Texture compression format.
	 * 
	 */
	public enum ThreeDcFormat {
		X,
		XY
	};

	/**
	 * 3DC Texture Compression format. See {@link ThreeDcFormat}.
	 */
	protected ThreeDcFormat mThreeDcFormat;

	public ThreeDcTexture(ThreeDcTexture other)
	{
		super(other);
		setThreeDcFormat(other.getThreeDcFormat());
	}

	public ThreeDcTexture(String textureName, ByteBuffer byteBuffer, ThreeDcFormat threeDcFormat)
	{
		this(textureName, new ByteBuffer[] { byteBuffer }, threeDcFormat);
	}

	public ThreeDcTexture(String textureName, ByteBuffer[] byteBuffers, ThreeDcFormat threeDcFormat)
	{
		super(textureName, byteBuffers);
		setCompressionType(CompressionType.THREEDC);
		setThreeDcFormat(threeDcFormat);
	}

	/**
	 * Copies every property from another ThreeDcTexture object
	 * 
	 * @param other
	 *            another ThreeDcTexture object to copy from
	 */
	public void setFrom(ThreeDcTexture other)
	{
		super.setFrom(other);
		mThreeDcFormat = other.getThreeDcFormat();
	}

	public ThreeDcTexture clone() {

		return new ThreeDcTexture(this);
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
		if(mThreeDcFormat == ThreeDcFormat.X)
			mCompressionFormat = GLES11Ext.GL_3DC_X_AMD;
		else
			mCompressionFormat = GLES11Ext.GL_3DC_XY_AMD;			
	}
}
