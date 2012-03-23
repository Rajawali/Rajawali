package rajawali;

import rajawali.math.Number3D;
import android.opengl.Matrix;

public class Camera implements ITransformable3D {
	protected Number3D mPosition, mLookAt, mRotation;
	protected float[] mVMatrix = new float[16];
	protected float[] mRotationMatrix = new float[16];
	protected float[] mProjMatrix = new float[16];
	protected float mNearPlane = 1.0f;
	protected float mFarPlane = 120.0f;
	protected float mFieldOfView = 45;
	protected boolean mUseRotationMatrix = false;
	protected float[] mRotateMatrixTmp = new float[16];
	protected float[] mTmpMatrix = new float[16];

	public Camera() {
		mPosition = new Number3D();
		mRotation = new Number3D();
		// mLookAt = new Number3D();
	}

	public float[] getViewMatrix() {
		if (mLookAt != null) {
			Matrix.setLookAtM(mVMatrix, 0, mPosition.x, mPosition.y,
					mPosition.z, mLookAt.x, mLookAt.y, mLookAt.z, 0f, 1.0f,
					0.0f);
		} else {
			if (mUseRotationMatrix == false) {
				Matrix.setIdentityM(mRotationMatrix, 0);
				rotateM(mRotationMatrix, 0, -mRotation.x, 1.0f, 0.0f, 0.0f);
				rotateM(mRotationMatrix, 0, -mRotation.y + 180, 0.0f, 1.0f, 0.0f);
				rotateM(mRotationMatrix, 0, -mRotation.z, 0.0f, 0.0f, 1.0f);
			}
			Matrix.setIdentityM(mTmpMatrix, 0);
			Matrix.setIdentityM(mVMatrix, 0);
			Matrix.translateM(mTmpMatrix, 0, -mPosition.x, -mPosition.y,
					-mPosition.z);
			Matrix.multiplyMM(mVMatrix, 0, mRotationMatrix, 0, mTmpMatrix, 0);
		}
		return mVMatrix;
	}

	protected void rotateM(float[] m, int mOffset, float a, float x, float y,
			float z) {
		Matrix.setIdentityM(mRotateMatrixTmp, 0);
		Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
		System.arraycopy(m, 0, mTmpMatrix, 0, 16);
		Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
	}

	public void setRotationMatrix(float[] m) {
		mRotationMatrix = m;
	}

	public void setPosition(float x, float y, float z) {
		mPosition.x = x;
		mPosition.y = y;
		mPosition.z = z;
	}

	public void setPosition(Number3D position) {
		mPosition = position;
	}

	public Number3D getPosition() {
		return mPosition;
	}

	public Number3D getRotation() {
		return mRotation;
	}

	public void setProjectionMatrix(int width, int height) {
		float ratio = (float) width / height;
		float frustumH = (float) Math.tan(getFieldOfView() / 360.0 * Math.PI)
				* getNearPlane();
		float frustumW = frustumH * ratio;

		Matrix.frustumM(mProjMatrix, 0, -frustumW, frustumW, -frustumH,
				frustumH, getNearPlane(), getFarPlane());
	}

	public float[] getProjectionMatrix() {
		return mProjMatrix;
	}

	public void setLookAt(float lookAtX, float lookAtY, float lookAtZ) {
		if (mLookAt == null)
			mLookAt = new Number3D();
		mLookAt.x = lookAtX;
		mLookAt.y = lookAtY;
		mLookAt.z = lookAtZ;
	}

	public void lookAt(BaseObject3D lookatObject) {
		if (mLookAt == null)
			mLookAt = new Number3D();
		mLookAt.x = lookatObject.getX();
		mLookAt.y = lookatObject.getY();
		mLookAt.z = lookatObject.getZ();
	}

	public void lookAt(float x, float y, float z) {
		if (mLookAt == null)
			mLookAt = new Number3D();
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

	public void setRotation(float rotX, float rotY, float rotZ) {
		mRotation.x = rotX;
		mRotation.y = rotY;
		mRotation.z = rotZ;
	}

	public void setRotX(float rotX) {
		mRotation.x = rotX;
	}

	public float getRotX() {
		return mRotation.x;
	}

	public void setRotY(float rotY) {
		mRotation.y = rotY;
	}

	public float getRotY() {
		return mRotation.y;
	}

	public void setRotZ(float rotZ) {
		mRotation.z = rotZ;
	}

	public float getRotZ() {
		return mRotation.z;
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

	public boolean getUseRotationMatrix() {
		return mUseRotationMatrix;
	}

	public void setUseRotationMatrix(boolean useRotationMatrix) {
		this.mUseRotationMatrix = useRotationMatrix;
	}

	@Override
	public void setScale(float scale) {}

	@Override
	public void setScale(float scaleX, float scaleY, float scaleZ) {}

	@Override
	public void setScaleX(float scaleX) {}

	@Override
	public float getScaleX() { return 0; }

	@Override
	public void setScaleY(float scaleY) {}

	@Override
	public float getScaleY() { return 0; }

	@Override
	public void setScaleZ(float scaleZ) {}

	@Override
	public float getScaleZ() { return 0; }

	@Override
	public void setRotation(Number3D rotation) {
		mRotation = rotation;
	}

	@Override
	public Number3D getScale() { return null; }

	@Override
	public void setScale(Number3D scale) {}
}
