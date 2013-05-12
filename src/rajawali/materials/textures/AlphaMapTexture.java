package rajawali.materials.textures;

import android.graphics.Bitmap;

public class AlphaMapTexture extends ASingleTexture {
	public AlphaMapTexture(AlphaMapTexture other)
	{
		super(other);
	}
	
	public AlphaMapTexture(String textureName)
	{
		super(TextureType.ALPHA, textureName);
	}
	
	public AlphaMapTexture(int resourceId)
	{
		super(TextureType.ALPHA, resourceId);
	}
	
	public AlphaMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.ALPHA, textureName, bitmap);
	}
	
	public AlphaMapTexture clone() {
		return new AlphaMapTexture(this);
	}
}
