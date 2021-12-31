package org.rajawali3d.cameras;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.vector.Vector3;

/* Implements a camera that responds to Scroll and Scale Gestures by orbiting a point of interest

   - drag gestures move the camera about a sphere around the point of interest
   - scale gestures move the camera towards or away from the point of interest
   - camera remains oriented to UpAxis, so smooth movement degrades at poles
   - ICameraConstraint allows parametric limits to movement, typically to avoid poles
   - override setFrame() and setLocation() for alternative camera behavior
 */
public class OrbitalCamera extends Camera {
    Context mContext;
    View mView;
    GestureDetector mPositionDetector;
    ScaleGestureDetector mScaleDetector;
    ICameraConstraint mConstraint;

    public OrbitalCamera(Context context, View view) {
        this(context, view, null
        );
    }

    public OrbitalCamera(Context context, View view, ICameraConstraint constraint) {
        super();
        mContext = context;
        mView = view;
        mConstraint = constraint;
        initialize();
    }

    void initialize() {
        mPositionDetector = new GestureDetector(mContext, new ScrollListener());
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());

        mView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getPointerCount()>1) {
                    return mScaleDetector.onTouchEvent(motionEvent);
                } else {
                    return mPositionDetector.onTouchEvent(motionEvent);
                }
            }
        });
    }

    void setConstraint(ICameraConstraint constraint) { mConstraint = constraint; }

    void setFrame(double factor) {
        double r = mPosition.distanceTo(getLookAt());
        if(r < 1e-8) { return; }

        Vector3 v = Vector3.subtractAndCreate(mPosition, mLookAt);
        v.divide(factor);

        if(mConstraint != null && !mConstraint.validateRadius(v)) { return; }
        mPosition.setAll(v.add(mLookAt));
    }

    void setLocation(double longitude, double latitude) {
        double r = mPosition.distanceTo(getLookAt());
        if(r < 1e-8) { return; }

        Vector3 d = Vector3.subtractAndCreate(mPosition, mLookAt);
        d.rotateBy(getOrientation());
        d.rotateX(longitude);
        d.rotateY(latitude);
        d.rotateBy(getOrientation().invertAndCreate());
        if(mConstraint != null && !mConstraint.validatePosition(d, mUpAxis)) { return; }

        mPosition.setAll(d.add(mLookAt));
        resetToLookAt();
    }

    interface ICameraConstraint {
        boolean validatePosition(Vector3 pos, Vector3 mUpAxis);
        boolean validateRadius(Vector3 radius);
    }

    static class PedestalDollyConstraint implements ICameraConstraint {
        double minTilt, maxTilt;
        double minRadius, maxRadius;

        PedestalDollyConstraint(double minTilt, double maxTilt, double minRadius, double maxRadius) {
            this.minTilt = minTilt;
            this.maxTilt = maxTilt;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
        }

        @Override
        public boolean validatePosition(Vector3 pos, Vector3 mUpAxis) {
            double declination = pos.angle(mUpAxis);
            return (declination > minTilt && declination < maxTilt);
        }

        @Override
        public boolean validateRadius(Vector3 radius) {
            return (radius.length() > minRadius && radius.length() < maxRadius);
        }
    }

    class ScrollListener extends GestureDetector.SimpleOnGestureListener {
        DisplayMetrics metrics = new DisplayMetrics();

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            super.onScroll(e1, e2, distanceX, distanceY);
            mView.getDisplay().getMetrics(metrics);
            double r = mPosition.distanceTo(getLookAt());
            double longitude = Math.asin((2 * Math.PI * distanceY / metrics.heightPixels)/r);
            double latitude = Math.asin((2 * Math.PI * distanceX / metrics.widthPixels)/r);
            setLocation(longitude, latitude);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            super.onDown(e);
            return true;
        }
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            super.onScale(detector);
            setFrame(detector.getScaleFactor());
            return true;
        }
    }
}
