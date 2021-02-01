package org.rajawali3d.curves;

import org.rajawali3d.math.Quaternion;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Path4D implements ICurve4D {

    protected List<Quaternion> mPoints;
    protected int mNumPoints;
    protected boolean mIsClosed;

    public Path4D() {
        mPoints = Collections.synchronizedList(new CopyOnWriteArrayList<Quaternion>());
    }

    public void addPoint(Quaternion point) {
        mPoints.add(point);
        mNumPoints++;
    }

    public int getNumPoints()
    {
        return mNumPoints;
    }

    public List<Quaternion> getPoints()
    {
        return mPoints;
    }

    public Quaternion getPoint(int index) {
        return mPoints.get(index);
    }

    public void calculatePoint(Quaternion result, double t) {
        while(t < 0) t+=1;
        while(t > 1) t-=1;

        int prev = (int)Math.floor(t*getNumPoints());
        int next = prev+1;
        double tween = (t*getNumPoints()-prev);
        if(next < getNumPoints()) {
            result.slerp(getPoint(prev), getPoint(next), tween);
        } else {
            if(mIsClosed) {
                result.slerp(getPoint(getNumPoints()-1), getPoint(0), tween);
            } else {
                result.setAll(getPoint(getNumPoints()-1));
            }
        }
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

