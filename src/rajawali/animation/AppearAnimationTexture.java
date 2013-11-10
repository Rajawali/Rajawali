package rajawali.animation;

public class AppearAnimationTexture extends AnimationTexture {

    public AppearAnimationTexture() {
        super();
    }

    @Override
    protected void applyTransformation() {
        mTexture.setInfluence((float) mInterpolatedTime);
    }
}

