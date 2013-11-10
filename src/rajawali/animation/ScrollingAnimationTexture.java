package rajawali.animation;

import rajawali.materials.textures.ATexture;

public class ScrollingAnimationTexture extends AnimationTexture {

    public ScrollingAnimationTexture() {
        super();
    }

    @Override
    protected void applyTransformation() {
        mTexture.setOffsetU((float) mInterpolatedTime);
    }

    public void setTexture(ATexture texture) {
        super.setTexture(texture);
        mTexture.enableOffset(true);
        mTexture.setOffsetU(0f);
    }
}

