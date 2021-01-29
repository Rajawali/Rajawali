package org.rajawali3d.animation;

import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.curves.ICurve4D;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

public class SplineOrientationAnimation3D extends Animation3D {

    // Place holders for transformation math
    protected final Quaternion mTempPoint1;

    protected ICurve4D mSplinePath;


    public SplineOrientationAnimation3D(ICurve4D splinePath) {
        super();

        mSplinePath = splinePath;
        mTempPoint1 = new Quaternion();
    }

    @Override
    protected void applyTransformation() {
        mSplinePath.calculatePoint(mTempPoint1, mInterpolatedTime);
        mTransformable3D.setOrientation(mTempPoint1);
    }

    public void setDurationMilliseconds(long duration) {
        super.setDurationMilliseconds(duration);
    }

}

