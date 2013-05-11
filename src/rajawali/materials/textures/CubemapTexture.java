package rajawali.materials.textures;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.opengl.GLES20;


public class CubemapTexture extends AMultiTexture {
	public final int[] CUBE_FACES = new int[] {
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	};
	
	public CubemapTexture(CubemapTexture other)
	{
		super(other);
	}
	
	public CubemapTexture(String textureName)
	{
		super(TextureType.CUBE_MAP, textureName);
	}
	
	public CubemapTexture(String textureName, int[] resourceIds)
	{
		super(TextureType.CUBE_MAP, textureName, resourceIds);
	}
	
	public CubemapTexture(String textureName, Bitmap[] bitmaps)
	{
		super(TextureType.CUBE_MAP, textureName, bitmaps);
	}
	
	public CubemapTexture(String textureName, ByteBuffer[] byteBuffers)
	{
		super(TextureType.CUBE_MAP, textureName, byteBuffers);
	}
	
	public CubemapTexture clone() {
		return new CubemapTexture(this);
	}

	@Override
	void add() throws TextureException {
		// TODO Auto-generated method stub
		
	}

	@Override
	void remove() throws TextureException {
		// TODO Auto-generated method stub
		
	}

	@Override
	void replace() throws TextureException {
		// TODO Auto-generated method stub
		
	}
}
