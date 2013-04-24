package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.math.Number3D;
import rajawali.math.Number3D.Axis;
import rajawali.math.Quaternion;

public class RotateAnimation3D extends Animation3D {
	protected double mDegreesToRotate;
	protected double mRotateFrom;
	protected double mRotationAngle;
	protected double mRotateX, mRotateY, mRotateZ;
	protected Number3D mRotationAxis;
	protected Quaternion mQuat;
	protected Quaternion mQuatFrom;
	protected Quaternion mTmpOrientation = new Quaternion();
	protected boolean mCopyCurrentOrientation;
	protected boolean mAngleAxisRotation;
	
	public RotateAnimation3D(Axis axis, double degreesToRotate) {
		this(axis, 0, degreesToRotate);
		mCopyCurrentOrientation = true;
	}
	
	public RotateAnimation3D(Axis axis, double rotateFrom, double degreesToRotate ) {
		this(Number3D.getAxisVector(axis), rotateFrom, degreesToRotate);
	}
	
	public RotateAnimation3D(Number3D axis, double degreesToRotate ) {
		this(axis, 0, degreesToRotate);
		mCopyCurrentOrientation = true;
	}

	public RotateAnimation3D(Number3D axis, double rotateFrom, double degreesToRotate ) {
		super();
		mAngleAxisRotation = true;
		mQuat = new Quaternion();
		mQuatFrom = new Quaternion();
		// TODO: Switch Quaternions to take in doubles instead of floats.
		mQuatFrom.fromAngleAxis((float)rotateFrom, axis);
		mRotationAxis = axis;
		mRotateFrom = rotateFrom;
		mDegreesToRotate = degreesToRotate;
	}
	
	public RotateAnimation3D(double xRotate, double yRotate, double zRotate) {
		super();
		mAngleAxisRotation = false;
		mCopyCurrentOrientation = true;

		mQuat = Quaternion.getIdentity();
		mQuatFrom = new Quaternion();
		
		mRotateX = xRotate;
		mRotateY = yRotate;
		mRotateZ = zRotate;
		
		mQuat.multiply(new Quaternion().fromAngleAxis((float)yRotate, Number3D.getAxisVector(Axis.Y)));
		mQuat.multiply(new Quaternion().fromAngleAxis((float)zRotate, Number3D.getAxisVector(Axis.Z)));
		mQuat.multiply(new Quaternion().fromAngleAxis((float)xRotate, Number3D.getAxisVector(Axis.X)));
	}
	
	public RotateAnimation3D(Number3D rotate) {
		this(rotate.x, rotate.y, rotate.z);
	}
	
	@Override
	public void eventStart() {
		if(mCopyCurrentOrientation)
			mQuatFrom.setAllFrom(mTransformable3D.getOrientation());
		super.eventStart();
	}
	
	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if(mCopyCurrentOrientation)
			mQuatFrom.setAllFrom(transformable3D.getOrientation());
	}
	
	@Override
	protected void applyTransformation() {
		if (mAngleAxisRotation) {
			// Rotation around an axis by amount of degrees.
			mRotationAngle = mRotateFrom + (mInterpolatedTime * mDegreesToRotate);
			mQuat.fromAngleAxis((float)mRotationAngle, mRotationAxis);
			mQuat.multiply(mQuatFrom);
			mTransformable3D.setOrientation(mQuat);
		} else {
			mTransformable3D.setOrientation(Quaternion.slerp((float)mInterpolatedTime, mQuatFrom, mQuat, true));
		}
	}
	
	/**
	 * @deprecated use RotateAnimation3D(axis, degreesToRotate) or RotateAnimation(axis, rotateFrom, degreesToRotate) 
	 * @param fromRotate
	 * @param toRotate
	 */
	@Deprecated
	public RotateAnimation3D(Number3D fromRotate, Number3D toRotate) {
	}
}

