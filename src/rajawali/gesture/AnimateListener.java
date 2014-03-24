package rajawali.gesture;

public abstract interface AnimateListener
{
    public abstract void AnimationEnded(Animate paramAnimate);

    public abstract void AnimationStarted(Animate paramAnimate);

    public abstract void AnimationUpdated(Animate paramAnimate);
}
