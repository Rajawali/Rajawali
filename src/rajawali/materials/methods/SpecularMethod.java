package rajawali.materials.methods;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.AShaderBase.DataType;
import rajawali.materials.shaders.AShaderBase.IGlobalShaderVar;
import rajawali.materials.shaders.fragments.specular.PhongFragmentShaderFragment;
import rajawali.materials.textures.ATexture;
import android.graphics.Color;


public abstract class SpecularMethod {
	public static enum SpecularShaderVar implements IGlobalShaderVar {
		U_SPECULAR_COLOR("uSpecularColor", DataType.VEC3),
		U_SHININESS("uShininess", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		SpecularShaderVar(String varString, DataType dataType) {
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
	
	public static final class Phong implements ISpecularMethod
	{
		private int mSpecularColor;
		private float mShininess;
		private List<ALight> mLights;
		private List<ATexture> mTextures;
		private PhongFragmentShaderFragment mFragmentShader;
		
		public Phong()
		{
			this(Color.WHITE, 96);
		}
		
		public Phong(int specularColor)
		{
			this(specularColor, 96);
		}
		
		public Phong(int specularColor, float shininess)
		{
			mSpecularColor = specularColor;
			mShininess = shininess;
		}
		
		public IShaderFragment getVertexShaderFragment()
		{
			return null;
		}
		
		public IShaderFragment getFragmentShaderFragment()
		{
			if(mFragmentShader == null)
				mFragmentShader = new PhongFragmentShaderFragment(mLights, mSpecularColor, mShininess, mTextures);
			return mFragmentShader;
		}
		
		public void setLights(List<ALight> lights)
		{
			mLights = lights;
		}
		
		public void setSpecularColor(int specularColor)
		{
			mSpecularColor = specularColor;
			if(mFragmentShader != null)
				mFragmentShader.setSpecularColor(specularColor);
		}
		
		public int getSpecularColor()
		{
			return mSpecularColor;
		}
		
		public void setShininess(float shininess)
		{
			mShininess = shininess;
			if(mFragmentShader != null)
				mFragmentShader.setShininess(shininess);
		}
		
		public float getShininess()
		{
			return mShininess;
		}

		public void setTextures(List<ATexture> textures) {
			mTextures = textures;
		}
	}
}
