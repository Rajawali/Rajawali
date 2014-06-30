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

import java.nio.FloatBuffer;
import java.util.Stack;

import rajawali.bounds.BoundingBox;
import rajawali.bounds.IBoundingVolume;
import rajawali.materials.Material;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Line3D;
import rajawali.renderer.AFrameTask;
import android.graphics.Color;
import android.opengl.GLES20;

public class Camera extends ATransformable3D {
	protected final Object mFrustumLock = new Object();	
	/**
	 * The following members are all guarded by {@link #mFrustumLock}
	 */
	protected final Matrix4 mViewMatrix = new Matrix4();
	protected final Matrix4 mRotationMatrix = new Matrix4();
	protected final Matrix4 mProjMatrix = new Matrix4();
	protected double mNearPlane = 1.0;
	protected double mFarPlane = 120.0;
	protected double mFieldOfView = 45.0;
	protected int mLastWidth;
	protected int mLastHeight;
	protected boolean mUseRotationMatrix = false;
	protected boolean mCameraDirty = true;
	protected Frustum mFrustum;
	protected BoundingBox mBoundingBox = new BoundingBox();
	protected int mDebugColor = Color.RED;
	protected Line3D mVisualFrustum;
	protected Vector3[] mFrustumCorners;
	protected Vector3[] mFrustumCornersTransformed;
	protected Matrix4 mMMatrix;	
	protected Quaternion mLocalOrientation;
	/**
	 * End guarded members
	 */
		
	public Camera() {
		super();
		mLocalOrientation = Quaternion.getIdentity();
		mIsCamera = true;
		mFrustum = new Frustum();
		mMMatrix = new Matrix4();
		mFrustumCorners = new Vector3[8];
		mFrustumCornersTransformed = new Vector3[8];
		for(int i=0; i<8; i++) {
			mFrustumCorners[i] = new Vector3();
			mFrustumCornersTransformed[i] = new Vector3();
		}
	}

	public Matrix4 getViewMatrix() {
		synchronized (mFrustumLock) {
			if (mLookAt != null) {
				mViewMatrix.setToLookAt(mPosition, mLookAt, Vector3.Y);
				mLocalOrientation.fromEuler(mRotation.y, mRotation.z, mRotation.x);
				mViewMatrix.rotate(mLocalOrientation);
			} else {
				if (mUseRotationMatrix == false && mRotationDirty) {
					setOrientation();
					mRotationDirty = false;
				}
				if (mUseRotationMatrix == false) {
					mOrientation.toRotationMatrix(mViewMatrix);
				} else {
					mViewMatrix.setAll(mRotationMatrix);
				}
				mViewMatrix.negTranslate(mPosition);
			}
			return mViewMatrix;
		}
	}
	
	public void getFrustumCorners(Vector3[] points, boolean transformed) {
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
		
		for(int i=0; i<8; i++) {
			points[i].setAll(mFrustumCorners[i]);
			
			if(transformed) {
				setOrientation();
				if (mLookAt == null) {
					mOrientation.toRotationMatrix(mRotationMatrix);
				} else {
					mRotationMatrix.setAll(mLookAtMatrix);
				}
				mMMatrix.identity().translate(mPosition).multiply(mRotationMatrix);
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
		return mFrustum;
	}

	public void setRotationMatrix(Matrix4 m) {
		synchronized (mFrustumLock) {
			mRotationMatrix.setAll(m);
		}
	}
	
	public Matrix4 getRotationMatrix() {
		synchronized (mFrustumLock) {
			return mRotationMatrix;
		}
	}

	public void setProjectionMatrix(int width, int height) {
		synchronized (mFrustumLock) {
			if(mLastWidth != width || mLastHeight != height) mCameraDirty = true;
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
	
	@Override
	public TYPE getFrameTaskType() {
		return AFrameTask.TYPE.CAMERA;
	}
	
	public void drawFrustum(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix,
			final Matrix4 vMatrix, final Matrix4 mMatrix) {
		if (mVisualFrustum == null) {
			if(mLastWidth == 0) return;
			
			Stack<Vector3> points = new Stack<Vector3>();
			
			getFrustumCorners(mFrustumCornersTransformed, true);
			
			//
			// -- Near plane
			//
			
			points.push(mFrustumCornersTransformed[0]);
			points.push(mFrustumCornersTransformed[1]);
			points.push(mFrustumCornersTransformed[1]);
			points.push(mFrustumCornersTransformed[2]);
			points.push(mFrustumCornersTransformed[2]);
			points.push(mFrustumCornersTransformed[3]);
			points.push(mFrustumCornersTransformed[3]);
			points.push(mFrustumCornersTransformed[0]);

			//
			// -- Far plane
			//

			points.push(mFrustumCornersTransformed[4]);
			points.push(mFrustumCornersTransformed[5]);
			points.push(mFrustumCornersTransformed[5]);
			points.push(mFrustumCornersTransformed[6]);
			points.push(mFrustumCornersTransformed[6]);
			points.push(mFrustumCornersTransformed[7]);
			points.push(mFrustumCornersTransformed[7]);
			points.push(mFrustumCornersTransformed[4]);

			//
			// -- Near to far plane
			//
			
			points.push(mFrustumCornersTransformed[0]);
			points.push(mFrustumCornersTransformed[4]);
			points.push(mFrustumCornersTransformed[1]);
			points.push(mFrustumCornersTransformed[5]);
			points.push(mFrustumCornersTransformed[2]);
			points.push(mFrustumCornersTransformed[6]);
			points.push(mFrustumCornersTransformed[3]);
			points.push(mFrustumCornersTransformed[7]);

			mVisualFrustum = new Line3D(points, 1, mDebugColor);
			mVisualFrustum.setDrawingMode(GLES20.GL_LINES);
			mVisualFrustum.setMaterial(new Material());

			mVisualFrustum.getGeometry().changeBufferUsage(
					mVisualFrustum.getGeometry().getVertexBufferInfo(), GLES20.GL_DYNAMIC_DRAW);
		}

		getFrustumCorners(mFrustumCornersTransformed, true);
		
		FloatBuffer b = mVisualFrustum.getGeometry().getVertices();
		int index = 0;
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[0]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[1]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[1]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[2]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[2]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[3]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[3]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[0]);

		addVertexToBuffer(b, index++, mFrustumCornersTransformed[4]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[5]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[5]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[6]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[6]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[7]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[7]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[4]);
		
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[0]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[4]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[1]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[5]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[2]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[6]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[3]);
		addVertexToBuffer(b, index++, mFrustumCornersTransformed[7]);

		mVisualFrustum.getGeometry().changeBufferData(
				mVisualFrustum.getGeometry().getVertexBufferInfo(),
				mVisualFrustum.getGeometry().getVertices(), 0);

		mVisualFrustum.render(camera, vpMatrix, projMatrix, vMatrix, null);
	}

	private void addVertexToBuffer(FloatBuffer b, int index, Vector3 vertex) {
		int vertIndex = index * 3;

		b.put(vertIndex, (float) vertex.x);
		b.put(vertIndex + 1, (float) vertex.y);
		b.put(vertIndex + 2, (float) vertex.z);
	}

	public Object3D getVisual() {
		return mVisualFrustum;
	}

	public void setDebugColor(int debugColor) {
		mDebugColor = debugColor;
	}
	
	public Camera clone() {
		Camera cam = new Camera();
		cam.setDebugColor(mDebugColor);
		cam.setFarPlane(mFarPlane);
		cam.setFieldOfView(mFieldOfView);
		cam.setGraphNode(mGraphNode, mInsideGraph);
		cam.setLookAt(mLookAt.clone());
		cam.setNearPlane(mNearPlane);
		cam.setOrientation(mOrientation.clone());
		cam.setPosition(mPosition.clone());
		cam.setProjectionMatrix(mLastWidth, mLastHeight);
		cam.setUseRotationMatrix(mUseRotationMatrix);
		
		return cam;
	}
}
