package rajawali.materials.methods;

import java.util.List;

import android.graphics.Color;

import rajawali.lights.ALight;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.AShaderBase.DataType;
import rajawali.materials.shaders.AShaderBase.IGlobalShaderVar;
import rajawali.materials.shaders.fragments.diffuse.LambertFragmentShaderFragment;
import rajawali.materials.shaders.fragments.diffuse.LambertVertexShaderFragment;
import rajawali.materials.shaders.fragments.effects.ToonFragmentShaderFragment;


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
	
	public static final class Toon implements IDiffuseMethod
	{
		private float[] mToonColor0;
		private float[] mToonColor1;
		private float[] mToonColor2;
		private float[] mToonColor3;
		private List<ALight> mLights;
		private ToonFragmentShaderFragment mFragmentShader;

		public Toon()
		{
			mToonColor0 = new float[] { 1, .5f, .5f, 1 };
			mToonColor1 = new float[] { .6f, .3f, .3f, 1 };
			mToonColor2 = new float[] { .4f, .2f, .2f, 1 };
			mToonColor3 = new float[] { .2f, .1f, .1f, 1 };
		}
		
		public Toon(int toonColor0, int toonColor1, int toonColor2, int toonColor3)
		{
			this();
			setToonColors(toonColor0, toonColor1, toonColor2, toonColor3);
		}
		
		public IShaderFragment getVertexShaderFragment() {
			return null;
		}

		public IShaderFragment getFragmentShaderFragment() {
			if(mFragmentShader == null)
			{
				mFragmentShader = new ToonFragmentShaderFragment(mLights);
				mFragmentShader.setToonColors(mToonColor0, mToonColor1, mToonColor2, mToonColor3);
			}
			return mFragmentShader;
		}

		public void setLights(List<ALight> lights) {
			mLights = lights;
		}
		
		public void setToonColors(int color0, int color1, int color2, int color3) {
			mToonColor0[0] = Color.red(color0); 
			mToonColor0[1] = Color.green(color0);
			mToonColor0[2] = Color.blue(color0); 
			mToonColor0[3] = Color.alpha(color0); 

			mToonColor1[0] = Color.red(color1); 
			mToonColor1[1] = Color.green(color1);
			mToonColor1[2] = Color.blue(color1); 
			mToonColor1[3] = Color.alpha(color1); 

			mToonColor2[0] = Color.red(color2); 
			mToonColor2[1] = Color.green(color2);
			mToonColor2[2] = Color.blue(color2); 
			mToonColor2[3] = Color.alpha(color2); 
			
			mToonColor3[0] = Color.red(color3); 
			mToonColor3[1] = Color.green(color3);
			mToonColor3[2] = Color.blue(color3); 
			mToonColor3[3] = Color.alpha(color3); 
			
			if(mFragmentShader != null)
				mFragmentShader.setToonColors(mToonColor0, mToonColor1, mToonColor2, mToonColor3);
		}
	}
}
