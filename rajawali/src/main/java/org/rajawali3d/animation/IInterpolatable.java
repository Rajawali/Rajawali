package org.rajawali3d.animation;

import androidx.annotation.FloatRange;

public interface IInterpolatable {

    public void enableInterpolation(boolean value);
    public void interpolate(@FloatRange(from = 0.0d, to = 1.0d) double factor);

}
