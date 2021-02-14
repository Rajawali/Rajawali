package org.rajawali3d.animation;

import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.materials.plugins.IMaterialPlugin;

public class KeyframeAnimation3D extends Animation3D {
    Class pluginClass;

    public KeyframeAnimation3D(Class pluginClass) {
        this.pluginClass = pluginClass;
    }

    @Override
    protected void applyTransformation() {
        if(mTransformable3D instanceof Object3D) {
            Object3D target = (Object3D) mTransformable3D;
            IMaterialPlugin plugin = target.getMaterial().getPlugin(pluginClass);
            if(plugin instanceof IInterpolatable) {
                ((IInterpolatable) plugin).interpolate(mInterpolatedTime);
            }
        }
    }

    @Override
    protected void eventEnd() {
        super.eventEnd();
        if(mTransformable3D instanceof Object3D) {
            Object3D target = (Object3D) mTransformable3D;
            IMaterialPlugin plugin = target.getMaterial().getPlugin(pluginClass);
            if(plugin instanceof IInterpolatable) {
                ((IInterpolatable) plugin).enableInterpolation(false);
            }
        }
    }

    @Override
    protected void eventStart() {
        super.eventStart();
        if(mTransformable3D instanceof Object3D) {
            Object3D target = (Object3D) mTransformable3D;
            IMaterialPlugin plugin = target.getMaterial().getPlugin(pluginClass);
            if(plugin instanceof IInterpolatable) {
                ((IInterpolatable) plugin).enableInterpolation(true);
            }
        }
    }
}
