package rajawali.filters;

import rajawali.materials.AMaterial;
import android.opengl.GLES20;

public class TouchRippleFilter extends AMaterial implements IPostProcessingFilter {
	protected static final String mVShader =
			"uniform mat4 uMVPMatrix;\n" +
	
			"attribute vec4 aPosition;\n" +
			"attribute vec2 aTextureCoord;\n" +
			"attribute vec4 aColor;\n" +
			
			"%RIPPLE_PARAMS%\n" +
			
			"uniform float uTime;\n" +
			"uniform float uDuration;\n" +
			"uniform vec2 uRatio;\n" +
			"uniform float uRippleSpeed;\n" +
			"uniform float uRippleSize;\n" +
			
			"varying vec2 vTextureCoord;\n" +
			
			"vec2 processTouch(vec2 touch, float time) {\n" +
			"	vec2 pos = vec2(1.0 - (aPosition.y + .5), 1.0 - (aPosition.x + .5));" +
			"	pos *= uRatio;\n" +
			"	touch *= uRatio;\n" +
			"	vec2 diff = normalize(touch - pos);\n" +
			"	float dist = distance(touch, pos);\n" +
			"	float strength = max(0.0, uDuration-time) * 0.01;\n" +
			"	float timedist = min(1.0, time / (dist * uDuration));\n" +
			"	vec2 displ = diff * cos(dist*uRippleSize-time*uRippleSpeed) * strength * timedist;\n" +
			"	return displ;\n" +
			"}\n" +
			
			"void main() {\n" +
			"	gl_Position = uMVPMatrix * aPosition;\n" +
			"%RIPPLE_DISPLACE%" +
			"	vTextureCoord = aTextureCoord;\n" +
			"}\n";
		
	protected static final String mFShader = 
			"precision highp float;" +
			"varying vec2 vTextureCoord;" +

			"uniform sampler2D uFrameBufferTexture;" +
			
			"%RIPPLE_PARAMS%\n" +
			
			"void main() {\n" +
			"	vec2 texCoord = vTextureCoord;\n" +
			
			"%RIPPLE_DISPLACE%" +
			
			"	gl_FragColor = texture2D(uFrameBufferTexture, texCoord);\n" +
			"}";
	
	private int[] muTouchHandles;
	
	private int[] muTouchStartHandles;
	
	private int muTimeHandle;
	private int muDurationHandle;
	private int muRatioHandle;
	private int muRippleSpeedHandle;
	private int muRippleSizeHandle;
	
	private float[][] mTouches;	
	private float[] mTouchStartTimes;
	
	private float mTime;
	private float mDuration = 6.0f;
	private float[] mRatio;
	private float mRippleSpeed = 10;
	private float mRippleSize = 42;
	private int mNumRipples;
	
	private int currentTouchIndex;
			
	public TouchRippleFilter() {
		this(3);
	}
	
	public TouchRippleFilter(int numRipples) {
		super(mVShader, mFShader, false);
		mNumRipples = numRipples;
		
		mTouches = new float[mNumRipples][2];
		mTouchStartTimes = new float[mNumRipples];
		muTouchHandles = new int[mNumRipples];
		muTouchStartHandles = new int[mNumRipples];
		
		for(int i=0; i<mNumRipples; ++i) {
			mTouchStartTimes[i] = -1000;
		}
		mRatio = new float[] { 1, 1 };
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public TouchRippleFilter(float duration, float rippleSpeed, float rippleSize) {
		this();
		mDuration = duration;
		mRippleSpeed = rippleSpeed;
		mRippleSize = rippleSize;
	}
	
	public boolean usesDepthBuffer() {
		return false;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		for(int i=0; i<mNumRipples; ++i) {
			GLES20.glUniform2fv(muTouchHandles[i], 1, mTouches[i], 0);
			GLES20.glUniform1f(muTouchStartHandles[i], mTouchStartTimes[i]);
		}
		GLES20.glUniform1f(muTimeHandle, mTime);
		GLES20.glUniform1f(muDurationHandle, mDuration);
		GLES20.glUniform2fv(muRatioHandle, 1, mRatio, 0);
		GLES20.glUniform1f(muRippleSizeHandle, mRippleSize);
		GLES20.glUniform1f(muRippleSpeedHandle, mRippleSpeed);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		StringBuffer params = new StringBuffer();
		StringBuffer vertDispl = new StringBuffer();
		StringBuffer fragDispl = new StringBuffer();
		
		for(int i=0; i<mNumRipples; ++i) {
			params.append("uniform vec2 uTouch").append(i).append(";\n");
			params.append("uniform float uTouch").append(i).append("Start;\n");
			params.append("varying vec2 vDisplace").append(i).append(";\n");
			
			vertDispl.append("vDisplace").append(i).append(" = processTouch(uTouch").append(i).append(", uTime - uTouch").append(i).append("Start);\n");
			
			fragDispl.append("texCoord += vDisplace").append(i).append(";\n");
		}
		
		super.setShaders(
				vertexShader.replace("%RIPPLE_PARAMS%", params.toString()).replace("%RIPPLE_DISPLACE%", vertDispl.toString()), 
				fragmentShader.replace("%RIPPLE_PARAMS%", params.toString()).replace("%RIPPLE_DISPLACE%", fragDispl.toString())
				);
		
		for(int i=0; i<mNumRipples; ++i) {
			muTouchHandles[i] = getUniformLocation("uTouch" + i);
			muTouchStartHandles[i] = getUniformLocation("uTouch"+i+"Start");
		}
		
		muTimeHandle = getUniformLocation("uTime");
		muDurationHandle = getUniformLocation("uDuration");
		muRatioHandle = getUniformLocation("uRatio");
		muRippleSizeHandle = getUniformLocation("uRippleSize");
		muRippleSpeedHandle = getUniformLocation("uRippleSpeed");
	}
	
	public void addTouch(float x, float y, float startTime) {
		mTouches[currentTouchIndex][0] = x;
		mTouches[currentTouchIndex][1] = y;
		mTouchStartTimes[currentTouchIndex] = startTime;
		currentTouchIndex++;
		if(currentTouchIndex == mNumRipples) currentTouchIndex = 0;
	}
	
	public void setTime(float time) {
		mTime = time;
	}

	public float getDuration() {
		return mDuration;
	}

	public void setDuration(float duration) {
		this.mDuration = duration;
	}
	
	public void setScreenSize(float width, float height) {
		if(width > height) {
			mRatio[0] = 1;
			mRatio[1] = height / width;
		} else if(height > width) {
			mRatio[0] = width / height;
			mRatio[1] =  1;
		} else {
			mRatio[0] = 1;
			mRatio[1] = 1;
		}
	}

	public float getRippleSpeed() {
		return mRippleSpeed;
	}

	public void setRippleSpeed(float rippleSpeed) {
		this.mRippleSpeed = rippleSpeed;
	}

	public float getRippleSize() {
		return mRippleSize;
	}

	public void setRippleSize(float rippleSize) {
		this.mRippleSize = rippleSize;
	}
}
