package rajawali.postprocessing.passes;

import android.graphics.Color;
import rajawali.framework.R;


public class FogPass extends EffectPass {
	private float mFogNear;
	private float mFogFar;
	private float[] mFogColor = new float[3];
	
	public FogPass()
	{
		this(1, 10, 0xeeeeee);
	}
	
	public FogPass(final float fogNear, final float fogFar, final int fogColor)
	{
		super();
		setFogNear(fogNear);
		setFogFar(fogFar);
		setFogColor(fogColor);
		createMaterial(R.raw.minimal_vertex_shader, R.raw.fog_fragment_shader);
	}
	
	public void setShaderParams()
	{
		super.setShaderParams();
		mFragmentShader.setUniform1f("uFogNear", mFogNear);
		mFragmentShader.setUniform1f("uFogFar", mFogFar);
		mFragmentShader.setUniform3fv("uFogColor", mFogColor);
		mMaterial.bindTextureByName(PARAM_DEPTH_TEXTURE, 1, mReadTarget.getDepthTexture());
	}
	
	public void setFogNear(final float fogNear)
	{
		mFogNear = fogNear;
	}
	
	public float getFogNear()
	{
		return mFogNear;
	}
	
	public void setFogFar(final float fogFar)
	{
		mFogFar = fogFar;
	}
	
	public float getFogFar()
	{
		return mFogFar;
	}
	
	public void setFogColor(final int fogColor)
	{
		mFogColor[0] = Color.red(fogColor) / 255.f;
		mFogColor[1] = Color.green(fogColor) / 255.f;
		mFogColor[2] = Color.blue(fogColor) / 255.f;
	}
	
	public int getFogColor()
	{
		return Color.rgb((int)(mFogColor[0] * 255.f), (int)(mFogColor[1] * 255.f), (int)(mFogColor[2] * 255.f));
	}
}
