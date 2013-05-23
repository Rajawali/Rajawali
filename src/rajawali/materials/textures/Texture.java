package rajawali.materials.textures;

import android.graphics.Bitmap;


public class Texture extends ASingleTexture {
	public Texture(Texture other)
	{
		super(other);
	}
	
	public Texture(String textureName)
	{
		super(TextureType.DIFFUSE, textureName);
	}
	
	public Texture(int resourceId)
	{
		super(TextureType.DIFFUSE, resourceId);
	}
	
	public Texture(String textureName, Bitmap bitmap)
	{
		super(TextureType.DIFFUSE, textureName, bitmap);
	}
	
	public Texture(String textureName, TextureAtlas atlas)
	{
		super(TextureType.DIFFUSE, textureName, atlas.getTileNamed("boat_int_prp_tex").getPage());
	}
	public Texture(String textureName, ACompressedTexture compressedTexture)
	{
		super(TextureType.DIFFUSE, textureName, compressedTexture);
	}
	
	public Texture clone() {
		return new Texture(this);
	}
}
