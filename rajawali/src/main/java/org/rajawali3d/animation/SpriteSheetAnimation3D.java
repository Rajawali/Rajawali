package org.rajawali3d.animation;

import androidx.annotation.IntRange;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.SpriteSheetMaterialPlugin;
import org.rajawali3d.math.MathUtil;

public class SpriteSheetAnimation3D extends Animation3D {
    final double mFrom;
    final double mTo;

    public SpriteSheetAnimation3D(@IntRange(from=0) int from, @IntRange(from=0) int to) {
        super();
        mFrom = from;
        mTo = to;
    }

    @Override
    public void setTransformable3D(ATransformable3D transformable3D) {
        super.setTransformable3D(transformable3D);
        if (!(transformable3D instanceof Object3D)) {
            throw new RuntimeException(
                    getClass().getSimpleName() + "requires the passed transformable3D to be an instance of "
                            + Object3D.class.getSimpleName());
        }
    }

    @Override
    protected void applyTransformation() {
        Material material = ((Object3D) mTransformable3D).getMaterial();
        if (material == null) return;
        SpriteSheetMaterialPlugin plugin = (SpriteSheetMaterialPlugin) material.getPlugin(SpriteSheetMaterialPlugin.class);
        if (plugin == null) return;
        double frame = MathUtil.mix(mFrom, mTo, mInterpolatedTime);
        plugin.selectFrame(Math.floor(frame));
   }
}
