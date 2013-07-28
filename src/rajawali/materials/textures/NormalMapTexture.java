package rajawali.materials.textures;

import android.graphics.Bitmap;


public class NormalMapTexture extends ASingleTexture {
	public NormalMapTexture(NormalMapTexture other)
	{
		super(other);
	}
	
	public NormalMapTexture(String textureName)
	{
		super(TextureType.NORMAL, textureName);
	}
	
	public NormalMapTexture(int resourceId)
	{
		super(TextureType.NORMAL, resourceId);
	}
	
	public NormalMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.NORMAL, textureName, bitmap);
	}
	
	public NormalMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.NORMAL, textureName, compressedTexture);
	}
	
	public NormalMapTexture clone() {
		return new NormalMapTexture(this);
	}
}
