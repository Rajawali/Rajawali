package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.math.Number3D;
import rajawali.math.Number3D.Axis;
import rajawali.math.Quaternion;

public class RotateAnimation3D extends Animation3D {
	protected float mDegreesToRotate;
	protected float mRotateFrom;
	protected float mRotationAngle;
	protected Number3D mRotationAxis;
	protected Quaternion mQuat;

	public RotateAnimation3D(Axis axis, float degreesToRotate) {
		this(axis, 0, degreesToRotate);
	}
	
	public RotateAnimation3D(Axis axis, float rotateFrom, float degreesToRotate ) {
		this(Number3D.getAxisVector(axis), rotateFrom, degreesToRotate);
	}
	
	public RotateAnimation3D(Number3D axis, float degreesToRotate ) {
		this(axis, 0, degreesToRotate);
	}

	public RotateAnimation3D(Number3D axis, float rotateFrom, float degreesToRotate ) {
		super();
		mQuat = new Quaternion();
		mRotationAxis = axis;
		mRotateFrom = rotateFrom;
		mDegreesToRotate = degreesToRotate;
	}
	
	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime) {
		mRotationAngle = mRotateFrom + (interpolatedTime * mDegreesToRotate);
		//Log.d("Rajawali", angle);
		mQuat.fromAngleAxis(mRotationAngle, mRotationAxis);
		mTransformable3D.setRotation(mQuat);
	}

	/**
	 * @deprecated use RotateAnimation3D(axis, degreesToRotate) or RotateAnimation(axis, rotateFrom, degreesToRotate)
	 * @param toRotate
	 */
	public RotateAnimation3D(Number3D toRotate) {
	}
	
	/**
	 * @deprecated use RotateAnimation3D(axis, degreesToRotate) or RotateAnimation(axis, rotateFrom, degreesToRotate) 
	 * @param fromRotate
	 * @param toRotate
	 */
	public RotateAnimation3D(Number3D fromRotate, Number3D toRotate) {
	}
}

