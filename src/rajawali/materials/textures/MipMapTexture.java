package rajawali.materials.textures;

import java.io.InputStream;
import java.nio.ByteBuffer;

import rajawali.materials.textures.ACompressedTexture.CompressionType;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.ATexture.TextureType;
import rajawali.renderer.RajawaliRenderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.ETC1;
import android.opengl.GLES20;
import android.opengl.GLUtils;


public class MipMapTexture extends AMultiTexture {
	

	public MipMapTexture(TextureType textureType, String textureName, int[] resourceIds)
	{
		super(textureType, textureName);
		setBitmapFormat(GLES20.GL_RGBA);
		setResourceIds(resourceIds);
	}


	public MipMapTexture(TextureType textureType, String textureName, ByteBuffer[] byteBuffers)
	{
		super(textureType, textureName);
		setBitmapFormat(GLES20.GL_RGBA);
		setByteBuffers(byteBuffers);
	}


	public MipMapTexture(MipMapTexture other) {
		super();
		setBitmapFormat(GLES20.GL_RGBA);
		setFrom(other);
	}
	
	
	@Override
	public ATexture clone() {
		return new MipMapTexture(this);
	}

	
	@Override
	void add() throws TextureException {
		
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		if (textureId > 0)
		{
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

			if (isMipmap())
			{
				if (mFilterType == FilterType.LINEAR)
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
							GLES20.GL_LINEAR_MIPMAP_LINEAR);
				else
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
							GLES20.GL_NEAREST_MIPMAP_NEAREST);
			} else {
				if (mFilterType == FilterType.LINEAR)
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				else
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			}
			
			if (mFilterType == FilterType.LINEAR)
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			else
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			if (mWrapType == WrapType.REPEAT) {
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			} else {
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			}

			if(mBitmaps != null && mBitmaps.length > 0) {
				for (int i = 0; i < mBitmaps.length; i++) {
					GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, i, mBitmapFormat, mBitmaps[i], 0);
				}				
			} else {			
				if (mByteBuffers == null || mByteBuffers.length == 0) {
					GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmapFormat, mWidth, mHeight, 0, 0, GLES20.GL_UNSIGNED_BYTE, null);
				} else {
					int w = mWidth, h = mHeight;
					for (int i = 0; i < mByteBuffers.length; i++) {	
							GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, i, mBitmapFormat,  w, h, 0, mBitmapFormat, GLES20.GL_UNSIGNED_BYTE, mByteBuffers[i]);
							w = w > 1 ? w / 2 : 1;
							h = h > 1 ? h / 2 : 1;							
							mByteBuffers[i].limit(0);
					}					
				}
			}
			setTextureId(textureId);
		} else {
			throw new TextureException("Couldn't generate a texture name.");
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

	}

	
	void remove() throws TextureException
	{
		GLES20.glDeleteTextures(1, new int[] { mTextureId }, 0);
	}

	void replace() throws TextureException
	{
		if(mBitmaps == null || mBitmaps.length == 0){
			if (mByteBuffers == null || mByteBuffers.length == 0)
				throw new TextureException("Texture could not be replaced because there is no ByteBuffer set.");
	
			if (mWidth == 0 || mHeight == 0 || mBitmapFormat == 0)
				throw new TextureException(
						"Could not update ByteBuffer texture. One or more of the following properties haven't been set: width, height or bitmap format");
	
			int w = mWidth, h = mHeight;
			for (int i = 0; i < mByteBuffers.length; i++) {
				GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, i, mBitmapFormat,  w, h, 0, mBitmapFormat, GLES20.GL_UNSIGNED_BYTE, mByteBuffers[i]);
				w = w > 1 ? w / 2 : 1;
				h = h > 1 ? h / 2 : 1;
			}
		} else {
			
		}
	}
	
}
