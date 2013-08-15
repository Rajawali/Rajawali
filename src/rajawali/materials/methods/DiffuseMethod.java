package rajawali.materials.methods;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.AShaderBase.DataType;
import rajawali.materials.shaders.AShaderBase.IGlobalShaderVar;
import rajawali.materials.shaders.fragments.diffuse.LambertFragmentShaderFragment;
import rajawali.materials.shaders.fragments.diffuse.LambertVertexShaderFragment;


public abstract class DiffuseMethod {
	public static enum DiffuseShaderVar implements IGlobalShaderVar {
		L_NDOTL("NdotL", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		DiffuseShaderVar(String varString, DataType dataType) {
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
	
	public static final class Lambert implements IDiffuseMethod
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
