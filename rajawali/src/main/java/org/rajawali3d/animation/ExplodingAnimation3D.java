package org.rajawali3d.animation;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.ExplodingMaterialPlugin;

public class ExplodingAnimation3D extends Animation3D {
    final double mDiffFactorValue;
    final double mFromFactorValue;
    final double mToFactorValue;

    public ExplodingAnimation3D(double from, double to) {
        super();
        mFromFactorValue = from;
        mToFactorValue = to;
        mDiffFactorValue = mToFactorValue - mFromFactorValue;
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
        ExplodingMaterialPlugin plugin = (ExplodingMaterialPlugin) material.getPlugin(ExplodingMaterialPlugin.class);
        if(plugin==null) return;
        double factor = mDiffFactorValue * mInterpolatedTime;
        plugin.setFactor(factor);
    }
}

