package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.Texture;
import android.opengl.GLES20;


public class DiffuseTextureFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "DIFFUSE_TEXTURE_FRAGMENT";
	private List<Texture> mTextures;
	
	public static enum DiffuseTextureShaderVar implements IGlobalShaderVar {
		U_INFLUENCE("uInfluence", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		DiffuseTextureShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}

		public String getVarString() {
			return mVarString;
		}

		public DataType getDataType() {
			return mDataType;
		}
	}
	
	private RSampler2D[] muTextures;
	private RFloat[] muInfluence;
	private int[] muTextureHandles, muInfluenceHandles;
	
	public DiffuseTextureFragmentShaderFragment(List<Texture> textures)
	{
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mTextures = textures;
		initialize();
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RVec4 color = (RVec4)getGlobal(DefaultVar.G_COLOR);
		RVec2 textureCoord = (RVec2)getGlobal(DefaultVar.V_TEXTURE_COORD);
		
		for(int i=0; i<mTextures.size(); i++)
		{
			color.assign(texture2D(muTextures[i], textureCoord));
		}
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
			Texture texture = mTextures.get(i);
			muTextures[i] = (RSampler2D) addUniform(texture.getTextureName(), DataType.SAMPLER2D);
			muInfluence[i] = (RFloat) addUniform(DiffuseTextureShaderVar.U_INFLUENCE, i);
		}
	}
	
	@Override
	public void setLocations(int programHandle) {
		for(int i=0; i<mTextures.size(); i++)
		{
			Texture texture = mTextures.get(i);
			muTextureHandles[i] = getUniformLocation(programHandle, texture.getTextureName());
			muInfluenceHandles[i] = getUniformLocation(programHandle, DiffuseTextureShaderVar.U_INFLUENCE, i);
		}
	}
	
	@Override
	public void applyParams() {
		super.applyParams();
		for(int i=0; i<mTextures.size(); i++)
		{
			Texture texture = mTextures.get(i);
			GLES20.glUniform1f(muInfluenceHandles[i], texture.getInfluence());
		}
	}
}
