package rajawali.animation;

import android.view.animation.Interpolator;
import rajawali.BaseObject3D;
import rajawali.math.Number3D;

public class RotationAnimation3D extends Animation3D {
	private float mXRotate;
	private float mYRotate;
	private float mZRotate;
	private float mXAnglePrev;
	private float mYAnglePrev;
	private float mZAnglePrev;
	
	public RotationAnimation3D(float xRotate, float yRotate, float zRotate) {
		super();
	
		mXRotate = xRotate;
		mYRotate = yRotate;
		mZRotate = zRotate;
		
		mXAnglePrev = 0;
		mYAnglePrev = 0;
		mZAnglePrev = 0;
	}
	
	public RotationAnimation3D(Number3D rotate) {
		super();
	
		mXRotate = rotate.x;
		mYRotate = rotate.y;
		mZRotate = rotate.z;
		
		mXAnglePrev = 0;
		mYAnglePrev = 0;
		mZAnglePrev = 0;
	}
	
	public RotationAnimation3D(BaseObject3D object, Number3D rotate, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(rotate);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}

	public RotationAnimation3D(BaseObject3D object, float xRotate, float yRotate, float zRotate, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(xRotate, yRotate, zRotate);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime) {
		super.applyTransformation(interpolatedTime);
		Number3D rotation = mTransformable3D.getRotation();
		
		float xAngleCurrent = (interpolatedTime * mXRotate);
		float xAngleDelta = rotation.x - mXAnglePrev + xAngleCurrent;
		mXAnglePrev = xAngleCurrent;
		float yAngleCurrent = (interpolatedTime * mYRotate);
		float yAngleDelta = rotation.y - mYAnglePrev + yAngleCurrent;
		mYAnglePrev = yAngleCurrent;
		float zAngleCurrent = (interpolatedTime * mZRotate);
		float zAngleDelta = rotation.z - mZAnglePrev + zAngleCurrent;
		mZAnglePrev = zAngleCurrent;

		rotation.x = xAngleDelta;
		rotation.y = yAngleDelta;
		rotation.z = zAngleDelta;

		mTransformable3D.setRotation(rotation);
	}
}

