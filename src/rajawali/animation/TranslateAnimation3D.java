package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.curves.ICurve3D;
import rajawali.math.Quaternion;
import rajawali.math.Vector3;
import rajawali.math.Vector3.Axis;

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

	private Vector3 mTempPoint1=new Vector3();
	private Vector3 mTempPoint2=new Vector3();
	private Vector3 mTempPoint3=new Vector3();
	
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
			Vector3 pathPoint = mSplinePath.calculatePoint((float) mInterpolatedTime,mTempPoint1);
			mTransformable3D.setPosition(pathPoint);

			if (mOrientToPath)
			{
				// -- calculate tangent
				Vector3 point1 = mSplinePath
							.calculatePoint((float) (mInterpolatedTime + (-mLookatDelta * (mIsReversing ? -1 : 1))),mTempPoint2);
				Vector3 point2 = mSplinePath
						.calculatePoint((float) (mInterpolatedTime + (mLookatDelta * (mIsReversing ? -1 : 1))),mTempPoint3);
				
				// -- calculate direction vector
				mTmpVec.setAllFrom(point2);
				mTmpVec.subtract(point1);
				mTmpVec.normalize();
					
				mTmpOrientation.setFromRotationBetween(mObjectRay, mTmpVec);
				mTmpOrientation.normalize();
				mTransformable3D.getOrientation(mTmpOrientation2);
				mTmpOrientation2.normalize();
				mTmpOrientation2.multiply(mTmpOrientation);
				mTmpOrientation2.normalize();
				mTransformable3D.setOrientation(mTmpOrientation2);
				mTmpOrientation2.normalize();				
				
				mObjectRay.setAllFrom(mTmpVec);
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
