package rajawali.materials.textures;

import android.graphics.Bitmap;

public class SphereMapTexture extends ASingleTexture {

	public SphereMapTexture(SphereMapTexture other)
	{
		super(other);
	}

	public SphereMapTexture(String textureName)
	{
		super(TextureType.SPHERE_MAP, textureName);
	}

	public SphereMapTexture(int resourceId)
	{
		super(TextureType.SPHERE_MAP, resourceId);
	}

	public SphereMapTexture(String textureName, Bitmap bitmap)
	{
		super(TextureType.SPHERE_MAP, textureName, bitmap);
	}
	
	public SphereMapTexture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.SPHERE_MAP, textureName, compressedTexture);
	}

	@Override
	public SphereMapTexture clone() {
		return new SphereMapTexture(this);
	}
}
