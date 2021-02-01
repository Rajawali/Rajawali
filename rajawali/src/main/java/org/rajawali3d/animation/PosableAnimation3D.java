package org.rajawali3d.animation;

import androidx.annotation.FloatRange;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.PosableMaterialPlugin;

import static org.rajawali3d.math.MathUtil.clamp;

public class PosableAnimation3D extends Animation3D {
    final double mDiffInterpolatedValue;
    final double mFromInterpolatedValue;
    final double mToInterpolatedValue;

    public PosableAnimation3D(@FloatRange(from = 0.0d, to = 1.0d) double from,
                                   @FloatRange(from = 0.0d, to = 1.0d) double to) {
        super();
        mFromInterpolatedValue = clamp(from, 0, 1);
        mToInterpolatedValue = clamp(to, 0, 1);
        mDiffInterpolatedValue = mToInterpolatedValue - mFromInterpolatedValue;
    }

    @Override
    public void setTransformable3D(ATransformable3D transformable3D) {
        super.setTransformable3D(transformable3D);
        if (!(transformable3D instanceof Object3D)) {
            throw new RuntimeException(
                    getClass().getSimpleName() +"requires the passed transformable3D to be an instance of "
                            + Object3D.class.getSimpleName());
        }
    }

    @Override
    protected void applyTransformation() {
        Material material = ((Object3D)mTransformable3D).getMaterial();
        if(material==null) return;
        PosableMaterialPlugin plugin = (PosableMaterialPlugin) material.getPlugin(PosableMaterialPlugin.class);
        if(plugin==null) return;
        double interpolation = Math.abs(mDiffInterpolatedValue * mInterpolatedTime);
        plugin.setInterpolation(clamp(interpolation, 0, 1));
    }
}

