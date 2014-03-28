package rajawali.postprocessing.passes;

import rajawali.framework.R;

/*
 * BlurFilter initially authored by Andrew Jo (andrewjo@gmail.com)
 * 
 * Two-pass blur can be achieved by adding this filter twice, once
 * as horizontal blur and again as vertical blur.
 * 
 * Portions of fragment shader referenced from Devmaster article 
 * (http://devmaster.net/posts/3100/shader-effects-glow-and-bloom)
 * edited for simplicity.
 */
public class BlurPass extends EffectPass {
	public enum Orientation {
		HORIZONTAL,
		VERTICAL
	};
	
	protected int muBlurAmountHandle;
	protected int muBlurScaleHandle;
	protected int muBlurStrengthHandle;
	protected int muOrientationHandle;
	protected int muScreenHeightHandle;
	protected int muScreenWidthHandle;

	protected int mBlurAmount;
	protected float mBlurScale;
	protected float mBlurStrength;
	protected float mScreenHeight;
	protected float mScreenWidth;
	protected Orientation mOrientation;
	
	public BlurPass(float screenWidth, float screenHeight, int blurAmount, float blurScale, float blurStrength, Orientation orientation) {
		super();
		this.mScreenWidth = screenWidth;
		this.mScreenHeight = screenHeight;
		this.mBlurAmount = blurAmount;
		this.mBlurScale = blurScale;
		this.mBlurStrength = blurStrength;
		this.mOrientation = orientation;
		createMaterial(R.raw.minimal_vertex_shader, R.raw.blur_fragment_shader);
	}
	
	public void setShaderParams()
	{
		super.setShaderParams();
		mFragmentShader.setUniform1i("uBlurAmount", mBlurAmount);
		mFragmentShader.setUniform1f("uBlurScale", mBlurScale);
		mFragmentShader.setUniform1f("uBlurStrength", mBlurStrength);
		switch (mOrientation) {
			case HORIZONTAL:
				mFragmentShader.setUniform1i("uOrientation", 0);
				break;
			case VERTICAL:
				mFragmentShader.setUniform1i("uOrientation", 1);
				break;
		}
		mFragmentShader.setUniform1f("uScreenHeight", mScreenHeight);
		mFragmentShader.setUniform1f("uScreenWidth", mScreenHeight);
	}

	
	public int getBlurAmount() {
		return this.mBlurAmount;
	}

	public float getBlurScale() {
		return this.mBlurScale;
	}

	public float getBlurStrength() {
		return this.mBlurStrength;
	}

	public float getScreenWidth() {
		return this.mScreenWidth;
	}

	public float getScreenHeight() {
		return this.mScreenHeight;
	}

	public void setBlurAmount(int blurAmount) {
		this.mBlurAmount = blurAmount;
	}

	public void setBlurScale(float blurScale) {
		this.mBlurScale = blurScale;
	}

	public void setBlurStrength(float blurStrength) {
		this.mBlurStrength = blurStrength;
	}

	public void setScreenHeight(float screenHeight) {
		this.mScreenHeight = screenHeight;
	}

	public void setScreenWidth(float screenWidth) {
		this.mScreenWidth = screenWidth;
	}
}
