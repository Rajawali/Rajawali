package rajawali.gesture;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

public class OffsetsDetector {
    public interface OnOffsetsListener {
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep);
    }
    private static final String TAG = "OffsetsDetector";
    private final OnOffsetsListener mListener;
    Animate mSwipeAnim = null;
    private GestureThread mGestureThread;
    private int mMaximumFlingVelocity;
    private VelocityTracker mVelocityTracker;
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mCurrentDeltaEvent;


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
        if (context == null) {
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
        if (!mManualThread) {
            mGestureThread = new GestureThread();
            mGestureThread.start();
        }
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
        final int count = event.getPointerCount();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_UP:


                // Check the dot product of current velocities.
                // If the pointer that left was opposing another velocity vector, clear.
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final int upIndex = event.getActionIndex();
                final int id1 = event.getPointerId(upIndex);
                final float x1 = mVelocityTracker.getXVelocity(id1);
                final float y1 = mVelocityTracker.getYVelocity(id1);
                for (int i = 0; i < count; i++) {
                    if (i == upIndex) continue;

                    final int id2 = event.getPointerId(i);
                    final float x = x1 * mVelocityTracker.getXVelocity(id2);
                    final float y = y1 * mVelocityTracker.getYVelocity(id2);

                    final float dot = x + y;
                    if (dot < 0) {
                        mVelocityTracker.clear();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                if (mCurrentDownEvent != null) {
                    mCurrentDownEvent.recycle();
                }
                if (mCurrentDeltaEvent != null) {
                    mCurrentDeltaEvent.recycle();
                }
                mCurrentDownEvent = MotionEvent.obtain(event);
                mCurrentDeltaEvent = MotionEvent.obtain(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mTotalTouchOffsetX += mCurrentDeltaEvent.getX() - event.getX();
                if (mCurrentDeltaEvent != null) {
                    mCurrentDeltaEvent.recycle();
                }
                mCurrentDeltaEvent = MotionEvent.obtain(event);

                mListener.onOffsetsChanged(getViewOffset(), yOffsetDefault, xOffsetStepDefault, yOffsetStepDefault);
                break;

            case (MotionEvent.ACTION_UP):


                // A fling must travel the minimum tap distance
                final VelocityTracker velocityTracker = mVelocityTracker;
                final int pointerId = event.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity(pointerId);
                final float velocityX = velocityTracker.getXVelocity(pointerId);


                onFling(mCurrentDownEvent, event, velocityX, velocityY);

                if (mVelocityTracker != null) {
                    // This may have been cleared when we called out to the
                    // application above.
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;


        }


    }

    private float getViewOffset() {
        //Log.e("TEST","mTotalTouchOffsetX "+mTotalTouchOffsetX);
        if (mTotalTouchOffsetX < 0.0F)
            mTotalTouchOffsetX = 0.0F;
        if (mTotalTouchOffsetX > mScreenWidth * nbScreen)
            mTotalTouchOffsetX = mScreenWidth * nbScreen;
        return mTotalTouchOffsetX / (float) (mScreenWidth * nbScreen);
    }

    public void onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
        if (Math.abs(paramMotionEvent1.getY() - paramMotionEvent2.getY()) > 250.0F)
            return;
        if ((paramMotionEvent1.getX() - paramMotionEvent2.getX() > 25.0F) && (Math.abs(paramFloat1) > 500.0F)) {

            animationRightScreen();

        } else if ((paramMotionEvent1.getX() - paramMotionEvent2.getX() > mScreenWidth * 0.4f)) {
            animationRightScreen();

        } else if ((paramMotionEvent1.getX() - paramMotionEvent2.getX() > 0.0F)) {
            animationLeftScreen();
        } else if ((paramMotionEvent2.getX() - paramMotionEvent1.getX() > 25.0F) && (Math.abs(paramFloat1) > 500.0F) && (mTotalTouchOffsetX > 0.0F)) {

            animationLeftScreen();
        } else if ((paramMotionEvent2.getX() - paramMotionEvent1.getX() > mScreenWidth * 0.4f)) {
            animationLeftScreen();
        } else if ((paramMotionEvent2.getX() - paramMotionEvent1.getX() > 0.0F)) {
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
                mSwipeAnim.endAnimation();
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
        if (mSwipeAnim != null) {
            mSwipeAnim.endAnimation();
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
