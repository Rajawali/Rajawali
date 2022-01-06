package org.rajawali3d.examples.examples.materials;

import android.content.Context;
import androidx.annotation.Nullable;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.AnimatedGIFTexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;

public class AnimatedGIFTextureFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new AnimatedGIFTextureRenderer(getActivity(), this);
	}

	private final class AnimatedGIFTextureRenderer extends AExampleRenderer {

		private AnimatedGIFTexture mGifTexture;

		public AnimatedGIFTextureRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			final Material material = new Material();
			final Plane plane = new Plane(1, 1, 1, 1);
			plane.setMaterial(material);
			getCurrentScene().addChild(plane);

			try {
				mGifTexture = new AnimatedGIFTexture("animGif", R.drawable.animated_gif);
				material.addTexture(mGifTexture);
				material.setColorInfluence(0);
				mGifTexture.rewind();
				plane.setScaleX((float) mGifTexture.getWidth() / (float) mGifTexture.getHeight());
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			final EllipticalOrbitAnimation3D anim = new EllipticalOrbitAnimation3D(new Vector3(0, 0, 3), new Vector3(
					0.5, 0.5, 3), 0, 359);
			anim.setDurationMilliseconds(12000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setTransformable3D(getCurrentCamera());
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

        @Override
        protected void onRender(long elapsedRealtime, double deltaTime) {
            super.onRender(elapsedRealtime, deltaTime);
			if (mGifTexture != null) {
				try {
					mGifTexture.update();
				} catch (ATexture.TextureException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
