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
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	protected float[] mDirection;
	protected float mRadius;
	protected float mResolution;
	
	public BlurPass(Direction direction, float radius, float screenWidth, float screenHeight) {
		super();
		mDirection = direction == Direction.HORIZONTAL ? new float[]{1, 0} : new float[]{0, 1};
		mRadius = radius;
		mResolution = direction == Direction.HORIZONTAL ? screenWidth : screenHeight;
		createMaterial(R.raw.minimal_vertex_shader, R.raw.blur_fragment_shader);
	}
	
	public void setShaderParams()
	{
		super.setShaderParams();
		mFragmentShader.setUniform2fv("uDirection", mDirection);
		mFragmentShader.setUniform1f("uRadius", mRadius);
		mFragmentShader.setUniform1f("uResolution", mResolution);
	}
}
