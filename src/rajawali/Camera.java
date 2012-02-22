package rajawali;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Camera {
	protected float x, y, z, lookAtX, lookAtY, lookAtZ;
	protected float[] mVMatrix = new float[16];
	protected float[] mProjMatrix = new float[16];
	protected float mNearPlane  = 1.0f;
	protected float mFarPlane = 120.0f;
	protected float mFieldOfView = 45;
	
	public Camera(){
	}
	
	public float[] getViewMatrix() {
		 Matrix.setLookAtM(mVMatrix, 0, x, y, z, lookAtX, lookAtY, lookAtZ, 0f, 1.0f, 0.0f);
		 return mVMatrix;
	}
	
	public void setPosition(float x, float y, float z) {
		this.x = x; this.y = y; this.z = z;
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
		this.lookAtX = lookAtX;
		this.lookAtY = lookAtY;
		this.lookAtZ = lookAtZ;
	}
	
	public void lookAt(BaseObject3D lookatObject) {
		this.lookAtX = lookatObject.getX();
		this.lookAtY = lookatObject.getY();
		this.lookAtZ = lookatObject.getZ();
	}
	
	public void lookAt(float x, float y, float z) {
		this.lookAtX = x;
		this.lookAtY = y;
		this.lookAtZ = z;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getLookAtX() {
		return lookAtX;
	}

	public void setLookAtX(float lookAtX) {
		this.lookAtX = lookAtX;
	}

	public float getLookAtY() {
		return lookAtY;
	}

	public void setLookAtY(float lookAtY) {
		this.lookAtY = lookAtY;
	}

	public float getLookAtZ() {
		return lookAtZ;
	}

	public void setLookAtZ(float lookAtZ) {
		this.lookAtZ = lookAtZ;
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
