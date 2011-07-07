package rajawali;

import android.opengl.Matrix;

public class Camera3D {
	protected float x, y, z, lookAtX, lookAtY, lookAtZ;
	protected float[] mVMatrix = new float[16];
	
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
}
