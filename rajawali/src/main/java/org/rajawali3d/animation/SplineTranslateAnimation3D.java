package org.rajawali3d.animation;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.math.vector.Vector3;


public class SplineTranslateAnimation3D extends Animation3D {

	// Place holders for transformation math
	protected final Vector3 mTempPoint1;
	protected final Vector3 mTempPoint2;
	
	protected ATransformable3D mTarget;
	protected Vector3 mUp;
	protected boolean mOrientToPath;
	protected ICurve3D mSplinePath;
	protected double mLookatDelta;


	public SplineTranslateAnimation3D(ICurve3D splinePath) {
		super();
		
		mSplinePath = splinePath;
		mTempPoint1 = new Vector3();
		mTempPoint2 = new Vector3();
	}
	
	@Override
	protected void applyTransformation() {
		mSplinePath.calculatePoint(mTempPoint1, mInterpolatedTime);
		mTransformable3D.setPosition(mTempPoint1);

		if(mTarget != null) {
			mTransformable3D.setLookAt(mTarget.getPosition());
			if(mUp != null) mTransformable3D.setUpAxis(mUp);
		}

		if (mOrientToPath) {
			// -- calculate tangent
			mSplinePath.calculatePoint(mTempPoint2, mInterpolatedTime + mLookatDelta * (mIsReversing ? -1 : 1));
			mTransformable3D.setLookAt(mTempPoint2);
		}
	}

	public void setLookAt(ATransformable3D target) {
		mTarget = target;
		if(target == null) mUp = null;
	}

	public void setLookAt(ATransformable3D target, Vector3 up) {
		mTarget = target;
		mUp = up;
	}

	public void setLookAt(ATransformable3D target, Vector3.Axis up) {
		mTarget = target;
		mUp = new Vector3().setAll(up);
	}

	public ATransformable3D getLookAt() {
		return mTarget;
	}

	public Vector3 getUpAxis() {
		return mUp;
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

	public void setDurationMilliseconds(long duration) {
		super.setDurationMilliseconds(duration);
		mLookatDelta = 300.f / duration;
	}
	
}
