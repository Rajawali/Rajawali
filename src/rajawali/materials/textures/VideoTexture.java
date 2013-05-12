package rajawali.materials.textures;

import android.opengl.GLES20;

public class VideoTexture extends ATexture {

	private final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

	public VideoTexture(String textureName)
	{
		super(TextureType.VIDEO_TEXTURE, textureName);
	}

	public VideoTexture(VideoTexture other)
	{
		super(other);
	}

	public VideoTexture clone() {
		return new VideoTexture(this);
	}

	void add() throws TextureException {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	}

	void remove() throws TextureException {
		GLES20.glDeleteTextures(1, new int[] { mTextureId }, 0);
	}

	void replace() throws TextureException {
		return;
	}

	void reset() throws TextureException {
		return;
	}
}
