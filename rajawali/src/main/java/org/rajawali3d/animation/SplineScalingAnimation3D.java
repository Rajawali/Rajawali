package org.rajawali3d.animation;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.math.vector.Vector3;

public class SplineScalingAnimation3D extends Animation3D {

    // Place holders for transformation math
    protected final Vector3 mTempPoint1;

    protected ICurve3D mSplinePath;


    public SplineScalingAnimation3D(ICurve3D splinePath) {
        super();

        mSplinePath = splinePath;
        mTempPoint1 = new Vector3();
    }

    @Override
    protected void applyTransformation() {
        mSplinePath.calculatePoint(mTempPoint1, mInterpolatedTime);
        mTransformable3D.setScale(mTempPoint1);
    }

    public void setDurationMilliseconds(long duration) {
        super.setDurationMilliseconds(duration);
    }

}

