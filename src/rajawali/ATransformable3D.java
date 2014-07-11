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
import rajawali.math.Matrix;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.renderer.AFrameTask;
import rajawali.scenegraph.IGraphNode;
import rajawali.scenegraph.IGraphNodeMember;

public abstract class ATransformable3D extends AFrameTask implements IGraphNodeMember {
	protected Vector3 mPosition, mRotation, mScale;
	protected Quaternion mOrientation;
	protected Quaternion mTmpOrientation;
	protected Vector3 mRotationAxis;
	protected boolean mRotationDirty;
	protected Vector3 mLookAt;
	protected Vector3 mTmpAxis, mTmpVec;
	protected boolean mIsCamera, mQuatWasSet;
	protected Vector3 mTmpRotX = new Vector3();
	protected Vector3 mTmpRotY = new Vector3();
	protected Vector3 mTmpRotZ = new Vector3();
	protected double[] mLookAtMatrix = new double[16];
	
	protected IGraphNode mGraphNode;
	protected boolean mInsideGraph = false; //Default to being outside the graph
	
	public ATransformable3D() {
		mPosition = new Vector3();
		mRotation = new Vector3();
		mScale = new Vector3(1, 1, 1);
		mOrientation = new Quaternion();
		mTmpOrientation = new Quaternion();
		mTmpAxis = new Vector3();
		mTmpVec = new Vector3();
		mRotationDirty = true;
	}
	
	public void setPosition(Vector3 position) {
		mPosition.setAll(position);
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public void setPosition(double x, double y, double z) {
		mPosition.setAll(x, y, z);
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public Vector3 getPosition() {
		return mPosition;
	}
	
	public void setX(double x) {
		mPosition.x = x;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public double getX() {
		return mPosition.x;
	}

	public void setY(double y) {
		mPosition.y = y;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public double getY() {
		return mPosition.y;
	}

	public void setZ(double z) {
		mPosition.z = z;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public double getZ() {
		return mPosition.z;
	}
	

	public void setOrientation() {
		if(!mRotationDirty && mLookAt == null) return;

		mOrientation.identity();
		if(mLookAt != null) {
			mTmpRotZ.setAll(mLookAt)
				.subtract(mPosition)
				.normalize();
			
			if(mTmpRotZ.isZero()) mTmpRotZ.z = 1;
			
			mTmpRotX.setAll(mTmpRotZ)
				.cross(Vector3.Y)
				.normalize();
			
			if(mTmpRotX.isZero()) {
				mTmpRotZ.x += .0001f;
				mTmpRotX.cross(mTmpRotZ).normalize();
			}
			
			mTmpRotY.setAll(mTmpRotX);
			mTmpRotY.cross(mTmpRotZ);
			
			Matrix.setIdentityM(mLookAtMatrix, 0);
			mLookAtMatrix[Matrix4.M00] = mTmpRotX.x;
			mLookAtMatrix[Matrix4.M10] = mTmpRotX.y;
			mLookAtMatrix[Matrix4.M20] = mTmpRotX.z;
			mLookAtMatrix[Matrix4.M01] = mTmpRotY.x;
			mLookAtMatrix[Matrix4.M11] = mTmpRotY.y;
			mLookAtMatrix[Matrix4.M21] = mTmpRotY.z;
			mLookAtMatrix[Matrix4.M02] = mTmpRotZ.x;
			mLookAtMatrix[Matrix4.M12] = mTmpRotZ.y;
			mLookAtMatrix[Matrix4.M22] = mTmpRotZ.z;

			//TODO: This will be fixed by Issue #968
			mOrientation.fromRotationMatrix(mLookAtMatrix);
		} else {
			mOrientation.multiply(mTmpOrientation.fromAngleAxis(Vector3.Y, mRotation.y));
			mOrientation.multiply(mTmpOrientation.fromAngleAxis(Vector3.Z, mRotation.z));
			mOrientation.multiply(mTmpOrientation.fromAngleAxis(Vector3.X, mRotation.x));
			if(mIsCamera)
				mOrientation.inverse();
		}
		//if (mGraphNode != null) mGraphNode.updateObject(this); //TODO: This may cause problems
	}

	public void rotateAround(Vector3 axis, double angle) {
		rotateAround(axis, angle, true);
	}
	
 	public void rotateAround(Vector3 axis, double angle, boolean append) {
 		if(append) {
 			mTmpOrientation.fromAngleAxis(axis, angle);
 			mOrientation.multiply(mTmpOrientation);
 		} else {
 			mOrientation.fromAngleAxis(axis, angle);
 		}
		mRotationDirty = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	public Quaternion getOrientation(Quaternion qt) {
		setOrientation(); // Force mOrientation to be recalculated
		qt.setAll(mOrientation); 
		return  qt;
	}
	
	public void setOrientation(Quaternion quat) {
		mOrientation.setAll(quat);
		mRotationDirty = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	public void setRotation(double rotX, double rotY, double rotZ) {
		mRotation.x = rotX;
		mRotation.y = rotY;
		mRotation.z = rotZ;
		mRotationDirty = true;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	public void setRotation(double[] rotationMatrix)
	{
		//TODO: This will be fixed by issue #968
		mOrientation.fromRotationMatrix(rotationMatrix);
	}
	
	public void setRotX(double rotX) {
		mRotation.x = rotX;
		mRotationDirty = true;
	}

	public double getRotX() {
		return mRotation.x;
	}

	public void setRotY(double rotY) {
		mRotation.y = rotY;
		mRotationDirty = true;
	}

	public double getRotY() {
		return mRotation.y;
	}

	public void setRotZ(double rotZ) {
		mRotation.z = rotZ;
		mRotationDirty = true;
	}

	public double getRotZ() {
		return mRotation.z;
	}
	
	public Vector3 getRotation() {
		return mRotation;
	}

	public void setRotation(Vector3 rotation) {
		mRotation.setAll(rotation);
		mRotationDirty = true;
	}

	public void setScale(double scale) {
		mScale.x = scale;
		mScale.y = scale;
		mScale.z = scale;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public void setScale(double scaleX, double scaleY, double scaleZ) {
		mScale.x = scaleX;
		mScale.y = scaleY;
		mScale.z = scaleZ;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public void setScaleX(double scaleX) {
		mScale.x = scaleX;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public double getScaleX() {
		return mScale.x;
	}

	public void setScaleY(double scaleY) {
		mScale.y = scaleY;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public double getScaleY() {
		return mScale.y;
	}

	public void setScaleZ(double scaleZ) {
		mScale.z = scaleZ;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public double getScaleZ() {
		return mScale.z;
	}
	
	public Vector3 getScale() {
		return mScale;
	}

	public void setScale(Vector3 scale) {
		mScale = scale;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public Vector3 getLookAt() {
		return mLookAt;
	}
	
	public void setLookAt(double x, double y, double z) {
		if (mLookAt == null) mLookAt = new Vector3();
		mLookAt.x = x;
		mLookAt.y = y;
		mLookAt.z = z;
		mRotationDirty = true;
	}
	
	public void setLookAt(Vector3 lookAt) {
		if (lookAt == null) {
			mLookAt = null;
			return;
		}
		setLookAt(lookAt.x,  lookAt.y, lookAt.z);
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#setGraphNode(rajawali.scenegraph.IGraphNode)
	 */
	public void setGraphNode(IGraphNode node, boolean inside) {
		mGraphNode = node;
		mInsideGraph = inside;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#getGraphNode()
	 */
	public IGraphNode getGraphNode() {
		return mGraphNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#isInGraph()
	 */
	public boolean isInGraph() {
		return mInsideGraph;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#getTransformedBoundingVolume()
	 */
	public IBoundingVolume getTransformedBoundingVolume() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#getScenePosition()
	 */
	public Vector3 getScenePosition() {
		return mPosition;
	}
}
