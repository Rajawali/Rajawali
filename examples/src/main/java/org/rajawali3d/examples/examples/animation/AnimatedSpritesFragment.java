package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.examples.examples.animation.particles.ExampleParticleSystem2;

public class AnimatedSpritesFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new AnimatedSpritesRenderer(getActivity(), this);
	}

	private final class AnimatedSpritesRenderer extends AExampleRenderer {
		private final int MAX_FRAMES = 200;
		private int mFrameCount;
		private ExampleParticleSystem2 mParticleSystem;

		public AnimatedSpritesRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			getCurrentCamera().setPosition(0, 0, 10);
            // TODO add particle system
			/*
			// -- explosion sprite sheet from:
			// http://gushh.net/blog/free-game-sprites-explosion-3/
			mParticleSystem = new ExampleParticleSystem2();
			mParticleSystem.setPointSize(600);
			try {
				mParticleSystem.getMaterial().addTexture(
						new Texture(R.drawable.explosion_3_40_128));
			} catch (TextureException e) {
				e.printStackTrace();
			}
			addChild(mParticleSystem);*/
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			/*
			mParticleSystem.setCurrentFrame(mFrameCount);
			mParticleSystem.setTime((float) mFrameCount * .1f);

			if (mFrameCount++ >= MAX_FRAMES)
				mFrameCount = 0;*/
		}

	}

}
