package rajawali.materials;

import java.nio.FloatBuffer;

import rajawali.math.Number3D;
import android.opengl.GLES20;


public class ParticleMaterial extends AParticleMaterial {
	protected static final String mVShader = 
		"precision mediump float;\n" +
		"uniform mat4 uMVPMatrix;\n" +
		"uniform float uPointSize;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform vec3 uCamPos;\n" +
		"uniform vec3 uDistanceAtt;\n" +
		"uniform vec3 uFriction;\n" +
		"uniform float uTime;\n" +
		"uniform bool uMultiParticlesEnabled;\n" +
		
		"#ifdef ANIMATED\n" +
		"uniform float uCurrentFrame;\n" +
		"uniform float uTileSize;\n" +
		"uniform float uNumTileRows;\n" +
		"attribute float aAnimOffset;\n" +
		"#endif\n" +
		
		"attribute vec4 aPosition;\n" +		
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec3 aVelocity;\n" +
		
		"varying vec2 vTextureCoord;\n" +

		"void main() {\n" +
		"	vec4 position = vec4(aPosition);\n" +
		"	if(uMultiParticlesEnabled){" +
		"		position.x += aVelocity.x * uFriction.x * uTime;\n" +
		"		position.y += aVelocity.y * uFriction.y * uTime;\n" +
		"		position.z += aVelocity.z * uFriction.z * uTime; }" +
		"	gl_Position = uMVPMatrix * position;\n" +
		"	vec3 cp = vec3(uCamPos);\n" +
		"	cp.x *= -1.0;\n" +
		"	float pdist = length(cp - position.xyz);\n" +
		"	gl_PointSize = uPointSize / sqrt(uDistanceAtt.x + uDistanceAtt.y * pdist + uDistanceAtt.z * pdist * pdist);\n" +
		"	#ifdef ANIMATED\n" +
		"		vTextureCoord.s = mod(uCurrentFrame + aAnimOffset, uNumTileRows) * uTileSize;" +
		"		vTextureCoord.t = uTileSize * floor((uCurrentFrame + aAnimOffset ) / uNumTileRows);\n" +
		"	#else\n" +
		"		vTextureCoord = aTextureCoord;\n" +
		"	#endif\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D uDiffuseTexture;\n" +
		
		"#ifdef ANIMATED\n" +
		"uniform float uTileSize;\n" +
		"uniform float uNumTileRows;\n" +
		"#endif\n" +

		"void main() {\n" +
		"	\n#ifdef ANIMATED\n" +
		"		vec2 realTexCoord = vTextureCoord + (gl_PointCoord / uNumTileRows);" +
		"		gl_FragColor = texture2D(uDiffuseTexture, realTexCoord);\n" +
		"	#else\n" +
		"		gl_FragColor = texture2D(uDiffuseTexture, gl_PointCoord);\n" +
		"	#endif\n" +
		"}\n";
	
	protected float mPointSize = 10.0f;
	
	protected int muPointSizeHandle;
	protected int muCamPosHandle;
	protected int muDistanceAttHandle;
	protected int muCurrentFrameHandle;
	protected int muTileSizeHandle;
	protected int muNumTileRowsHandle;
	protected int maVelocityHandle;
	protected int maAnimOffsetHandle;
	protected int muFrictionHandle;
	protected int muTimeHandle;
	protected int muMultiParticlesEnabledHandle;
	
	protected float[] mDistanceAtt;
	protected boolean mMultiParticlesEnabled;
	protected float[] mFriction;
	protected float[] mCamPos;
	protected float mTime;	
	protected int mCurrentFrame;
	protected float mTileSize;
	protected float mNumTileRows;
	protected boolean mIsAnimated;
	
	public ParticleMaterial() {
		this(false);
	}

	public ParticleMaterial(boolean isAnimated) {
		this(mVShader, mFShader, isAnimated);
	}

	public ParticleMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, NONE);
		mDistanceAtt = new float[] {1, 1, 1};
		mFriction = new float[3];
		mCamPos = new float[3];
		mIsAnimated = isAnimated;
		if(mIsAnimated) {
			mUntouchedVertexShader = "\n#define ANIMATED\n" + mUntouchedVertexShader;
			mUntouchedFragmentShader = "\n#define ANIMATED\n" + mUntouchedFragmentShader;
		}
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public void setPointSize(float pointSize) {
		mPointSize = pointSize;
		GLES20.glUniform1f(muPointSizeHandle, mPointSize);
	}
	
	public void setMultiParticlesEnabled(boolean enabled) {
		mMultiParticlesEnabled = enabled;
		GLES20.glUniform1i(muMultiParticlesEnabledHandle, mMultiParticlesEnabled == true ? GLES20.GL_TRUE : GLES20.GL_FALSE);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
	}
	
	public void setVelocity(final int velocityBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, velocityBufferHandle);
		GLES20.glEnableVertexAttribArray(maVelocityHandle);
		fix.android.opengl.GLES20.glVertexAttribPointer(maVelocityHandle, 3, GLES20.GL_FLOAT, false,
				0, 0);
    }
	
	public void setFriction(Number3D friction) {
		mFriction[0] = friction.x; mFriction[1] = friction.y; mFriction[2] = friction.z;
		GLES20.glUniform3fv(muFrictionHandle, 1, mFriction, 0);
	}
	
	public void setTime(float time) {
		mTime = time;
		GLES20.glUniform1f(muTimeHandle, mTime);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muPointSizeHandle = getUniformLocation("uPointSize");
		muDistanceAttHandle = getUniformLocation("uDistanceAtt");
		
		maVelocityHandle = getAttribLocation("aVelocity");
		maAnimOffsetHandle = getAttribLocation("aAnimOffset");
		muFrictionHandle = getUniformLocation("uFriction");
		muTimeHandle = getUniformLocation("uTime");
		muMultiParticlesEnabledHandle = getUniformLocation("uMultiParticlesEnabled");
		
		muCurrentFrameHandle = getUniformLocation("uCurrentFrame");
		muTileSizeHandle = getUniformLocation("uTileSize");
		muNumTileRowsHandle = getUniformLocation("uNumTileRows");
	}
	
	public void setAnimOffsets(FloatBuffer animOffsets) {
		GLES20.glEnableVertexAttribArray(maAnimOffsetHandle);
		GLES20.glVertexAttribPointer(maAnimOffsetHandle, 1, GLES20.GL_FLOAT, false, 0, animOffsets);
	}
	
	public void setCurrentFrame(int currentFrame) {
		mCurrentFrame = currentFrame;
		GLES20.glUniform1f(muCurrentFrameHandle, mCurrentFrame);
	}
	
	public void setTileSize(float tileSize) {
		mTileSize = tileSize;
		GLES20.glUniform1f(muTileSizeHandle, mTileSize);
	}
	
	public void setNumTileRows(int numTileRows) {
		mNumTileRows = numTileRows;
		GLES20.glUniform1f(muNumTileRowsHandle, mNumTileRows);
	}
	
	public void setCameraPosition(Number3D cameraPos) {
		mCamPos[0] = cameraPos.x; mCamPos[1] = cameraPos.y; mCamPos[2] = cameraPos.z;
		GLES20.glUniform3fv(muCamPosHandle, 1, mCamPos, 0);
		GLES20.glUniform3fv(muDistanceAttHandle, 1, mDistanceAtt, 0);
	}
}
