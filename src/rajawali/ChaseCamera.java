package rajawali;

import rajawali.math.Vector3;
import rajawali.math.Quaternion;

public class ChaseCamera extends Camera {
	protected Vector3 mCameraOffset;
	protected BaseObject3D mObjectToChase;
	protected Vector3 mUpVector;
	protected float mSlerpFactor = .1f;
	protected Quaternion mTmpOr;
	
	public ChaseCamera() {
		this(new Vector3(0, 3, 16), .1f, null);
	}
	
	public ChaseCamera(Vector3 cameraOffset, float slerpFactor) {
		this(cameraOffset, .1f, null);
	}

	public ChaseCamera(Vector3 cameraOffset, float slerpFactor, BaseObject3D objectToChase) {
		super();
		mTmpOr = new Quaternion();
		mUpVector = Vector3.getUpVector();
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
		mOrientation.setAllFrom(Quaternion.slerp(mOrientation, mTmpOr, mSlerpFactor));
		mOrientation.toRotationMatrix(mRotationMatrix);
		
		return super.getViewMatrix();
	}

	public void setCameraOffset(Vector3 offset) {
		mCameraOffset.setAllFrom(offset);
	}
	
	public Vector3 getCameraOffset() {
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