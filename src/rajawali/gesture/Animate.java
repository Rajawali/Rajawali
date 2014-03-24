package rajawali.gesture;


public class Animate {

    protected float mStep;
    private AnimateListener mAnimateListener;
    private float mCurrentValue = -1.0F;
    private float mFromValue = -1.0F;
    private float mToValue = -1.0F;
    private boolean mbAnimateRunning = false;

    protected Animate() {
        this(0.0F, 0.0F, 50.0F);
    }

    public Animate(float fromValue, float toValue) {
        this(fromValue, toValue, 50.0F);
    }

    public Animate(float fromValue, float toValue, float step) {
        setFromValue(fromValue);
        setToValue(toValue);
        setStep(step);
    }

    private void notifyAnimateStarted() {
        if (mAnimateListener != null)
            mAnimateListener.AnimationStarted(this);
    }

    private void notifyAnimateEnded() {
        if (mAnimateListener != null)
            mAnimateListener.AnimationEnded(this);
    }

    private void notifyAnimateUpdated() {
        if (mAnimateListener != null)
            mAnimateListener.AnimationUpdated(this);
    }

    public void setAnimationListener(AnimateListener paramAnimateListener) {
        mAnimateListener = paramAnimateListener;
    }

    public float getCurrentValue() {
        return this.mCurrentValue;
    }

    public boolean isAnimationRunning() {
        return this.mbAnimateRunning;
    }


    void setFromValue(float paramFloat) {
        this.mFromValue = paramFloat;
    }

    void setToValue(float paramFloat) {
        this.mToValue = paramFloat;
    }

    void setStep(float step) {
        this.mStep = step;
    }

    public synchronized void startAnimation() {
        this.mbAnimateRunning = true;
        mCurrentValue = mFromValue;
        notifyAnimateStarted();
    }

    public synchronized void endAnimation() {
        this.mCurrentValue = this.mToValue;
        this.mbAnimateRunning = false;
        notifyAnimateEnded();
    }

    public synchronized void destroyAnimation(){
        endAnimation();
        mAnimateListener=null;
    }

    public void update() {
        if (mbAnimateRunning) {
            if (mFromValue <= mToValue) {
                if (mCurrentValue >= mToValue) {
                    endAnimation();
                }else {
                    this.mCurrentValue += mStep;
                }
            } else if (mFromValue >= mToValue) {
                if (mCurrentValue <= mToValue) {
                    endAnimation();
                } else {
                    this.mCurrentValue -= mStep;
                }
            }
            notifyAnimateUpdated();
        }
    }
}

