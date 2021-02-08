package org.rajawali3d.curves;

import android.graphics.Color;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.rajawali3d.math.MathUtil.clamp;

public class ColorPath1D implements ICurve1D {

    protected List<Integer> mPoints;
    protected int mNumPoints;
    protected boolean mIsClosed;

    protected float[] mAddedColor = new float[3];
    protected float[] mFromColor = new float[3];
    protected float[] mMultipliedColor = new float[3];
    protected float[] mToColor = new float[3];
    protected float[] mDiffColor;
    protected int mDiffAlpha;
    protected int mFromAlpha;
    protected int mToAlpha;
    protected int mMultipliedAlpha;

    public ColorPath1D() {
        mPoints = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    }

    public void addPoint(int point) {
        mPoints.add(point);
        mNumPoints++;
    }

    public int getNumPoints()
    {
        return mNumPoints;
    }

    public List<Integer> getPoints()
    {
        return mPoints;
    }

    public int getPoint(int index) {
        return mPoints.get(index);
    }

    int getNumTransitions() {
        return mIsClosed ? getNumPoints() : getNumPoints()-1;
    }


    // mix performs a linear interpolation between x and y using a to weight between them.
    // The return value is computed as x×(1−a)+y×a.
    private int mix(int fromColor, int toColor, double tween) {
        double a = clamp(tween,0,1);

        Color.colorToHSV(fromColor, mFromColor);
        Color.colorToHSV(toColor, mToColor);

        mFromAlpha = fromColor >>> 24;
        mToAlpha = toColor >>> 24;

        mDiffColor = new float[3];
        mDiffColor[0] = mToColor[0] - mFromColor[0];
        mDiffColor[1] = mToColor[1] - mFromColor[1];
        mDiffColor[2] = mToColor[2] - mFromColor[2];

        mDiffAlpha = mToAlpha - mFromAlpha;

        mMultipliedColor[0] = mDiffColor[0] * (float) a;
        mMultipliedColor[1] = mDiffColor[1] * (float) a;
        mMultipliedColor[2] = mDiffColor[2] * (float) a;
        mMultipliedAlpha = (int) (mDiffAlpha * (float) a);

        mAddedColor[0] = mFromColor[0] + mMultipliedColor[0];
        mAddedColor[1] = mFromColor[1] + mMultipliedColor[1];
        mAddedColor[2] = mFromColor[2] + mMultipliedColor[2];

        return Color.HSVToColor(mMultipliedAlpha + mFromAlpha, mAddedColor);
    }

    public int calculatePoint(double t) {
        while(t < 0) t+=1;
        while(t > 1) t-=1;

        int result;
        int prev = (int)Math.floor(t*getNumTransitions());
        int next = prev+1;
        double tween = (t*getNumTransitions()-prev);
        if(next < getNumPoints()) {
            result = mix(getPoint(prev), getPoint(next), tween);
        } else {
            if(mIsClosed) {
                result = mix(getPoint(getNumPoints()-1), getPoint(0), tween);
            } else {
                result = getPoint(getNumPoints()-1);
            }
        }
        return result;
    }

    public void isClosedCurve(boolean closed)
    {
        mIsClosed = closed;
    }

    public boolean isClosedCurve()
    {
        return mIsClosed;
    }

}
