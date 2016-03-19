package org.rajawali3d.examples.examples.postprocessing;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.passes.RenderPass;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.scene.Scene;

import java.util.Random;

public class RenderToTextureFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new RenderToTextureRenderer(getActivity(), this);
	}

	private final class RenderToTextureRenderer extends AExampleRenderer {
		private PostProcessingManager mEffects;
		private Scene                 mOtherScene;
		private Object3D              mSphere;
		private ATexture              mCurrentTexture;

		public RenderToTextureRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {

			//
			// -- Create the scene that we are going to use for
			//    off-screen rendering
			//

			DirectionalLight light = new DirectionalLight();
            light.setLookAt(0, 0, -1);
            light.enableLookAt();
			light.setPower(1);
			getCurrentScene().setBackgroundColor(0xdfae74);
			getCurrentScene().addLight(light);

			Material material = new Material();
			material.enableLighting(true);
			material.setDiffuseMethod(new DiffuseMethod.Lambert());

			getCurrentCamera().setZ(10);

			Random random = new Random();

			for (int i = 0; i < 20; i++) {
				Cube cube = new Cube(1);
				cube.setPosition(-5 + random.nextFloat() * 10,
						-5 + random.nextFloat() * 10, random.nextFloat() * -10);
				cube.setMaterial(material);
				cube.setColor(0x666666 + random.nextInt(0x999999));
				getCurrentScene().addChild(cube);

				Vector3 randomAxis = new Vector3(random.nextFloat(),
						random.nextFloat(), random.nextFloat());
				randomAxis.normalize();

				RotateOnAxisAnimation anim = new RotateOnAxisAnimation(randomAxis,
						360);
				anim.setTransformable3D(cube);
				anim.setDurationMilliseconds(3000 + (int) (random.nextDouble() * 5000));
				anim.setRepeatMode(Animation.RepeatMode.INFINITE);
				getCurrentScene().registerAnimation(anim);
				anim.play();
			}

			//
			// -- Create the scene that will contain an object
			//    that uses the rendered to texture
			//

			mOtherScene = new Scene(this);
			mOtherScene.setBackgroundColor(0xffffff);
			mOtherScene.addLight(light);

			Material cubeMaterial = new Material();
			cubeMaterial.enableLighting(true);
			cubeMaterial.setColorInfluence(0);
			cubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

			mSphere = new Cube(1);
			mSphere.setMaterial(cubeMaterial);
			mOtherScene.addChild(mSphere);

			Vector3 axis = new Vector3(1, 1, 0);
			axis.normalize();

			RotateOnAxisAnimation anim = new RotateOnAxisAnimation(axis, 360);
			anim.setTransformable3D(mSphere);
			anim.setDurationMilliseconds(10000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			//
			// -- Set up the post processing manager with the required texture size
			//

			mEffects = new PostProcessingManager(this, 400, 400);
			RenderPass renderPass = new RenderPass(getCurrentScene(),
					getCurrentCamera(), 0);
			mEffects.addPass(renderPass);

			//
			// -- Other effect passes could be added here
			//

			switchScene(mOtherScene);
		}

        @Override
        public void onRender(final long ellapsedTime, final double deltaTime) {
			//
			// -- Off screen rendering first. Render to texture.
			//
			mEffects.render(ellapsedTime, deltaTime);
			try {
				if (mCurrentTexture != null)
					mSphere.getMaterial().removeTexture(mCurrentTexture);

				//
				// -- Get the latest updated texture from the post
				//    processing manager
				//

				mCurrentTexture = mEffects.getTexture();
				mSphere.getMaterial().addTexture(mCurrentTexture);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}
			super.onRender(ellapsedTime, deltaTime);
		}
	}
}
