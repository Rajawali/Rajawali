package rajawali;

import rajawali.math.Number3D;
import android.opengl.Matrix;

public class Camera {
	protected Number3D mPosition, mLookAt;
	protected float[] mVMatrix = new float[16];
	protected float[] mProjMatrix = new float[16];
	protected float mNearPlane  = 1.0f;
	protected float mFarPlane = 120.0f;
	protected float mFieldOfView = 45;
	
	public Camera(){
		mPosition = new Number3D();
		mLookAt = new Number3D();
	}
	
	public float[] getViewMatrix() {
		 Matrix.setLookAtM(mVMatrix, 0, 
				 mPosition.x, mPosition.y, mPosition.z, 
				 mLookAt.x, mLookAt.y, mLookAt.z, 
				 0f, 1.0f, 0.0f);
		 return mVMatrix;
	}
	
	public void setPosition(float x, float y, float z) {
		mPosition.x = x; mPosition.y = y; mPosition.z = z;
	}
	
	public void setPosition(Number3D position) {
		mPosition = position;
	}
	
	public Number3D getPosition() {
		return mPosition;
	}
	
	public void setProjectionMatrix(int width, int height) {
		float ratio = (float)width/height;
		float frustumH = (float)Math.tan(getFieldOfView() / 360.0 * Math.PI) * getNearPlane();
		float frustumW = frustumH * ratio;
		
		Matrix.frustumM(mProjMatrix, 0, -frustumW, frustumW, -frustumH, frustumH, getNearPlane(), getFarPlane());
	}
	
	public float[] getProjectionMatrix() {
		return mProjMatrix;
	}
	
	public void setLookAt(float lookAtX, float lookAtY, float lookAtZ) {
		mLookAt.x = lookAtX;
		mLookAt.y = lookAtY;
		mLookAt.z = lookAtZ;
	}
	
	public void lookAt(BaseObject3D lookatObject) {
		mLookAt.x = lookatObject.getX();
		mLookAt.y = lookatObject.getY();
		mLookAt.z = lookatObject.getZ();
	}
	
	public void lookAt(float x, float y, float z) {
		mLookAt.x = x;
		mLookAt.y = y;
		mLookAt.z = z;
	}

	public float getX() {
		return mPosition.x;
	}

	public void setX(float x) {
		mPosition.x = x;
	}

	public float getY() {
		return mPosition.y;
	}

	public void setY(float y) {
		mPosition.y = y;
	}

	public float getZ() {
		return mPosition.z;
	}

	public void setZ(float z) {
		mPosition.z = z;
	}

	public float getLookAtX() {
		return mLookAt.x;
	}

	public void setLookAtX(float lookAtX) {
		mLookAt.x = lookAtX;
	}

	public float getLookAtY() {
		return mLookAt.y;
	}

	public void setLookAtY(float lookAtY) {
		mLookAt.y = lookAtY;
	}

	public float getLookAtZ() {
		return mLookAt.z;
	}

	public void setLookAtZ(float lookAtZ) {
		mLookAt.z = lookAtZ;
	}

	public float getNearPlane() {
		return mNearPlane;
	}

	public void setNearPlane(float nearPlane) {
		this.mNearPlane = nearPlane;
	}

	public float getFarPlane() {
		return mFarPlane;
	}

	public void setFarPlane(float farPlane) {
		this.mFarPlane = farPlane;
	}

	public float getFieldOfView() {
		return mFieldOfView;
	}

	public void setFieldOfView(float fieldOfView) {
		this.mFieldOfView = fieldOfView;
	}
}
