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
package rajawali.filters;

import android.opengl.GLES20;
import rajawali.materials.AMaterial;

public class BlurFilter extends AMaterial implements IPostProcessingFilter {
	public enum Orientation {
		HORIZONTAL,
		VERTICAL
	}
	protected static final String mVShader =
			"precision mediump float;\n" +
			"uniform mat4 uMVPMatrix;\n" +

			"attribute vec4 aPosition;\n" +
			"attribute vec2 aTextureCoord;\n" +
			"attribute vec4 aColor;\n" +

			"varying vec2 vTextureCoord;\n" +

			"void main() {\n" +
			"	gl_Position = uMVPMatrix * aPosition;\n" +
			"	vTextureCoord = aTextureCoord;\n" +
			"}\n";
	
	protected static final String mFShader =
			"precision mediump float;\n" +
	
			"const float pi = 3.14159265359;\n" +
					
			"varying vec2 vTextureCoord;" +
			
			"uniform sampler2D uFrameBufferTexture;\n" +
			"uniform vec2 uTexelSize;\n" +
			
			"uniform int uOrientation;\n" +
			"uniform int uBlurAmount;\n" +
			"uniform float uBlurScale;\n" +
			"uniform float uBlurStrength;\n" +
			"uniform float uScreenHeight;\n" +
			"uniform float uScreenWidth;\n" +
			
			// Gaussian function : http://en.wikipedia.org/wiki/Gaussian_blur#Mechanics
			"float gaussian(float x, float variance) {\n" +
			"  return (1.0 / sqrt(2.0 * pi * variance)) * exp(-((x * x) / (2.0 * variance)));\n" +
			"}\n" +
			
			"void main() {\n" +
			// Calculate texel size.
			"  vec2 textureSize = vec2(uScreenWidth, uScreenHeight);\n" +
			"  vec2 texelSize = vec2(0.0, 0.0);\n" +
			// Prevent division by zero.
			"  if (uScreenWidth != 0.0) \n" +
			"    texelSize.x = 1.0 / uScreenWidth;\n" +
			"  else\n" +
			"    texelSize.x = 0.0;\n" +
			"  if (uScreenHeight != 0.0) \n" +
			"    texelSize.y = 1.0 / uScreenHeight;\n" +
			"  else\n" +
			"    texelSize.y = 0.0;\n" +
			"  float halfBlur = float(uBlurAmount) * 0.5;\n" +
			"  vec4 color = vec4(0.0);\n" +
			"  vec4 texColor = vec4(0.0);\n" +
			
			"  float std = halfBlur * 0.35;\n" +
			"  std *= std;" +
			"  float strength = 1.0 - uBlurStrength;\n" +
			
			// Limit number of blur steps to no more than 10.
			"  for (int i = 0; i < 10; ++i) {\n" +
			"    if (i >= uBlurAmount)\n" +
			"      break;\n" +
			"    float offset = float(i) - halfBlur;\n" +
			"    if (uOrientation == 0) {\n" +
			       // Horizontal gaussian blur
			"      texColor = texture2D(uFrameBufferTexture, vTextureCoord + vec2(offset * texelSize.x * uBlurScale, 0.0)) * gaussian(offset * strength, std);\n" +
			"    } else {\n" +
			       // Vertical gaussian blur
			"      texColor = texture2D(uFrameBufferTexture, vTextureCoord + vec2(0.0, offset * texelSize.y * uBlurScale)) * gaussian(offset * strength, std);\n" +
			"    }\n" +
			"    color += texColor;\n" +
			"  }\n" +
			
			"  gl_FragColor = clamp(color, 0.0, 1.0);\n" +	// Ensure the color values stay within 0.0 - 1.0 range
			"  gl_FragColor.w = 1.0;\n" + // Always opaque
			"}";
	
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
	
	public BlurFilter(float screenWidth, float screenHeight, int blurAmount, float blurScale, float blurStrength, Orientation orientation) {
		super(mVShader, mFShader, false);
		this.mScreenWidth = screenWidth;
		this.mScreenHeight = screenHeight;
		this.mBlurAmount = blurAmount;
		this.mBlurScale = blurScale;
		this.mBlurStrength = blurStrength;
		this.mOrientation = orientation;
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muBlurAmountHandle = getUniformLocation("uBlurAmount");
		muBlurScaleHandle = getUniformLocation("uBlurScale");
		muBlurStrengthHandle = getUniformLocation("uBlurStrength");
		muOrientationHandle = getUniformLocation("uOrientation");
		muScreenHeightHandle = getUniformLocation("uScreenHeight");
		muScreenWidthHandle = getUniformLocation("uScreenWidth");
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform1i(muBlurAmountHandle, mBlurAmount);
		GLES20.glUniform1f(muBlurScaleHandle, mBlurScale);
		GLES20.glUniform1f(muBlurStrengthHandle, mBlurStrength);
		switch (mOrientation) {
			case HORIZONTAL:
				GLES20.glUniform1f(muOrientationHandle, 0);
				break;
			case VERTICAL:
				GLES20.glUniform1f(muOrientationHandle, 1);
				break;
		}
		
		GLES20.glUniform1f(muScreenHeightHandle, mScreenHeight);
		GLES20.glUniform1f(muScreenWidthHandle, mScreenWidth);
	}
	
	public boolean usesDepthBuffer() {
		return false;
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
