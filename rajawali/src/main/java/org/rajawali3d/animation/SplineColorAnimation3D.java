package org.rajawali3d.animation;

import android.graphics.Color;

import org.rajawali3d.curves.ICurve1D;
import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;


public class SplineColorAnimation3D extends Animation3D {
    protected ICurve1D mSplinePath;

    public SplineColorAnimation3D(ICurve1D splinePath) {
        super();
        mSplinePath = splinePath;
    }

    @Override
    public void setTransformable3D(ATransformable3D transformable3D) {
        super.setTransformable3D(transformable3D);
        if (!(transformable3D instanceof Object3D)) {
            throw new RuntimeException(
                    getClass().getSimpleName() + " requires the passed transformable3D to be an instance of "
                            + Object3D.class.getSimpleName());
        }
    }

    @Override
    protected void applyTransformation() {
        int color = mSplinePath.calculatePoint(mInterpolatedTime);
        ((Object3D) mTransformable3D).setColor(color);
    }

    public void setDurationMilliseconds(long duration) {
        super.setDurationMilliseconds(duration);
    }

}
