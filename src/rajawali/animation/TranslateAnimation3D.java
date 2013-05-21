package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.math.Vector3;

public class TranslateAnimation3D extends Animation3D {

	protected Vector3 mToPosition;
	protected Vector3 mFromPosition;
	protected Vector3 mDiffPosition;
	protected Vector3 mMultipliedPosition = new Vector3();
	protected Vector3 mAddedPosition = new Vector3();
	protected boolean mOrientToPath = false;
	protected ISpline mSplinePath;
	protected float mLookatDelta;

	public TranslateAnimation3D(Vector3 toPosition) {
		super();
		mToPosition = toPosition;
	}

	public TranslateAnimation3D(Vector3 fromPosition, Vector3 toPosition) {
		super();
		mFromPosition = fromPosition;
		mToPosition = toPosition;
	}

	public TranslateAnimation3D(ISpline splinePath) {
		super();
		mSplinePath = splinePath;
	}

	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (mFromPosition == null)
			mFromPosition = new Vector3(transformable3D.getPosition());
	}

	@Override
	protected void applyTransformation() {
		if (mSplinePath == null) {
			if (mDiffPosition == null)
				mDiffPosition = Vector3.subtract(mToPosition, mFromPosition);
			mMultipliedPosition.setAllFrom(mDiffPosition);
			mMultipliedPosition.multiply((float) mInterpolatedTime);
			mAddedPosition.setAllFrom(mFromPosition);
			mAddedPosition.add(mMultipliedPosition);
			mTransformable3D.setPosition(mAddedPosition);
		} else {
			Vector3 pathPoint = mSplinePath.calculatePoint((float) mInterpolatedTime);
			mTransformable3D.setPosition(pathPoint);
			mTransformable3D.getPosition().setAllFrom(pathPoint);

			if (mOrientToPath)
				mTransformable3D.setLookAt(mSplinePath
						.calculatePoint((float) (mInterpolatedTime + (mLookatDelta * (mIsReversing ? -1 : 1)))));
		}
	}

	public boolean getOrientToPath() {
		return mOrientToPath;
	}

	public void setOrientToPath(boolean orientToPath) {
		if (mSplinePath == null)
			throw new RuntimeException("You must set a spline path before orientation to path is possible.");

		mOrientToPath = orientToPath;
		mSplinePath.setCalculateTangents(orientToPath);
	}

	public void setDuration(long duration) {
		super.setDuration(duration);
		mLookatDelta = 300.f / duration;
	}
	
	@Override
	public void reset() {
		super.reset();
		// Diff position needs to be reset or future uses of animation will cause unexpected translations.
		mDiffPosition = null;
	}
}
