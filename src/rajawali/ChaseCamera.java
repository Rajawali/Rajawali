package rajawali;

import rajawali.math.Number3D;
import rajawali.math.Quaternion;

public class ChaseCamera extends Camera {
	protected Number3D mCameraOffset;
	protected BaseObject3D mObjectToChase;
	protected Number3D mUpVector;
	protected float mSlerpFactor = .1f;
	protected Quaternion mTmpOr;
	
	public ChaseCamera() {
		this(new Number3D(0, 3, 16), .1f, null);
	}
	
	public ChaseCamera(Number3D cameraOffset, float slerpFactor) {
		this(cameraOffset, .1f, null);
	}

	public ChaseCamera(Number3D cameraOffset, float slerpFactor, BaseObject3D objectToChase) {
		super();
		mTmpOr = new Quaternion();
		mUpVector = Number3D.getUpVector();
		mCameraOffset = cameraOffset;
		mObjectToChase = objectToChase;
		mSlerpFactor = slerpFactor;
	}
	
	public float[] getViewMatrix() {
		mRotationDirty = false;
		mPosition.setAllFrom(mCameraOffset);

		mTmpOr.setAllFrom(mObjectToChase.getOrientation());
		mPosition.setAllFrom(mTmpOr.multiply(mCameraOffset));
		mPosition.add(mObjectToChase.getPosition());
		mTmpOr.inverseSelf();
		mOrientation.setAllFrom(Quaternion.slerp(mSlerpFactor, mOrientation, mTmpOr, true));
		mOrientation.toRotationMatrix(mRotationMatrix);
		
		return super.getViewMatrix();
	}

	public void setCameraOffset(Number3D offset) {
		mCameraOffset.setAllFrom(offset);
	}
	
	public Number3D getCameraOffset() {
		return mCameraOffset;
	}
	
	public void setSlerpFactor(float factor) {
		mSlerpFactor = factor;
	}
	
	public float getSlerpFactor() {
		return mSlerpFactor;
	}

	public BaseObject3D getObjectToChase() {
		return mObjectToChase;
	}

	public void setObjectToChase(BaseObject3D objectToChase) {
		this.mObjectToChase = objectToChase;
	}
	
	public BaseObject3D getChasedObject() {
		return mObjectToChase;
	}	
}