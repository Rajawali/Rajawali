package rajawali.filters;

import android.opengl.GLES20;
import rajawali.materials.AMaterial;

public class CrosshatchFilter extends AMaterial implements IPostProcessingFilter {
	protected static final String mVShader =
		"uniform mat4 uMVPMatrix;\n" +

		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +

		"varying vec2 vTextureCoord;\n" +

		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"}\n";
	
	/**
	 * From http://machinesdontcare.wordpress.com/2011/02/02/glsl-crosshatch/
	 */
	protected static final String mFShader = 
			"precision mediump float;" +
			"varying vec2 vTextureCoord;" +
			
			"uniform sampler2D uFrameBufferTexture;" +
			"uniform float uScreenWidth;" +
			"uniform float uScreenHeight;" +

			"void main() {\n" +
			"	float lum = length(texture2D(uFrameBufferTexture, vTextureCoord).rgb);" +
			"	vec2 fragCoord = vec2(vTextureCoord.s * uScreenWidth, vTextureCoord.t * uScreenHeight);" +
			"	gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);" +
			"	if(lum<1.0) {" +
			"		if(mod(fragCoord.x + fragCoord.y, 10.0) == 0.0) {" +
			"			gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
			"		}" +
			"	}" +
			"	if(lum<0.7) {" +
			"		if(mod(fragCoord.x - fragCoord.y, 10.0) == 0.0) {" +
			"			gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
			"		}" +
			"	}" +
			"	if(lum<0.50) {" +
			"		if(mod(fragCoord.x + fragCoord.y - 5.0, 10.0) == 0.0) {" +
			"			gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
			"		}" +
			"	}" +
			"	if(lum<0.3) {" +
			"		if(mod(fragCoord.x - fragCoord.y - 5.0, 10.0) == 0.0) {" +
			"			gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
			"		}" +
			"	}" +
			"}";
			
	protected int muScreenWidthHandle;
	protected int muScreenHeightHandle;

	protected float mScreenWidth;
	protected float mScreenHeight;
	
	public CrosshatchFilter() {
		super(mVShader, mFShader, false);
	}
	
	public boolean usesDepthBuffer() {
		return false;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform1f(muScreenWidthHandle, mScreenWidth);
		GLES20.glUniform1f(muScreenHeightHandle, mScreenHeight);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muScreenWidthHandle = getUniformLocation("uScreenWidth");
		muScreenHeightHandle = getUniformLocation("uScreenHeight");
	}
	
	public float getScreenWidth() {
		return mScreenWidth;
	}

	public void setScreenWidth(float screenWidth) {
		this.mScreenWidth = screenWidth;
	}

	public float getScreenHeight() {
		return mScreenHeight;
	}

	public void setScreenHeight(float screenHeight) {
		this.mScreenHeight = screenHeight;
	}
}
