package org.rajawali3d.animation;

import org.rajawali3d.math.vector.Vector3;

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class Keyframes3D implements IKeyframes<Double, Vector3[]> {

    protected NavigableMap<Double, Vector3[]> mPoints;
    protected boolean mIsClosed;

    public Keyframes3D() {
        mPoints = new ConcurrentSkipListMap<>();
    }

    public void addPoint(double k, Vector3[] v) {
        mPoints.put(k,v);
    }

    public int getNumPoints()
    {
        return mPoints.size();
    }

    public Map.Entry<Double, Vector3[]> getCeilingFrame(double key) {
        return mPoints.ceilingEntry(key);
    }

    public Map.Entry<Double, Vector3[]> getFloorFrame(double key) {
        return mPoints.floorEntry(key);
    }

    // mix performs a linear interpolation between x and y using a to weight between them.
    // The return value is computed as x×(1−a)+y×a.
    private Vector3[] mix(Vector3[] x, Vector3[] y, double a) {
        if(x.length < 1) return null;
        if(x.length != y.length) return null;
        Vector3[] result = new Vector3[x.length];
        for(int i=0; i<x.length; i++) {
            result[i] = x[i].clone().multiply(1-a);
            result[i].add(y[i].clone().multiply(a));
            result[i].normalize();
        }
        return result;
    }

    int getNumTransitions() {
        return mIsClosed ? getNumPoints() : getNumPoints()-1;
    }

    @Override
    public Vector3[] calculatePoint(Double t) {
        while(t < 0) t+=1;
        while(t > 1) t-=1;

        int prev = (int)Math.floor(t*getNumTransitions());
        int next = prev+1;
        Vector3[] result;
        double tween = t*getNumTransitions()-prev;
        if(next < getNumPoints()) {
            result = mix(getFloorFrame(t).getValue(), getCeilingFrame(t).getValue(), tween);
        } else {
            if(mIsClosed) {
                result = mix(getFloorFrame(1).getValue(), getCeilingFrame(0).getValue(), tween);
            } else {
                result = getCeilingFrame(1).getValue();
            }
        }
        return result;
    }
}
