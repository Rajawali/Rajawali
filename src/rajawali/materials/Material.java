package rajawali.materials;

import java.util.ArrayList;

import rajawali.materials.shaders.AShader;
import rajawali.materials.textures.ATexture;


public class Material {
	private AShader mVertexShader;
	private AShader mFragmentShader;
	
	private boolean mUseSingleColor;
	private boolean mUseVertexColors;
	protected ArrayList<ATexture> mTextureList;
	
	public Material()
	{
		mTextureList = new ArrayList<ATexture>();
	}
	
	public void useSingleColor(boolean value)
	{
		mUseSingleColor = value;
	}
	
	public boolean usingSingleColor()
	{
		return mUseSingleColor;
	}
	
	public void useVertexColors(boolean value)
	{
		mUseVertexColors = value;
	}
	
	public boolean usingVertexColors()
	{
		return mUseVertexColors;
	}
	
	public void createShaders()
	{
	}
}
