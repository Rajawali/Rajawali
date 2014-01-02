package rajawali.animation;

public class DisappearAnimationTexture extends AnimationTexture {

    public DisappearAnimationTexture() {
        super();
    }

    @Override
    protected void applyTransformation() {
        mTexture.setInfluence(1.0f - (float) mInterpolatedTime);
    }
}

