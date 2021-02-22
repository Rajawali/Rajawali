package org.rajawali3d.cameras;

import android.annotation.SuppressLint;
import androidx.annotation.FloatRange;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import static org.rajawali3d.math.MathUtil.clamp;

public class PanTiltZoomCamera extends Camera {
    Context mContext;
    ScaleGestureDetector mScaleDetector;
    View.OnTouchListener mGestureListener;
    GestureDetector mDetector;
    View mView;
    IConstraint mConstraints;
    double mStartFOV;

    interface IConstraint {
        boolean constrainPan(double panAngle, Quaternion orientation);
        boolean constrainTilt(double tiltAngle, Quaternion orientation);
        boolean constrainZoom(double fov, Quaternion orientation);
    }

    public PanTiltZoomCamera(Context context, View view) {
        this(context, view, new DefaultPTZconstraint());
    }

    public PanTiltZoomCamera(Context context, View view, IConstraint contraints) {
        super();
        mContext = context;
        mView = view;
        mConstraints = contraints;
        initialize();
        addListeners();
    }

    private void initialize() {
        mStartFOV = mFieldOfView;
    }

    public void setConstraints(IConstraint constraint) {
        mConstraints = (constraint instanceof IConstraint) ? constraint : new DefaultPTZconstraint();
    }

    public void setFieldOfView(double fieldOfView) {
        synchronized (mFrustumLock) {
            mStartFOV = fieldOfView;
            super.setFieldOfView(fieldOfView);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addListeners() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetector = new GestureDetector(mContext, new GestureListener());
                mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());

                mGestureListener = new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mScaleDetector.onTouchEvent(event);

                        if (!mScaleDetector.isInProgress()) {
                            mDetector.onTouchEvent(event);
                        }

                        return true;
                    }
                };
                mView.setOnTouchListener(mGestureListener);
            }
        });
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            disableLookAt();

            DisplayMetrics metrics = new DisplayMetrics();
            mView.getDisplay().getMetrics(metrics);

            double panAngle = (distanceX/metrics.widthPixels) * getFieldOfView();
            Quaternion pan = new Quaternion();
            if(!mConstraints.constrainPan(panAngle, getOrientation())) {
                pan.fromAngleAxis(mUpAxis, panAngle);
                pan.normalize();
            }

            Vector3 tiltAxis = Vector3.X.clone();
            tiltAxis.rotateY(-getOrientation().getRotationY());
            double tiltAngle = (distanceY/metrics.heightPixels) * getFieldOfView();
            Quaternion tilt = new Quaternion();
            if(!mConstraints.constrainTilt(tiltAngle, getOrientation())) {
                tilt.fromAngleAxis(tiltAxis, tiltAngle);
                tilt.normalize();
            }

            setOrientation(getOrientation().multiply(tilt).multiply(pan));
            return false;
        }
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            double fov = mStartFOV * (1.0 / detector.getScaleFactor());
            if(!mConstraints.constrainZoom(fov, getOrientation())) {
                setFieldOfView(fov);
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }

    static class DefaultPTZconstraint implements IConstraint {
        double mMaxTilt, mMinTilt;
        double mMaxFOV, mMinFOV;

        public DefaultPTZconstraint() {
            this(0.8, -0.8, 5, 1);
        }

        public DefaultPTZconstraint(@FloatRange(from = -1, to = 1) double maxTilt,
                                    @FloatRange(from = -1, to = 1) double minTilt,
                                    @FloatRange(from = 0.55, to = 100) double maxZoom,
                                    @FloatRange(from = 0.55, to = 100) double minZoom) {
            mMaxTilt = clamp(maxTilt,-1,1);
            mMinTilt = clamp(minTilt, -1, 1);
            mMaxFOV = clamp(100 / minZoom, 1, 180); // max FOV is min Zoom
            mMinFOV = clamp(100 / maxZoom, 1, 180); // min FOV is max Zoom
        }

        @Override
        public boolean constrainPan(double panAngle, Quaternion orientation) {
            return false;
        }

        @Override
        public boolean constrainTilt(double tiltAngle, Quaternion orientation) {
            double z = orientation.getYAxis().z;
            if((z>mMaxTilt) && (tiltAngle>0)) return true;
            if((z<mMinTilt) && (tiltAngle<0)) return true;
            return false;
        }

        @Override
        public boolean constrainZoom(double fov, Quaternion orientation) {
            if(fov > mMaxFOV) return true;
            if(fov < mMinFOV) return true;
            return false;
        }
    }
}

