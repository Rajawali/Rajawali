package org.rajawali3d.animation;

import static org.rajawali3d.math.MathUtil.clamp;

import android.renderscript.Matrix3f;

import androidx.annotation.NonNull;

import org.rajawali3d.animation.Animation;
import org.rajawali3d.materials.textures.ATexture;

public class SpriteSheetAnimation extends Animation {
    private final ATexture mTexture;
    private final Matrix3f mTransform = new Matrix3f();
    private int mCols = 1;
    private int mRows = 1;
    private int mStart = 0;
    private int mEnd = 1;
    private boolean mForward = true;

    /* assumes sprite sheet is a grid of frames,
       sequenced left to right, and top to bottom
     */
    public SpriteSheetAnimation(@NonNull ATexture texture, int numCols, int numRows) {
        super();
        texture.enableTransforms(true);
        mTexture = texture;
        if(numCols > 0) mCols = numCols;
        if(numRows > 0) mRows = numRows;
        mEnd = mCols * mRows -1;
    }

    /* selects range of frames to animate,
       if startFrame is greater than endFrame,
       sprite is animated in reverse
     */
    public void selectRange(int startFrame, int endFrame) {
        mStart = clamp(startFrame, 0, mCols*mRows-1);
        mEnd = clamp(endFrame, 0, mCols*mRows-1);
        mForward = mEnd > mStart;
    }

    @Override
    protected void applyTransformation() {
        double t = mForward ?
                Math.abs(mStart + mInterpolatedTime * (mEnd-mStart)) :
                Math.abs(mStart - mInterpolatedTime * (mStart-mEnd));

        mTransform.loadIdentity();
        mTransform.translate(selectCol(t)/mCols,selectRow(t)/mRows);
        mTransform.scale(1f/mCols,1f/mRows);
        mTexture.applyTransform(mTransform);
    }

    private float selectCol(double t) {
        return Math.round(t) % mCols;
    }

    private float selectRow(double t) {
        return Math.round(t) / mCols;
    }
}

