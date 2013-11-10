package rajawali.animation;

import rajawali.materials.textures.ATexture;

public class ScrollingAnimationTexture extends AnimationTexture {

    public enum DirectionMode {
        RIGHT_TO_LEFT,LEFT_TO_RIGHT;
    }

    protected DirectionMode mDirection = DirectionMode.RIGHT_TO_LEFT;

    public ScrollingAnimationTexture() {
        super();
    }

    @Override
    protected void applyTransformation() {
        switch (mDirection){
            case RIGHT_TO_LEFT:
                mTexture.setOffsetU((float) mInterpolatedTime);
                break;
            case LEFT_TO_RIGHT:
                mTexture.setOffsetU(1.0f - (float) mInterpolatedTime);
                break;
        }

    }

    public void setTexture(ATexture texture) {
        super.setTexture(texture);
        mTexture.enableOffset(true);
        mTexture.setOffsetU(0f);
    }

    public void setDirectionMode(DirectionMode direction){ mDirection = direction;}
}

