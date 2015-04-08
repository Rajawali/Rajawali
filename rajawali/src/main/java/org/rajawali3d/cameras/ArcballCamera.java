package org.rajawali3d.cameras;

import android.app.Activity;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector2;
import org.rajawali3d.math.vector.Vector3;

/**
 *
 * @author dennis.ippel
 */
public class ArcballCamera extends Camera {
    private Context mContext;
    private ScaleGestureDetector mScaleDetector;
    private View.OnTouchListener mGestureListener;
    private GestureDetector mDetector;
    private View mView;
    private boolean mIsRotating;
    private boolean mIsScaling;
    private Vector3 mCameraStartPos;
    private Vector3 mPrevSphereCoord;
    private Vector3 mCurrSphereCoord;
    private Vector2 mPrevScreenCoord;
    private Vector2 mCurrScreenCoord;
    private Quaternion mStartOrientation;
    private Quaternion mCurrentOrientation;
    private Object3D mEmpty;
    private Object3D mTarget;
    private Matrix4 mScratchMatrix;
    private Vector3 mScratchVector;
    private double mStartFOV;

    public ArcballCamera(Context context, View view) {
        this(context, view, null);
    }

    public ArcballCamera(Context context, View view, Object3D target) {
        super();
        mContext = context;
        mTarget = target;
        mView = view;
        initialize();
        addListeners();
    }

    private void initialize() {
        mStartFOV = mFieldOfView;
        mLookAtEnabled = true;
        setLookAt(0, 0, 0);
        mEmpty = new Object3D();
        mScratchMatrix = new Matrix4();
        mScratchVector = new Vector3();
        mCameraStartPos = new Vector3();
        mPrevSphereCoord = new Vector3();
        mCurrSphereCoord = new Vector3();
        mPrevScreenCoord = new Vector2();
        mCurrScreenCoord = new Vector2();
        mStartOrientation = new Quaternion();
        mCurrentOrientation = new Quaternion();
    }

    @Override
    public void setProjectionMatrix(int width, int height) {
        super.setProjectionMatrix(width, height);
    }

    private void mapToSphere(final float x, final float y, Vector3 out)
    {
        float lengthSquared = x * x + y * y;
        if (lengthSquared > 1)
        {
            out.setAll(x, y, 0);
            out.normalize();
        }
        else
        {
            out.setAll(x, y, Math.sqrt(1 - lengthSquared));
        }
    }

    private void mapToScreen(final float x, final float y, Vector2 out)
    {
        out.setX((2 * x - mLastWidth) / mLastWidth);
        out.setY(-(2 * y - mLastHeight) / mLastHeight);
    }

    private void startRotation(final float x, final float y)
    {
        mapToScreen(x, y, mPrevScreenCoord);

        mCurrScreenCoord.setAll(mPrevScreenCoord.getX(), mPrevScreenCoord.getY());

        mIsRotating = true;
    }

    private void updateRotation(final float x, final float y)
    {
        mapToScreen(x, y, mCurrScreenCoord);

        applyRotation();
    }

    private void endRotation()
    {
        mStartOrientation.multiply(mCurrentOrientation);
    }

    private void applyRotation()
    {
        if (mIsRotating)
        {
            mapToSphere((float) mPrevScreenCoord.getX(), (float) mPrevScreenCoord.getY(), mPrevSphereCoord);
            mapToSphere((float) mCurrScreenCoord.getX(), (float) mCurrScreenCoord.getY(), mCurrSphereCoord);

            Vector3 rotationAxis = mPrevSphereCoord.clone();
            rotationAxis.cross(mCurrSphereCoord);
            rotationAxis.normalize();

            double rotationAngle = Math.acos(Math.min(1, mPrevSphereCoord.dot(mCurrSphereCoord)));
            mCurrentOrientation.fromAngleAxis(rotationAxis, MathUtil.radiansToDegrees(rotationAngle));
            mCurrentOrientation.normalize();

            Quaternion q = new Quaternion(mStartOrientation);
            q.multiply(mCurrentOrientation);

            mEmpty.setOrientation(q);
        }
    }

    @Override
    public Matrix4 getViewMatrix() {
        Matrix4 m = super.getViewMatrix();

        if(mTarget != null) {
            mScratchMatrix.identity();
            mScratchMatrix.translate(mTarget.getPosition());
            m.multiply(mScratchMatrix);
        }

        mScratchMatrix.identity();
        mScratchMatrix.rotate(mEmpty.getOrientation());
        m.multiply(mScratchMatrix);

        if(mTarget != null) {
            mScratchVector.setAll(mTarget.getPosition());
            mScratchVector.inverse();

            mScratchMatrix.identity();
            mScratchMatrix.translate(mScratchVector);
            m.multiply(mScratchMatrix);
        }

        return m;
    }

    public void setFieldOfView(double fieldOfView) {
        synchronized (mFrustumLock) {
            mStartFOV = fieldOfView;
            super.setFieldOfView(fieldOfView);
        }
    }

    private void addListeners() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetector = new GestureDetector(mContext, new GestureListener());
                mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());

                mGestureListener = new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        mScaleDetector.onTouchEvent(event);

                        if (!mIsScaling) {
                            mDetector.onTouchEvent(event);

                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (mIsRotating) {
                                    endRotation();
                                    mIsRotating = false;
                                }
                            }
                        }

                        return true;
                    }
                };
                mView.setOnTouchListener(mGestureListener);
            }
        });
    }

    public void setTarget(Object3D target) {
        mTarget = target;
        setLookAt(mTarget.getPosition());
    }

    public Object3D getTarget() {
        return mTarget;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            if(!mIsRotating) {
                startRotation(event2.getX(), event2.getY());
                return false;
            }
            mIsRotating = true;
            updateRotation(event2.getX(), event2.getY());
            return false;
        }
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            double fov = Math.max(30, Math.min(100, mStartFOV * (1.0 / detector.getScaleFactor())));
            setFieldOfView(fov);
            return true;
        }

        @Override
        public boolean onScaleBegin (ScaleGestureDetector detector) {
            mIsScaling = true;
            mIsRotating = false;
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd (ScaleGestureDetector detector) {
            mIsRotating = false;
            mIsScaling = false;
        }
    }
}
