package rajawali.filters;

import rajawali.materials.AMaterial;
import android.opengl.GLES20;

public class SwirlFilter extends AMaterial implements IPostProcessingFilter {
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
	
	/**
	 * From http://www.geeks3d.com/20110428/shader-library-swirl-post-processing-filter-in-glsl/
	 */
	protected static final String mFShader = 
			"precision mediump float;" +
			"varying vec2 vTextureCoord;" +
			
			"uniform sampler2D uFrameBufferTexture;" +
			
			"uniform float uTime;" +
			"uniform float uScreenWidth;" +
			"uniform float uScreenHeight;" +
			"uniform float uRadius;" +
			"uniform float uAngle;" +
			"uniform vec2 uCenter;" +
			
			"vec4 PostFX(sampler2D tex, vec2 uv, float time) {" +
			"	vec2 texSize = vec2(uScreenWidth, uScreenHeight);" +
			"	vec2 tc = uv * texSize;" +
			"	tc -= uCenter;" +
			"	float dist = length(tc);" +
			"	if(dist < uRadius) {" +
			"		float percent = (uRadius-dist) / uRadius;" +
			"		float theta = percent * percent * uAngle * 8.0 * uTime;" +
			"		float s = sin(theta);" +
			"		float c = cos(theta);" +
			"		tc = vec2(dot(tc, vec2(c, -s)), dot(tc, vec2(s, c)));" +
			"	}" +
			"	tc += uCenter;" +
			"	vec3 color = texture2D(uFrameBufferTexture, tc / texSize).rgb;" +
			"	return vec4(color, 1.0);" +
			"}" +
			
			"void main() {\n" +
			" 	vec2 uv = vTextureCoord.st;" +
			"	gl_FragColor = PostFX(uFrameBufferTexture, uv, uTime);" +
			"}";
	
	protected int muTimeHandle;
	protected int muScreenWidthHandle;
	protected int muScreenHeightHandle;
	protected int muRadiusHandle;
	protected int muAngleHandle;
	protected int muCenterHandle;
	
	protected float mTime;
	protected float mScreenWidth;
	protected float mScreenHeight;
	protected float mRadius;
	protected float mAngle;
	protected float[] mCenter;
			
	public SwirlFilter(float screenWidth, float screenHeight, float radius, float angle) {
		super(mVShader, mFShader, false);
		mCenter = new float[] { screenWidth * .5f, screenHeight * .5f };
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
		mRadius = radius;
		mAngle = angle;
		mTime = 1;
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public boolean usesDepthBuffer() {
		return false;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform1f(muTimeHandle, mTime);
		GLES20.glUniform1f(muScreenWidthHandle, mScreenWidth);
		GLES20.glUniform1f(muScreenHeightHandle, mScreenHeight);
		GLES20.glUniform1f(muRadiusHandle, mRadius);
		GLES20.glUniform1f(muAngleHandle, mAngle);
		GLES20.glUniform2fv(muCenterHandle, 1, mCenter, 0);
	}
	
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muTimeHandle = getUniformLocation("uTime");
		muScreenWidthHandle = getUniformLocation("uScreenWidth");
		muScreenHeightHandle = getUniformLocation("uScreenHeight");
		muRadiusHandle = getUniformLocation("uRadius");
		muAngleHandle = getUniformLocation("uAngle");
		muCenterHandle = getUniformLocation("uCenter");
	}

	public float getTime() {
		return mTime;
	}

	public void setTime(float time) {
		this.mTime = time;
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

	public float getRadius() {
		return mRadius;
	}

	public void setRadius(float radius) {
		this.mRadius = radius;
	}

	public float getAngle() {
		return mAngle;
	}

	public void setAngle(float angle) {
		this.mAngle = angle;
	}

	public float[] getCenter() {
		return mCenter;
	}

	public void setCenter(float x, float y) {
		this.mCenter[0] = x;
		this.mCenter[1] = y;
	}
}
