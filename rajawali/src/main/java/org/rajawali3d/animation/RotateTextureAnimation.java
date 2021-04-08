package org.rajawali3d.animation;

import androidx.annotation.NonNull;
import androidx.annotation.FloatRange;

import org.rajawali3d.animation.Animation;
import org.rajawali3d.materials.textures.ATexture;

import static org.rajawali3d.math.MathUtil.clamp;

public class RotateTextureAnimation extends Animation {
    final ATexture mTexture;
    final float mDiffInterpolatedValue;
    final float mFromInterpolatedValue;
    final float mToInterpolatedValue;

    public RotateTextureAnimation(@NonNull ATexture texture,
                                  @FloatRange(from = 0, to = 360f) float fromDegrees,
                                  @FloatRange(from = 0, to = 360f) float toDegrees) {
        super();
        mTexture = texture;
        mFromInterpolatedValue = clamp(fromDegrees, 0, 360f);
        mToInterpolatedValue = clamp(toDegrees, 0, 360f);
        mDiffInterpolatedValue = mToInterpolatedValue - mFromInterpolatedValue;
    }

    @Override
    protected void applyTransformation() {
        float interpolation = (float) Math.abs(mDiffInterpolatedValue * mInterpolatedTime);
        mTexture.setRotation(clamp(interpolation, 0, 360f));
    }
}
