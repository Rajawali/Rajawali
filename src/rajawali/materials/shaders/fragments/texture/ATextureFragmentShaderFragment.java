package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.ATexture;
import android.opengl.GLES20;


public abstract class ATextureFragmentShaderFragment extends AShader implements IShaderFragment {
	protected List<ATexture> mTextures;
	
	protected RSampler2D[] muTextures;
	protected RFloat[] muInfluence;
	protected int[] muTextureHandles, muInfluenceHandles;

	public ATextureFragmentShaderFragment(List<ATexture> textures)
	{
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mTextures = textures;
		initialize();
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		int numTextures = mTextures.size();
		
		muTextures = new RSampler2D[numTextures];
		muInfluence = new RFloat[numTextures];
		muTextureHandles = new int[numTextures];
		muInfluenceHandles = new int[numTextures];
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			muTextures[i] = (RSampler2D) addUniform(texture.getTextureName(), DataType.SAMPLER2D);
			muInfluence[i] = (RFloat) addUniform(DefaultVar.U_INFLUENCE, texture.getTextureName());
		}
	}

	@Override
	public void setLocations(int programHandle) {
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			muTextureHandles[i] = getUniformLocation(programHandle, texture.getTextureName());
			muInfluenceHandles[i] = getUniformLocation(programHandle, DefaultVar.U_INFLUENCE, texture.getTextureName());
		}
	}

	@Override
	public void applyParams() {
		super.applyParams();
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			GLES20.glUniform1f(muInfluenceHandles[i], texture.getInfluence());
		}
	}
}
