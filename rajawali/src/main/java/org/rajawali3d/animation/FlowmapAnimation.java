package org.rajawali3d.animation;

import androidx.annotation.NonNull;
import org.rajawali3d.materials.plugins.FlowmapTexturePlugin;

public class FlowmapAnimation extends Animation {
    FlowmapTexturePlugin mPlugin;
    final double mDiff;
    final double mFrom;
    final double mTo;

    public FlowmapAnimation(@NonNull FlowmapTexturePlugin plugin) {
        this(plugin,-1,1);
    }

    public FlowmapAnimation(@NonNull FlowmapTexturePlugin plugin, double from, double to) {
        super();
        mPlugin = plugin;
        mFrom = from;
        mTo = to;
        mDiff = mTo - mFrom;
    }

    @Override
    protected void applyTransformation() {
        double interpolation = Math.abs(mDiff * mInterpolatedTime) + Math.min(mFrom, mTo);
        if(mPlugin != null) mPlugin.setFactor(interpolation);
    }
}
