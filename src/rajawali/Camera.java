/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali;

import rajawali.bounds.IBoundingVolume;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;
import rajawali.renderer.AFrameTask;

public class Camera extends ATransformable3D {
	
	protected final Object mFrustumLock = new Object();
	
	/**
	 * The following members are all guarded by {@link #mFrustumLock}
	 */
	protected final Matrix4 mVMatrix = new Matrix4();
	protected final Matrix4 mRotationMatrix = new Matrix4();
	protected final Matrix4 mProjMatrix = new Matrix4();
	protected double mNearPlane = 1.0;
	protected double mFarPlane = 120.0;
	protected double mFieldOfView = 45.0;
	protected int mLastWidth;
	protected int mLastHeight;
	protected final Vector3 mUpAxis;
	protected boolean mUseRotationMatrix = false;
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

	public Matrix4 getViewMatrix() {
		synchronized (mFrustumLock) {
			if (mLookAt != null) {
				mVMatrix.setToLookAt(mPosition, mLookAt, mUpAxis);
				mLocalOrientation.fromEuler(mRotation.y, mRotation.z, mRotation.x);
				mVMatrix.rotate(mLocalOrientation);
			} else {
				if (mUseRotationMatrix == false && mRotationDirty) {
					setOrientation();
					mRotationDirty = false;
				}
				if (mUseRotationMatrix == false) {
					mOrientation.toRotationMatrix(mVMatrix);
				} else {
					mVMatrix.setAll(mRotationMatrix);
				}
				mVMatrix.negTranslate(mPosition);
			}
			return mVMatrix;
		}
	}
	
	public void updateFrustum(Matrix4 invVPMatrix) {
		synchronized (mFrustumLock) {
			mFrustum.update(invVPMatrix);
		}
	}

	public void setRotationMatrix(Matrix4 m) {
		synchronized (mFrustumLock) {
			mRotationMatrix.setAll(m);
		}
	}
	
	public Matrix4 getRotationMatrix()
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
			mProjMatrix.setToPerspective(mNearPlane, mFarPlane, mFieldOfView, ratio);
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
    		mUpAxis.setAll(upAxis);
    	}
    }
    
	public Matrix4 getProjectionMatrix() {
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
