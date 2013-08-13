package rajawali.materials.methods;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.diffuse.LambertFragmentShaderFragment;
import rajawali.materials.shaders.fragments.diffuse.LambertVertexShaderFragment;


public class DiffuseMethod {
	
	public static class Lambert implements IDiffuseMethod
	{
		private float mIntensity;
		private List<ALight> mLights;
		
		public Lambert()
		{
			this(0.8f);
		}
		
		public Lambert(float intensity)
		{
			mIntensity = intensity;
		}
		
		public float getIntensity()
		{
			return mIntensity;
		}
		
		public void setIntensity(float intensity)
		{
			mIntensity = intensity;
		}
		
		public IShaderFragment getVertexShaderFragment()
		{
			return new LambertVertexShaderFragment();
		}
		
		public IShaderFragment getFragmentShaderFragment()
		{
			return new LambertFragmentShaderFragment(mLights);
		}
		
		public void setLights(List<ALight> lights)
		{
			mLights = lights;
		}
	}
}
