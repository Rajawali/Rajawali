package rajawali;

import android.opengl.Matrix;

public class Camera3D {
	protected float x, y, z, lookAtX, lookAtY, lookAtZ;
	protected float[] mVMatrix = new float[16];
	protected float mNearPlane  = 1.0f;
	protected float mFarPlane = 80.0f;
	protected float mFieldOfView = 45;
	
	public Camera3D(){}
	
	public float[] getViewMatrix() {
		 Matrix.setLookAtM(mVMatrix, 0, x, y, z, lookAtX, lookAtY, lookAtZ, 0f, 1.0f, 0.0f);
		 return mVMatrix;
	}
	
	public void setPosition(float x, float y, float z) {
		this.x = x; this.y = y; this.z = z;
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
