package rajawali.materials.methods;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.specular.PhongFragmentShaderFragment;
import android.graphics.Color;


public abstract class SpecularMethod {
	public static final class Phong implements ISpecularMethod
	{
		private int mSpecularColor;
		private float mShininess;
		private List<ALight> mLights;
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
				mFragmentShader = new PhongFragmentShaderFragment(mLights, mSpecularColor, mShininess);
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
	}
}
