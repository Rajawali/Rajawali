package rajawali.animation;

import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.Texture;

public class SwitchAnimationTexture extends AnimationTexture {

    protected ATexture mTextureBis;

    public SwitchAnimationTexture() {
        super();
    }

    public void play() {
        super.play();
    }

    @Override
    protected void applyTransformation() {
        mTexture.setInfluence(1.0f - (float) mInterpolatedTime);
        mTextureBis.setInfluence((float) mInterpolatedTime);
    }

    public void setTextures(Texture[] textures) {
        mTexture = textures[0];
        mTextureBis = textures[1];
    }

    public void setTextures(ATexture textureFirst, ATexture textureSecond) {
        mTexture = textureFirst;
        mTextureBis = textureSecond;
    }

    public void pause() {
        super.pause();
        mTexture.setInfluence(1);
        mTextureBis.setInfluence(0);
    }

    @Override
    public void reset() {
        super.reset();
    }
}

