package rajawali.materials.textures;

import android.graphics.Bitmap;


public class BumpmapTexture extends ASingleTexture {
	public BumpmapTexture(BumpmapTexture other)
	{
		super(other);
	}
	
	public BumpmapTexture(String textureName)
	{
		super(TextureType.BUMP, textureName);
	}
	
	public BumpmapTexture(int resourceId)
	{
		super(TextureType.BUMP, resourceId);
	}
	
	public BumpmapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.BUMP, textureName, bitmap);
	}
	
	public BumpmapTexture clone() {
		return new BumpmapTexture(this);
	}
}
