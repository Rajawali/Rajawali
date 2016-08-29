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
package org.rajawali3d.cameras;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

public class Camera extends ATransformable3D {

	protected final Object mFrustumLock = new Object();

	/**
	 * The following members are all guarded by {@link #mFrustumLock}
	 */
	protected final Matrix4 mViewMatrix = new Matrix4();
	protected final Matrix4 mProjMatrix = new Matrix4();
	protected final Matrix4 mScratchMatrix = new Matrix4();
	protected double mNearPlane = 1.0;
	protected double mFarPlane = 120.0;
	protected double mFieldOfView = 45.0;
	protected int mLastWidth;
	protected int mLastHeight;
	protected boolean mCameraDirty = true;
	protected Frustum mFrustum;
	protected BoundingBox mBoundingBox = new BoundingBox();
	protected Vector3[] mFrustumCorners;
	protected Quaternion mLocalOrientation;
    protected boolean mIsInitialized;
	/**
	 * End guarded members
	 */

	public Camera() {
		super();
		mLocalOrientation = Quaternion.getIdentity();
		mIsCamera = true;
		mFrustum = new Frustum();
		mFrustumCorners = new Vector3[8];
        for(int i=0; i<8; i++) {
            mFrustumCorners[i] = new Vector3();
        }
	}

    @Override
    public boolean onRecalculateModelMatrix(Matrix4 parentMatrix) {
        super.onRecalculateModelMatrix(parentMatrix);
        mMMatrix.rotate(mLocalOrientation);
        return true;
    }

    public void setCameraOrientation(Quaternion quaternion) {
        mLocalOrientation.setAll(quaternion);
    }

    public void setCameraYaw(double angle) {
        mLocalOrientation.fromEuler(angle, mLocalOrientation.getRotationX(), mLocalOrientation.getRotationZ());
    }

    public void setCameraPitch(double angle) {
        mLocalOrientation.fromEuler(mLocalOrientation.getRotationY(), angle, mLocalOrientation.getRotationZ());
    }

    public void setCameraRoll(double angle) {
        mLocalOrientation.fromEuler(mLocalOrientation.getRotationY(), mLocalOrientation.getRotationX(), angle);
    }

    public void resetCameraOrientation() {
        mLocalOrientation.identity();
    }

	public Matrix4 getViewMatrix() {
		synchronized (mFrustumLock) {
            // Create an inverted orientation. This is because the view matrix is the
            // inverse operation of a model matrix
            mTmpOrientation.setAll(mOrientation);
            mTmpOrientation.inverse();

            // Create the view matrix
            final double[] matrix = mViewMatrix.getDoubleValues();
            // Precompute these factors for speed
            final double x2 = mTmpOrientation.x * mTmpOrientation.x;
            final double y2 = mTmpOrientation.y * mTmpOrientation.y;
            final double z2 = mTmpOrientation.z * mTmpOrientation.z;
            final double xy = mTmpOrientation.x * mTmpOrientation.y;
            final double xz = mTmpOrientation.x * mTmpOrientation.z;
            final double yz = mTmpOrientation.y * mTmpOrientation.z;
            final double wx = mTmpOrientation.w * mTmpOrientation.x;
            final double wy = mTmpOrientation.w * mTmpOrientation.y;
            final double wz = mTmpOrientation.w * mTmpOrientation.z;

            matrix[Matrix4.M00] = 1.0 - 2.0 * (y2 + z2);
            matrix[Matrix4.M10] = 2.0 * (xy - wz);
            matrix[Matrix4.M20] = 2.0 * (xz + wy);
            matrix[Matrix4.M30] = 0;

            matrix[Matrix4.M01] = 2.0 * (xy + wz);
            matrix[Matrix4.M11] = 1.0 - 2.0 * (x2 + z2);
            matrix[Matrix4.M21] = 2.0 * (yz - wx);
            matrix[Matrix4.M31] = 0;

            matrix[Matrix4.M02] = 2.0 * (xz - wy);
            matrix[Matrix4.M12] = 2.0 * (yz + wx);
            matrix[Matrix4.M22] = 1.0 - 2.0 * (x2 + y2);
            matrix[Matrix4.M32] = 0;

            matrix[Matrix4.M03] = -mPosition.x * matrix[Matrix4.M00]
                + -mPosition.y * matrix[Matrix4.M01] + -mPosition.z * matrix[Matrix4.M02];
            matrix[Matrix4.M13] = -mPosition.x * matrix[Matrix4.M10]
                + -mPosition.y * matrix[Matrix4.M11] + -mPosition.z * matrix[Matrix4.M12];
            matrix[Matrix4.M23] = -mPosition.x * matrix[Matrix4.M20]
                + -mPosition.y * matrix[Matrix4.M21] + -mPosition.z * matrix[Matrix4.M22];
            matrix[Matrix4.M33] = 1;

            mTmpOrientation.setAll(mLocalOrientation).inverse();
            mViewMatrix.leftMultiply(mTmpOrientation.toRotationMatrix(mScratchMatrix));
			return mViewMatrix;
		}
	}

    public void getFrustumCorners(Vector3[] points) {
        getFrustumCorners(points, false);
    }

    public void getFrustumCorners(Vector3[] points, boolean transformed) {
        getFrustumCorners(points, transformed, false);
    }

	public void getFrustumCorners(Vector3[] points, boolean transformed, boolean inverse) {
		if(mCameraDirty) {
			double aspect = mLastWidth / (double)mLastHeight;
			double nearHeight = 2.0 * Math.tan(mFieldOfView / 2.0) * mNearPlane;
			double nearWidth = nearHeight * aspect;

			double farHeight = 2.0 * Math.tan(mFieldOfView / 2.0) * mFarPlane;
			double farWidth = farHeight * aspect;

			// near plane, top left
			mFrustumCorners[0].setAll(nearWidth / -2, nearHeight / 2, mNearPlane);
			// near plane, top right
			mFrustumCorners[1].setAll(nearWidth / 2, nearHeight / 2, mNearPlane);
			// near plane, bottom right
			mFrustumCorners[2].setAll(nearWidth / 2, nearHeight / -2, mNearPlane);
			// near plane, bottom left
			mFrustumCorners[3].setAll(nearWidth / -2, nearHeight / -2, mNearPlane);
			// far plane, top left
			mFrustumCorners[4].setAll(farWidth / -2, farHeight / 2, mFarPlane);
			// far plane, top right
			mFrustumCorners[5].setAll(farWidth / 2, farHeight / 2, mFarPlane);
			// far plane, bottom right
			mFrustumCorners[6].setAll(farWidth / 2, farHeight / -2, mFarPlane);
			// far plane, bottom left
			mFrustumCorners[7].setAll(farWidth / -2, farHeight / -2, mFarPlane);
			mCameraDirty = false;
		}

        if(transformed) {
            mMMatrix.identity();
            if(inverse)
                mMMatrix.scale(-1);
            mMMatrix.translate(mPosition).rotate(mOrientation);
        }

        for (int i = 0; i < 8; ++i) {
            points[i].setAll(mFrustumCorners[i]);
            if(transformed) {
                points[i].multiply(mMMatrix);
            }
        }
	}

	public void updateFrustum(Matrix4 invVPMatrix) {
		synchronized (mFrustumLock) {
			mFrustum.update(invVPMatrix);
		}
	}

	public Frustum getFrustum() {
        synchronized (mFrustumLock) {
            return mFrustum;
        }
	}

    public void setProjectionMatrix(Matrix4 matrix) {
        synchronized (mFrustumLock) {
            mProjMatrix.setAll(matrix);
            mIsInitialized = true;
        }
    }

	public void setProjectionMatrix(int width, int height) {
		synchronized (mFrustumLock) {
			if(mLastWidth != width || mLastHeight != height) mCameraDirty = true;
			mLastWidth = width;
			mLastHeight = height;
			double ratio = ((double) width) / ((double) height);
			mProjMatrix.setToPerspective(mNearPlane, mFarPlane, mFieldOfView, ratio);
            mIsInitialized = true;
		}
	}

	public void setProjectionMatrix(double fieldOfView, int width, int height)
	{
		synchronized (mFrustumLock) {
            mFieldOfView = fieldOfView;
            setProjectionMatrix(width, height);
        }
	}

    public void updatePerspective(double left, double right, double bottom, double top) {
        updatePerspective(left + right, bottom + top);
    }

    public void updatePerspective(double fovX, double fovY) {
        synchronized (mFrustumLock) {
            double ratio = fovX / fovY;
            mFieldOfView = fovX;
            mProjMatrix.setToPerspective(mNearPlane, mFarPlane, fovX, ratio);
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
			mCameraDirty = true;
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
			mCameraDirty = true;
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
			mCameraDirty = true;
			setProjectionMatrix(mLastWidth, mLastHeight);
		}
	}

    public boolean isInitialized() {
        synchronized (mFrustumLock) {
            return mIsInitialized;
        }
    }

	/*
	 * (non-Javadoc)
	 * @see rajawali.ATransformable3D#getTransformedBoundingVolume()
	 */
	@Override
	public IBoundingVolume getTransformedBoundingVolume() {
		synchronized (mFrustumLock) {
			// TODO create an actual bounding box
			return mBoundingBox;
		}
	}

	public Camera clone() {
		Camera cam = new Camera();
		cam.setFarPlane(mFarPlane);
		cam.setFieldOfView(mFieldOfView);
		cam.setGraphNode(mGraphNode, mInsideGraph);
		cam.setLookAt(mLookAt.clone());
		cam.setNearPlane(mNearPlane);
		cam.setOrientation(mOrientation.clone());
		cam.setPosition(mPosition.clone());
		cam.setProjectionMatrix(mLastWidth, mLastHeight);

		return cam;
	}
}
