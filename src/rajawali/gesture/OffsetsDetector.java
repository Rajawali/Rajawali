package rajawali.gesture;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

public class OffsetsDetector {

    private static final String TAG = "OffsetsDetector";
    private final OnOffsetsListener mListener;
    Animate mSwipeAnim = null;
    private GestureThread mGestureThread;
    private int mMaximumFlingVelocity;
    private VelocityTracker mVelocityTracker;
    private boolean mAlwaysInTapRegion;
    private int mTouchSlopSquare;
    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;
    private float mTotalTouchOffsetX = -1.0F;
    private float xOffsetDefault = 0.5f;
    private float yOffsetDefault = 0.5f;
    private float yOffsetStepDefault = 1f;
    private int nbScreen = 4;
    private float xOffsetStepDefault = 1f / nbScreen;
    private int mScreenWidth;
    private boolean mManualThread;

    public OffsetsDetector(Context context, OnOffsetsListener listener) {
        this(context, listener, false);
    }

    public OffsetsDetector(Context context, OnOffsetsListener listener, boolean manualThread) {
        mListener = listener;
        mManualThread = manualThread;
        init(context);
    }

    private void init(Context context) {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        int touchSlop;

        if (context == null) {
            touchSlop = ViewConfiguration.getTouchSlop();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
        if (!mManualThread) {
            mGestureThread = new GestureThread();
            mGestureThread.start();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
    }

    public float getOffsetXCurrent() {
        return getViewOffset();
    }

    public Animate getSwipeAnimation() {
        return mSwipeAnim;
    }

    public void swipeAnimationUpdate() {
        if (mSwipeAnim != null)
            mSwipeAnim.update();
    }

    public float getXOffsetDefault() {
        return xOffsetDefault;
    }

    public void setXOffsetDefault(float xOffsetDefault) {
        this.xOffsetDefault = xOffsetDefault;
    }

    public float getYOffsetDefault() {
        return yOffsetDefault;
    }

    public void setYOffsetDefault(float yOffsetDefault) {
        this.yOffsetDefault = yOffsetDefault;
    }

    public float getYOffsetStepDefault() {
        return yOffsetStepDefault;
    }

    public void setYOffsetStepDefault(float yOffsetStepDefault) {
        this.yOffsetStepDefault = yOffsetStepDefault;
    }

    public float getXOffsetStepDefault() {
        return xOffsetStepDefault;
    }

    public void setxOffsetStepDefault(float xOffsetStepDefault) {
        this.xOffsetStepDefault = xOffsetStepDefault;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public void setScreenWidth(int width) {
        this.mScreenWidth = width;
        if (mTotalTouchOffsetX == -1)
            mTotalTouchOffsetX = width * nbScreen / 2f;
        mListener.onOffsetsChanged(xOffsetDefault, yOffsetDefault, xOffsetStepDefault, yOffsetStepDefault);

    }

    public int getScreens() {
        return nbScreen;
    }

    public void setScreens(int nbScreen) {
        this.nbScreen = nbScreen;
        xOffsetStepDefault = 1f / this.nbScreen;
    }

    public void onVisibilityChanged(boolean visible) {
        if (!mManualThread) {
            if (visible) {
                mGestureThread.resumeThread();
            } else {
                mGestureThread.pauseThread();
            }
        }
    }

    public void onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        final float focusX = event.getX(0);
        final float focusY = event.getY(0);

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                mAlwaysInTapRegion = true;

                break;
            case MotionEvent.ACTION_POINTER_UP:

                if (event.getActionIndex() == 0) {
                    mDownFocusX = mLastFocusX = event.getX(1);
                    mDownFocusY = mLastFocusY = event.getY(1);
                }
                break;
            case MotionEvent.ACTION_DOWN:

                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                mAlwaysInTapRegion = true;

                break;
            case MotionEvent.ACTION_MOVE:
                final float scrollX = mLastFocusX - focusX;
                final float scrollY = mLastFocusY - focusY;
                if (mAlwaysInTapRegion) {
                    final int deltaX = (int) (focusX - mDownFocusX);
                    final int deltaY = (int) (focusY - mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    if (distance > mTouchSlopSquare) {

                        mLastFocusX = focusX;
                        mLastFocusY = focusY;
                        mAlwaysInTapRegion = false;
                        mTotalTouchOffsetX += scrollX;
                        mListener.onOffsetsChanged(getViewOffset(), yOffsetDefault, xOffsetStepDefault, yOffsetStepDefault);
                    }

                } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {

                    mTotalTouchOffsetX += scrollX;
                    mListener.onOffsetsChanged(getViewOffset(), yOffsetDefault, xOffsetStepDefault, yOffsetStepDefault);
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
                }
                break;

            case (MotionEvent.ACTION_UP):

                if (!mAlwaysInTapRegion) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    final int pointerId = event.getPointerId(0);
                    velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    final float velocityX = velocityTracker.getXVelocity(pointerId);
                    onFling(mDownFocusX, event.getX(), velocityX);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;

        }
    }

    private void cancel() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mAlwaysInTapRegion = false;
    }

    private float getViewOffset() {
        if (mTotalTouchOffsetX < 0.0F)
            mTotalTouchOffsetX = 0.0F;
        if (mTotalTouchOffsetX > mScreenWidth * nbScreen)
            mTotalTouchOffsetX = mScreenWidth * nbScreen;
        return mTotalTouchOffsetX / (float) (mScreenWidth * nbScreen);
    }

    public void onFling(float scrollDownX, Float scrollUpX, float velocityX) {

        if ((scrollDownX - scrollUpX > 25.0F) && (Math.abs(velocityX) > 500.0F)) {
            animationRightScreen();
        } else if ((scrollDownX - scrollUpX > mScreenWidth * 0.4f)) {
            animationRightScreen();

        } else if ((scrollDownX - scrollUpX > 0.0F)) {
            animationLeftScreen();
        } else if ((scrollUpX - scrollDownX > 25.0F) && (Math.abs(velocityX) > 500.0F) && (mTotalTouchOffsetX > 0.0F)) {

            animationLeftScreen();
        } else if ((scrollUpX - scrollDownX > mScreenWidth * 0.4f)) {
            animationLeftScreen();
        } else if ((scrollUpX - scrollDownX > 0.0F)) {
            animationRightScreen();

        }
    }

    private float nextScreen(float mTotalTouchOffsetX) {
        int nb = nbScreen;
        while (nb >= 0) {
            if (mTotalTouchOffsetX <= mScreenWidth * nb && mTotalTouchOffsetX > mScreenWidth * (nb - 1)) {
                break;
            }
            nb--;
        }
        return mScreenWidth * (nb);
    }

    private float beforeScreen(float mTotalTouchOffsetX) {
        int nb = nbScreen;
        while (nb >= 0) {
            if (mTotalTouchOffsetX <= mScreenWidth * nb && mTotalTouchOffsetX > mScreenWidth * (nb - 1)) {
                break;
            }
            nb--;
        }
        return mScreenWidth * (nb - 1);
    }

    private void animationRightScreen() {
        if (mTotalTouchOffsetX < mScreenWidth * nbScreen) {

            if (mSwipeAnim != null) {
                mSwipeAnim.destroyAnimation();
            }
            mSwipeAnim = new Animate(mTotalTouchOffsetX, nextScreen(mTotalTouchOffsetX));
            mSwipeAnim.setAnimationListener(new AnimateListener() {
                public void AnimationEnded(Animate paramAnonymousAnimate) {

                }

                public void AnimationStarted(Animate paramAnonymousAnimate) {
                }

                public void AnimationUpdated(Animate paramAnonymousAnimate) {
                    mTotalTouchOffsetX = paramAnonymousAnimate.getCurrentValue();
                    mListener.onOffsetsChanged(getViewOffset(), yOffsetDefault, xOffsetStepDefault, yOffsetStepDefault);
                }
            });
            mSwipeAnim.startAnimation();
        }
    }

    private void animationLeftScreen() {

        if (mTotalTouchOffsetX > 0) {
            if (mSwipeAnim != null) {
                mSwipeAnim.destroyAnimation();
            }
            mSwipeAnim = new Animate(mTotalTouchOffsetX, beforeScreen(mTotalTouchOffsetX));
            mSwipeAnim.setAnimationListener(new AnimateListener() {
                public void AnimationEnded(Animate paramAnonymousAnimate) {
                }

                public void AnimationStarted(Animate paramAnonymousAnimate) {
                }

                public void AnimationUpdated(Animate paramAnonymousAnimate) {
                    mTotalTouchOffsetX = paramAnonymousAnimate.getCurrentValue();
                    mListener.onOffsetsChanged(getViewOffset(), yOffsetDefault, xOffsetStepDefault, yOffsetStepDefault);
                }
            });
            mSwipeAnim.startAnimation();
        }
    }

    public void onDestroy() {
        if (!mManualThread) {
            mGestureThread.stopThread();
            joinThread(mGestureThread);
            mGestureThread = null;
        }
    }

    private void joinThread(Thread thread) {
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }

    }

    public interface OnOffsetsListener {
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep);
    }

    class GestureThread extends Thread {
        private Object pauseLock = new Object();

        private boolean running = true;
        private boolean paused = true;

        private int fps = 30;
        private int timeFrame = 1000 / fps; // drawing time frame in miliseconds 1000 ms / fps

        @Override
        public void run() {

            while (running) {

                waitOnPause();

                if (!running) {
                    return;
                }
                long beforeDrawTime = System.currentTimeMillis();
                swipeAnimationUpdate();
                long afterDrawTime = System.currentTimeMillis() - beforeDrawTime;
                try {
                    if (timeFrame > afterDrawTime) {
                        Thread.sleep(timeFrame - afterDrawTime);
                    }
                } catch (InterruptedException ex) {
                    Log.e(TAG, "Exception during Thread.sleep().", ex);
                }

            }

        }

        public void stopThread() {
            synchronized (pauseLock) {
                paused = false;
                running = false;
                pauseLock.notifyAll();
            }
            Log.d(TAG, "Stopped thread (" + this.getId() + ")");
        }

        public void pauseThread() {
            synchronized (pauseLock) {
                paused = true;
            }
            Log.d(TAG, "Paused thread (" + this.getId() + ")");
        }

        public void resumeThread() {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll();
            }
            Log.d(TAG, "Resumed thread (" + this.getId() + ")");
        }

        private void waitOnPause() {
            synchronized (pauseLock) {
                while (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
