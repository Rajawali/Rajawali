package rajawali.materials.textures;

import android.graphics.Bitmap;


public class SpecularMapTexture extends ASingleTexture {
	public SpecularMapTexture(SpecularMapTexture other)
	{
		super(other);
	}
	
	public SpecularMapTexture(String textureName)
	{
		super(TextureType.SPECULAR, textureName);
	}
	
	public SpecularMapTexture(int resourceId)
	{
		super(TextureType.SPECULAR, resourceId);
	}
	
	public SpecularMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.SPECULAR, textureName, bitmap);
	}
	
	public SpecularMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.SPECULAR, textureName, compressedTexture);
	}
	
	public SpecularMapTexture clone() {
		return new SpecularMapTexture(this);
	}
}
