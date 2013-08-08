package rajawali;

import rajawali.bounds.IBoundingVolume;
import rajawali.math.MathUtil;
import rajawali.math.Matrix;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;
import rajawali.renderer.AFrameTask;
import android.util.Log;

public class Camera extends ATransformable3D {
	
	protected final Object mFrustumLock = new Object();
	
	/**
	 * The following members are all guarded by {@link #mFrustumLock}
	 */
	protected double[] mVMatrix = new double[16];
	protected double[] mInvVMatrix = new double[16];
	protected double[] mRotationMatrix = new double[16];
	protected double[] mProjMatrix = new double[16];
	protected double mNearPlane = 1.0f;
	protected double mFarPlane = 120.0f;
	protected double mFieldOfView = 45;
	protected int mLastWidth;
	protected int mLastHeight;
	protected Vector3 mUpAxis;
	protected boolean mUseRotationMatrix = false;
	protected double[] mRotateMatrixTmp = new double[16];
	protected double[] mTmpMatrix = new double[16];
	protected double[] mCombinedMatrix = new double[16];
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
	protected float mFogNear = 5f;
	protected float mFogFar = 25f;
	protected boolean mFogEnabled = false;
	
	public Camera() {
		super();
		mLocalOrientation = Quaternion.getIdentity();
		mUpAxis = new Vector3(0, 1, 0);
		mIsCamera = true;
		mFrustum = new Frustum();
	}

	public double[] getViewMatrix() {
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
	
	public void updateFrustum(double[] pMatrix, double[] vMatrix) {
		synchronized (mFrustumLock) {
			Matrix.multiplyMM(mCombinedMatrix, 0, pMatrix, 0, vMatrix, 0);
			Matrix.invertM(mTmpMatrix, 0, mCombinedMatrix, 0);
			mFrustum.update(mTmpMatrix);
		}
	}

	protected void rotateM(double[] m, int mOffset, double a, double x, double y,
			double z) {
		synchronized (mFrustumLock) {
			Matrix.setIdentityM(mRotateMatrixTmp, 0);
			Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
			System.arraycopy(m, 0, mTmpMatrix, 0, 16);
			Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
		}
	}

	public void setRotationMatrix(double[] m) {
		synchronized (mFrustumLock) {
			mRotationMatrix = m;
		}
	}
	
	public double[] getRotationMatrix()
	{
		synchronized (mFrustumLock) {
			return mRotationMatrix;
		}
	}

	public void setProjectionMatrix(int width, int height) {
		synchronized (mFrustumLock) {
			mLastWidth = width;
			mLastHeight = height;
			double ratio = ((double) width) / ((double) height);
			double frustumH = MathUtil.tan(getFieldOfView() / 360.0 * MathUtil.PI)
					* getNearPlane();
			double frustumW = frustumH * ratio;

			Matrix.frustumM(mProjMatrix, 0, -frustumW, frustumW, -frustumH,
					frustumH, getNearPlane(), getFarPlane());
		}
	}
	
	public void setProjectionMatrix(double fieldOfView, int width, int height)
	{
		synchronized (mFrustumLock) {
			mFieldOfView = fieldOfView;
			setProjectionMatrix(width, height);
		}		
	}

    public void setUpAxis(double x, double y, double z) {
    	synchronized (mFrustumLock) {
    		mUpAxis.setAll(x, y, z);
    	}
    }
    
    public void setUpAxis(Vector3 upAxis) {
    	synchronized (mFrustumLock) {
    		mUpAxis.setAll(upAxis);
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
    
	public double[] getProjectionMatrix() {
		synchronized (mFrustumLock) {
			return mProjMatrix;
		}
	}

	public double getNearPlane() {
		synchronized (mFrustumLock) {
			return mNearPlane;
		}
	}

	public void setNearPlane(double nearPlane) {
		synchronized (mFrustumLock) {
			mNearPlane = nearPlane;
			setProjectionMatrix(mLastWidth, mLastHeight);
		}
	}

	public double getFarPlane() {
		synchronized (mFrustumLock) {
			return mFarPlane;
		}
	}

	public void setFarPlane(double farPlane) {
		synchronized (mFrustumLock) {
			mFarPlane = farPlane;
			setProjectionMatrix(mLastWidth, mLastHeight);
		}
	}

	public double getFieldOfView() {
		synchronized (mFrustumLock) {
			return mFieldOfView;
		}
	}

	public void setFieldOfView(double fieldOfView) {
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
