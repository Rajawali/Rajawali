package rajawali;

import rajawali.bounds.IBoundingVolume;
import rajawali.math.AngleAxis;
import rajawali.math.Vector3;
import rajawali.math.Vector3.Axis;
import rajawali.math.Quaternion;
import rajawali.renderer.AFrameTask;
import rajawali.scenegraph.IGraphNode;
import rajawali.scenegraph.IGraphNodeMember;
import android.opengl.Matrix;

public abstract class ATransformable3D extends AFrameTask implements IGraphNodeMember {
	protected Vector3 mPosition, mRotation, mScale;
	protected Quaternion mOrientation;
	protected Quaternion mTmpOrientation;
	protected Vector3 mRotationAxis;
	protected Vector3 mAxisX, mAxisY, mAxisZ;
	protected boolean mRotationDirty;
	protected Vector3 mLookAt;
	protected Vector3 mTmpAxis, mTmpVec;
	protected boolean mIsCamera, mQuatWasSet;
	protected AngleAxis mAngleAxis; 
	
	protected IGraphNode mGraphNode;
	protected boolean mInsideGraph = false; //Default to being outside the graph
	
	public ATransformable3D() {
		mPosition = new Vector3();
		mRotation = new Vector3();
		mScale = new Vector3(1, 1, 1);
		mOrientation = new Quaternion();
		mTmpOrientation = new Quaternion();
		mAxisX = Vector3.getAxisVector(Axis.X);
		mAxisY = Vector3.getAxisVector(Axis.Y);
		mAxisZ = Vector3.getAxisVector(Axis.Z);
		mTmpAxis = new Vector3();
		mTmpVec = new Vector3();
		mAngleAxis = new AngleAxis();
		mRotationDirty = true;
	}
	
	public void setPosition(Vector3 position) {
		mPosition.setAllFrom(position);
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public void setPosition(float x, float y, float z) {
		mPosition.setAll(x, y, z);
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public Vector3 getPosition() {
		return mPosition;
	}
	
	public void setX(float x) {
		mPosition.x = x;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public float getX() {
		return mPosition.x;
	}

	public void setY(float y) {
		mPosition.y = y;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public float getY() {
		return mPosition.y;
	}

	public void setZ(float z) {
		mPosition.z = z;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public float getZ() {
		return mPosition.z;
	}
	
	Vector3 mTmpRotX = new Vector3();
	Vector3 mTmpRotY = new Vector3();
	Vector3 mTmpRotZ = new Vector3();
	float[] mLookAtMatrix = new float[16];

	public void setOrientation() {
		if(!mRotationDirty && mLookAt == null) return;

		mOrientation.setIdentity();
		if(mLookAt != null) {			
			mTmpRotZ.setAllFrom(mLookAt);
			mTmpRotZ.subtract(mPosition);
			mTmpRotZ.normalize();
			
			if(mTmpRotZ.length() == 0)
				mTmpRotZ.z = 1;
			
			mTmpRotX.setAllFrom(mAxisY);
			mTmpRotX.cross(mTmpRotZ);
			mTmpRotX.normalize();
			
			if(mTmpRotX.length() == 0) {
				mTmpRotZ.x += .0001f;
				mTmpRotX.cross(mTmpRotZ);
				mTmpRotX.normalize();
			}
			
			mTmpRotY.setAllFrom(mTmpRotZ);
			mTmpRotY.cross(mTmpRotX);
			
			Matrix.setIdentityM(mLookAtMatrix, 0);
			mLookAtMatrix[0] = mTmpRotX.x;
			mLookAtMatrix[1] = mTmpRotX.y;
			mLookAtMatrix[2] = mTmpRotX.z;
			mLookAtMatrix[4] = mTmpRotY.x;
			mLookAtMatrix[5] = mTmpRotY.y;
			mLookAtMatrix[6] = mTmpRotY.z;
			mLookAtMatrix[8] = mTmpRotZ.x;
			mLookAtMatrix[9] = mTmpRotZ.y;
			mLookAtMatrix[10] = mTmpRotZ.z;
		} else {
			mOrientation.multiply(mTmpOrientation.fromAngleAxis(mIsCamera ? mRotation.y : mRotation.y, mAxisY));
			mOrientation.multiply(mTmpOrientation.fromAngleAxis(mIsCamera ? mRotation.z : mRotation.z, mAxisZ));
			mOrientation.multiply(mTmpOrientation.fromAngleAxis(mIsCamera ? mRotation.x : mRotation.x, mAxisX));
			if(mIsCamera)
				mOrientation.inverseSelf();
		}
		//if (mGraphNode != null) mGraphNode.updateObject(this); //TODO: This may cause problems
	}

	public void rotateAround(Vector3 axis, float angle) {
		rotateAround(axis, angle, true);
	}
	
 	public void rotateAround(Vector3 axis, float angle, boolean append) {
 		if(append) {
 			mTmpOrientation.fromAngleAxis(angle, axis);
 			mOrientation.multiply(mTmpOrientation);
 		} else {
 			mOrientation.fromAngleAxis(angle, axis);
 		}
		mRotationDirty = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	public Quaternion getOrientation() {
		setOrientation(); // Force mOrientation to be recalculated
		return new Quaternion(mOrientation);
	}
	
	public void setOrientation(Quaternion quat) {
		mOrientation.setAllFrom(quat);
		mRotationDirty = false;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	public void setRotation(float rotX, float rotY, float rotZ) {
		mRotation.x = rotX;
		mRotation.y = rotY;
		mRotation.z = rotZ;
		mRotationDirty = true;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}
	
	public void setRotation(float[] rotationMatrix)
	{
		mOrientation.fromRotationMatrix(rotationMatrix);
	}
	
	public void setRotX(float rotX) {
		mRotation.x = rotX;
		mRotationDirty = true;
	}

	public float getRotX() {
		return mRotation.x;
	}

	public void setRotY(float rotY) {
		mRotation.y = rotY;
		mRotationDirty = true;
	}

	public float getRotY() {
		return mRotation.y;
	}

	public void setRotZ(float rotZ) {
		mRotation.z = rotZ;
		mRotationDirty = true;
	}

	public float getRotZ() {
		return mRotation.z;
	}
	
	public Vector3 getRotation() {
		return mRotation;
	}

	public void setRotation(Vector3 rotation) {
		mRotation.setAllFrom(rotation);
		mRotationDirty = true;
	}

	public void setScale(float scale) {
		mScale.x = scale;
		mScale.y = scale;
		mScale.z = scale;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public void setScale(float scaleX, float scaleY, float scaleZ) {
		mScale.x = scaleX;
		mScale.y = scaleY;
		mScale.z = scaleZ;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public void setScaleX(float scaleX) {
		mScale.x = scaleX;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public float getScaleX() {
		return mScale.x;
	}

	public void setScaleY(float scaleY) {
		mScale.y = scaleY;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public float getScaleY() {
		return mScale.y;
	}

	public void setScaleZ(float scaleZ) {
		mScale.z = scaleZ;
		if (mGraphNode != null) mGraphNode.updateObject(this);
	}

	public float getScaleZ() {
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
	
	public void setLookAt(float x, float y, float z) {
		if(mLookAt == null) mLookAt = new Vector3();
		mLookAt.x = x;
		mLookAt.y = y;
		mLookAt.z = z;
		mRotationDirty = true;
	}
	
	public void setLookAt(Vector3 lookAt) {
		if(lookAt == null) {
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
