package rajawali.materials.textures;

import android.opengl.GLES20;

public class FrameBufferTexture extends ATexture {
	public FrameBufferTexture(FrameBufferTexture other)
	{
		super(other);
	}
	
	public FrameBufferTexture(String textureName)
	{
		super(TextureType.FRAME_BUFFER, textureName);
	}
	
	@Override
	public FrameBufferTexture clone() {
		return new FrameBufferTexture(this);
	}
	
	public void setFrom(FrameBufferTexture other)
	{
		super.setFrom(other);
	}	

	@Override
	void add() throws TextureException {
		if(mWidth == 0 || mHeight == 0)
			throw new TextureException("FrameBufferTexture could not be added because the width and/or height weren't specified.");
		
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		if(textureId > 0) {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
			setTextureId(textureId);
		}
	}

	@Override
	void remove() throws TextureException {
		GLES20.glDeleteTextures(1, new int[] { mTextureId }, 0);
	}

	@Override
	void replace() throws TextureException {
		return;
	}
	
	void reset() throws TextureException
	{
		return;
	}
}
