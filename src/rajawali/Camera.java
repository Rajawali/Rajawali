package rajawali;

import rajawali.bounds.IBoundingVolume;
import rajawali.math.MathUtil;
import rajawali.math.Quaternion;
import rajawali.math.Vector3;
import rajawali.math.Vector3.Axis;
import rajawali.renderer.AFrameTask;
import android.opengl.Matrix;

public class Camera extends ATransformable3D {
	
	protected final Object mFrustumLock = new Object();
	
	/**
	 * The following members are all guarded by {@link #mFrustumLock}
	 */
	protected float[] mVMatrix = new float[16];
	protected float[] mInvVMatrix = new float[16];
	protected float[] mRotationMatrix = new float[16];
	protected float[] mProjMatrix = new float[16];
	protected float mNearPlane = 1.0f;
	protected float mFarPlane = 120.0f;
	protected float mFieldOfView = 45;
	protected int mLastWidth;
	protected int mLastHeight;
	protected Vector3 mUpAxis;
	protected boolean mUseRotationMatrix = false;
	protected float[] mRotateMatrixTmp = new float[16];
	protected float[] mTmpMatrix = new float[16];
	protected float[] mCombinedMatrix=new float[16];
	public Frustum mFrustum;
	
	// Camera's localized vectors
	protected Vector3 mRightVector;
	protected Vector3 mUpVector;
	protected Vector3 mLookVector;
	protected Quaternion mLocalOrientation;
	/**
	 * End guarded members
	 */
		
	protected int mFogColor = 0xdddddd;
	protected float mFogNear = 5;
	protected float mFogFar = 25;
	protected boolean mFogEnabled = false;
	
	public Camera() {
		super();
		mLocalOrientation = Quaternion.getIdentity();
		mUpAxis = new Vector3(0, 1, 0);
		mIsCamera = true;
		mFrustum = new Frustum();
	}

	public float[] getViewMatrix() {
		synchronized (mFrustumLock) {
			if (mLookAt != null) {
				Matrix.setLookAtM(mVMatrix, 0, mPosition.x, mPosition.y,
						mPosition.z, mLookAt.x, mLookAt.y, mLookAt.z, mUpAxis.x, mUpAxis.y,
						mUpAxis.z);

				mLocalOrientation.fromEuler(mRotation.y, mRotation.z, mRotation.x);
				mLocalOrientation.toRotationMatrix(mRotationMatrix);
				Matrix.multiplyMM(mVMatrix, 0, mRotationMatrix, 0, mVMatrix, 0);
			} else {
				if (mUseRotationMatrix == false && mRotationDirty) {
					setOrientation();
					mRotationDirty = false;
				}
				if(mUseRotationMatrix == false)
					mOrientation.toRotationMatrix(mRotationMatrix);
				Matrix.setIdentityM(mTmpMatrix, 0);
				Matrix.setIdentityM(mVMatrix, 0);
				Matrix.translateM(mTmpMatrix, 0, -mPosition.x, -mPosition.y, -mPosition.z);
				Matrix.multiplyMM(mVMatrix, 0, mRotationMatrix, 0, mTmpMatrix, 0);
			}
			return mVMatrix;
		}
	}
	
	public void updateFrustum(float[] pMatrix,float[] vMatrix) {
		synchronized (mFrustumLock) {
			Matrix.multiplyMM(mCombinedMatrix, 0, pMatrix, 0, vMatrix, 0);
			Matrix.invertM(mTmpMatrix, 0, mCombinedMatrix, 0);
			mFrustum.update(mCombinedMatrix);
		}
	}

	protected void rotateM(float[] m, int mOffset, float a, float x, float y,
			float z) {
		synchronized (mFrustumLock) {
			Matrix.setIdentityM(mRotateMatrixTmp, 0);
			Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
			System.arraycopy(m, 0, mTmpMatrix, 0, 16);
			Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
		}
	}

	public void setRotationMatrix(float[] m) {
		synchronized (mFrustumLock) {
			mRotationMatrix = m;
		}
	}
	
	public float[] getRotationMatrix()
	{
		synchronized (mFrustumLock) {
			return mRotationMatrix;
		}
	}

	public void setProjectionMatrix(int width, int height) {
		synchronized (mFrustumLock) {
			mLastWidth = width;
			mLastHeight = height;
			float ratio = (float) width / height;
			float frustumH = MathUtil.tan(getFieldOfView() / 360.0f * MathUtil.PI)
					* getNearPlane();
			float frustumW = frustumH * ratio;

			Matrix.frustumM(mProjMatrix, 0, -frustumW, frustumW, -frustumH,
					frustumH, getNearPlane(), getFarPlane());
		}
	}
	
	public void setProjectionMatrix(float fieldOfView, int width, int height)
	{
		synchronized (mFrustumLock) {
			mFieldOfView = fieldOfView;
			setProjectionMatrix(width, height);
		}		
	}

    public void setUpAxis(float x, float y, float z) {
    	synchronized (mFrustumLock) {
    		mUpAxis.setAll(x, y, z);
    	}
    }
    
    public void setUpAxis(Vector3 upAxis) {
    	synchronized (mFrustumLock) {
    		mUpAxis.setAllFrom(upAxis);
    	}
    }
    
    public void setUpAxis(Axis upAxis) {
    	synchronized (mFrustumLock) {
    		if(upAxis == Axis.X)
    			mUpAxis.setAll(1, 0, 0);
    		else if(upAxis == Axis.Y)
    			mUpAxis.setAll(0, 1, 0);
    		else
    			mUpAxis.setAll(0, 0, 1);
    	}
    }
    
	public float[] getProjectionMatrix() {
		synchronized (mFrustumLock) {
			return mProjMatrix;
		}
	}

	public float getNearPlane() {
		synchronized (mFrustumLock) {
			return mNearPlane;
		}
	}

	public void setNearPlane(float nearPlane) {
		synchronized (mFrustumLock) {
			mNearPlane = nearPlane;
			setProjectionMatrix(mLastWidth, mLastHeight);
		}
	}

	public float getFarPlane() {
		synchronized (mFrustumLock) {
			return mFarPlane;
		}
	}

	public void setFarPlane(float farPlane) {
		synchronized (mFrustumLock) {
			mFarPlane = farPlane;
			setProjectionMatrix(mLastWidth, mLastHeight);
		}
	}

	public float getFieldOfView() {
		synchronized (mFrustumLock) {
			return mFieldOfView;
		}
	}

	public void setFieldOfView(float fieldOfView) {
		synchronized (mFrustumLock) {
			mFieldOfView = fieldOfView;
			setProjectionMatrix(mLastWidth, mLastHeight);
		}
	}

	public boolean getUseRotationMatrix() {
		synchronized (mFrustumLock) {
			return mUseRotationMatrix;
		}
	}

	public void setUseRotationMatrix(boolean useRotationMatrix) {
		synchronized (mFrustumLock) {
			mUseRotationMatrix = useRotationMatrix;
		}
	}

	public int getFogColor() {
		return mFogColor;
	}

	public void setFogColor(int fogColor) {
		mFogColor = fogColor;
	}

	public float getFogNear() {
		return mFogNear;
	}

	public void setFogNear(float fogNear) {
		mFogNear = fogNear;
	}

	public float getFogFar() {
		return mFogFar;
	}

	public void setFogFar(float fogFar) {
		mFogFar = fogFar;
	}

	public boolean isFogEnabled() {
		return mFogEnabled;
	}

	public void setFogEnabled(boolean fogEnabled) {
		mFogEnabled = fogEnabled;
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.ATransformable3D#getTransformedBoundingVolume()
	 */
	@Override
	public IBoundingVolume getTransformedBoundingVolume() {
		synchronized (mFrustumLock) {
			return mFrustum.getBoundingBox();
		}
	}
	
	@Override
	public TYPE getFrameTaskType() {
		return AFrameTask.TYPE.CAMERA;
	}
}
