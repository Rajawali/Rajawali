package org.rajawali3d.examples.examples.materials;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;

public class VideoTextureFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new VideoTextureRenderer(getActivity(), this);
	}

	private final class VideoTextureRenderer extends AExampleRenderer {
		private MediaPlayer mMediaPlayer;
		private StreamingTexture mVideoTexture;

		public VideoTextureRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			PointLight pointLight = new PointLight();
			pointLight.setPower(1);
			pointLight.setPosition(-1, 1, 4);

			getCurrentScene().addLight(pointLight);
			getCurrentScene().setBackgroundColor(0xff040404);

			try {
				Object3D android = new Cube(2.0f);
				Material material = new Material();
				material.enableLighting(true);
				material.setDiffuseMethod(new DiffuseMethod.Lambert());
				material.setSpecularMethod(new SpecularMethod.Phong());
				android.setMaterial(material);
				android.setColor(0xff99C224);
				//getCurrentScene().addChild(android);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}

			mMediaPlayer = MediaPlayer.create(getContext(),
											  R.raw.sintel_trailer_480p);
			mMediaPlayer.setLooping(true);

			mVideoTexture = new StreamingTexture("sintelTrailer", mMediaPlayer);
			Material material = new Material();
			material.setColorInfluence(0);
			try {
				material.addTexture(mVideoTexture);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			Plane screen = new Plane(3, 2, 2, 2, Vector3.Axis.Z);
			screen.setMaterial(material);
			screen.setX(.1f);
			screen.setY(-.2f);
			screen.setZ(1.5f);
			getCurrentScene().addChild(screen);

            getCurrentCamera().enableLookAt();
			getCurrentCamera().setLookAt(0, 0, 0);

			// -- animate the spot light

			TranslateAnimation3D lightAnim = new TranslateAnimation3D(
					new Vector3(-3, 3, 10), // from
					new Vector3(3, 1, 3)); // to
			lightAnim.setDurationMilliseconds(5000);
			lightAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			lightAnim.setTransformable3D(pointLight);
			lightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
			getCurrentScene().registerAnimation(lightAnim);
			lightAnim.play();

			// -- animate the camera

			EllipticalOrbitAnimation3D camAnim = new EllipticalOrbitAnimation3D(
					new Vector3(3, 2, 10), new Vector3(1, 0, 8), 0, 359);
			camAnim.setDurationMilliseconds(20000);
			camAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
			camAnim.setTransformable3D(getCurrentCamera());
			getCurrentScene().registerAnimation(camAnim);
			camAnim.play();

			mMediaPlayer.start();
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			mVideoTexture.update();
		}

        @Override
        public void onPause() {
            super.onPause();
            if (mMediaPlayer != null)
                mMediaPlayer.pause();
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mMediaPlayer != null)
                mMediaPlayer.start();
        }

        @Override
		public void onRenderSurfaceDestroyed(SurfaceTexture surfaceTexture) {
			super.onRenderSurfaceDestroyed(surfaceTexture);
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}

	}

}
