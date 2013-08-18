package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.WrapType;
import android.opengl.GLES20;


public abstract class ATextureFragmentShaderFragment extends AShader implements IShaderFragment {
	protected List<ATexture> mTextures;
	
	protected RSampler2D[] muTextures;
	protected RFloat[] muInfluence;
	protected RVec2[] muRepeat, muOffset;
	protected int[] muTextureHandles, muInfluenceHandles, muRepeatHandles, muOffsetHandles;

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
		muRepeat = new RVec2[numTextures];
		muOffset = new RVec2[numTextures];
		muTextureHandles = new int[numTextures];
		muInfluenceHandles = new int[numTextures];
		muRepeatHandles = new int[numTextures];
		muOffsetHandles = new int[numTextures];
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			muTextures[i] = (RSampler2D) addUniform(texture.getTextureName(), DataType.SAMPLER2D);
			muInfluence[i] = (RFloat) addUniform(DefaultVar.U_INFLUENCE, texture.getTextureName());
			if(texture.getWrapType() == WrapType.REPEAT)
				muRepeat[i] = (RVec2) addUniform(DefaultVar.U_REPEAT, i);
			if(texture.offsetEnabled())
				muOffset[i] = (RVec2) addUniform(DefaultVar.U_OFFSET, i);
		}
	}

	@Override
	public void setLocations(int programHandle) {
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			muTextureHandles[i] = getUniformLocation(programHandle, texture.getTextureName());
			muInfluenceHandles[i] = getUniformLocation(programHandle, DefaultVar.U_INFLUENCE, texture.getTextureName());
			if(texture.getWrapType() == WrapType.REPEAT)
				muRepeatHandles[i] = getUniformLocation(programHandle, DefaultVar.U_REPEAT, i);
			if(texture.offsetEnabled())
				muOffsetHandles[i] = getUniformLocation(programHandle, DefaultVar.U_OFFSET, i);
		}
	}

	@Override
	public void applyParams() {
		super.applyParams();
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			GLES20.glUniform1f(muInfluenceHandles[i], texture.getInfluence());
			if(texture.getWrapType() == WrapType.REPEAT)
				GLES20.glUniform2fv(muRepeatHandles[i], 1, texture.getRepeat(), 0);
			if(texture.offsetEnabled())
				GLES20.glUniform2fv(muOffsetHandles[i], 1, texture.getOffset(), 0);
		}
	}
}
