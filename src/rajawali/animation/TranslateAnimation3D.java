package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.curves.ICurve3D;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;

public class TranslateAnimation3D extends Animation3D {

	protected Vector3 mToPosition;
	protected Vector3 mFromPosition;
	protected Vector3 mDiffPosition;
	protected Vector3 mMultipliedPosition = new Vector3();
	protected Vector3 mAddedPosition = new Vector3();
	protected Vector3 mForwardVec = Vector3.getAxisVector(Axis.Z);
	protected Vector3 mTmpVec = new Vector3();
	protected Vector3 mObjectRay = Vector3.getAxisVector(Axis.Z);
	protected Quaternion mTmpOrientation = new Quaternion();
	protected Quaternion mTmpOrientation2 = new Quaternion();
	
	protected boolean mOrientToPath = false;
	protected ICurve3D mSplinePath;
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

	public TranslateAnimation3D(ICurve3D splinePath) {
		super();
		mSplinePath = splinePath;
	}

	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (mFromPosition == null)
			mFromPosition = new Vector3(transformable3D.getPosition());
	}

	private Vector3 mTempPoint1 = new Vector3();
	private Vector3 mTempPoint2 = new Vector3();
	private Vector3 mTempPoint3 = new Vector3();
	
	@Override
	protected void applyTransformation() {
		if (mSplinePath == null) {
			if (mDiffPosition == null)
				mDiffPosition = Vector3.subtractAndCreate(mToPosition, mFromPosition);
			mMultipliedPosition.scaleAndSet(mDiffPosition, (float) mInterpolatedTime);
			mAddedPosition.addAndSet(mFromPosition, mMultipliedPosition);
			mTransformable3D.setPosition(mAddedPosition);
		} else {
			mSplinePath.calculatePoint(mTempPoint1, (float) mInterpolatedTime);
			mTransformable3D.setPosition(mTempPoint1);

			if (mOrientToPath)
			{
				// -- calculate tangent
				mSplinePath.calculatePoint(mTempPoint2, (float) (mInterpolatedTime + (-mLookatDelta * (mIsReversing ? -1 : 1))));
				mSplinePath.calculatePoint(mTempPoint3, (float) (mInterpolatedTime + (mLookatDelta * (mIsReversing ? -1 : 1))));
				
				// -- calculate direction vector
				mTmpVec.subtractAndSet(mTempPoint3, mTempPoint2);
				mTmpVec.normalize();
					
				mTmpOrientation.setFromRotationBetween(mObjectRay, mTmpVec);
				mTmpOrientation.normalize();
				mTransformable3D.getOrientation(mTmpOrientation2);
				mTmpOrientation2.normalize();
				mTmpOrientation2.multiply(mTmpOrientation);
				mTmpOrientation2.normalize();
				mTransformable3D.setOrientation(mTmpOrientation2);
				mTmpOrientation2.normalize();				
				
				mObjectRay.setAll(mTmpVec);
			}
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
