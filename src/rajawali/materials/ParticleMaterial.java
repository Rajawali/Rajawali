package rajawali.materials;

import java.nio.FloatBuffer;

import com.monyetmabuk.livewallpapers.photosdof.R;

import rajawali.math.Number3D;
import android.opengl.GLES20;


public class ParticleMaterial extends AParticleMaterial {
	
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
		this(RawMaterialLoader.fetch(R.raw.particle_material_vertex), RawMaterialLoader.fetch(R.raw.particle_material_fragment), isAnimated);
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
		GLES20.glUniform3fv(muCamPosHandle, 1, mCamPos, 0);
		GLES20.glUniform3fv(muDistanceAttHandle, 1, mDistanceAtt, 0);
		GLES20.glUniform3fv(muFrictionHandle, 1, mFriction, 0);
		GLES20.glUniform1f(muTimeHandle, mTime);
		GLES20.glUniform1f(muCurrentFrameHandle, mCurrentFrame);
		GLES20.glUniform1f(muTileSizeHandle, mTileSize);
		GLES20.glUniform1f(muNumTileRowsHandle, mNumTileRows);
	}
	
	public void setVelocity(final int velocityBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, velocityBufferHandle);
		GLES20.glEnableVertexAttribArray(maVelocityHandle);
		GLES20.glVertexAttribPointer(maVelocityHandle, 3, GLES20.GL_FLOAT, false,
				0, 0);
    }
	
	public void setFriction(Number3D friction) {
		mFriction[0] = friction.x; mFriction[1] = friction.y; mFriction[2] = friction.z;
	}
	
	public void setTime(float time) {
		mTime = time;
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
	}
	
	public void setTileSize(float tileSize) {
		mTileSize = tileSize;
	}
	
	public void setNumTileRows(int numTileRows) {
		mNumTileRows = numTileRows;
	}
	
	public void setCameraPosition(Number3D cameraPos) {
		mCamPos[0] = cameraPos.x; mCamPos[1] = cameraPos.y; mCamPos[2] = cameraPos.z;
	}
}
