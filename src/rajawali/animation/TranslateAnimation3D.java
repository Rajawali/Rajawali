package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.BaseObject3D;
import rajawali.math.Number3D;
import android.view.animation.Interpolator;

public class TranslateAnimation3D extends Animation3D {
	protected Number3D mToPosition;
	protected Number3D mFromPosition;
	protected Number3D mDiffPosition;
	protected Number3D mMultipliedPosition = new Number3D();
	protected Number3D mAddedPosition = new Number3D();
	protected boolean mOrientToPath = false;
	protected ISpline mSplinePath;
	protected float mDelta;
	
	public TranslateAnimation3D(Number3D toPosition) {
		super();
		mToPosition = toPosition;
	}

	public TranslateAnimation3D(Number3D fromPosition, Number3D toPosition) {
		super();
		mFromPosition = fromPosition;
		mToPosition = toPosition;
	}

	public TranslateAnimation3D(ISpline splinePath) {
		super();
		mSplinePath = splinePath;
	}

	public TranslateAnimation3D(BaseObject3D object, Number3D toPosition, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(toPosition);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}

	public TranslateAnimation3D(BaseObject3D object, Number3D fromPosition, Number3D toPosition, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(fromPosition, toPosition);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}

	public TranslateAnimation3D(BaseObject3D object, ISpline splinePath, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(splinePath);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}
	
	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (mFromPosition == null)
			mFromPosition = new Number3D(transformable3D.getPosition());
	}

	@Override
	protected void applyTransformation(float interpolatedTime) {
		super.applyTransformation(interpolatedTime);
		if (mSplinePath == null) {
			if (mDiffPosition == null)
				mDiffPosition = Number3D.subtract(mToPosition, mFromPosition);
			mMultipliedPosition.setAllFrom(mDiffPosition);
			mMultipliedPosition.multiply(interpolatedTime);
			mAddedPosition.setAllFrom(mFromPosition);
			mAddedPosition.add(mMultipliedPosition);
			mTransformable3D.getPosition().setAllFrom(mAddedPosition);
		} else {
			Number3D pathPoint = mSplinePath.calculatePoint(interpolatedTime);
			mTransformable3D.getPosition().setAllFrom(pathPoint);

			if (mOrientToPath) {
				mTransformable3D.setLookAt(mSplinePath.calculatePoint(interpolatedTime + (mDelta * mDirection)));
			}
		}
	}

	public boolean getOrientToPath() {
		return mOrientToPath;
	}

	public void setOrientToPath(boolean orientToPath) {
		this.mOrientToPath = orientToPath;
		mSplinePath.setCalculateTangents(orientToPath);
	}
	
	public void setDuration(long duration) {
		super.setDuration(duration);
		mDelta = 300.f / duration;
	}
}
