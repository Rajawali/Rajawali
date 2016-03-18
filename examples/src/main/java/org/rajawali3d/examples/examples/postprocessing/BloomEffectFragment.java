package org.rajawali3d.examples.examples.postprocessing;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.effects.BloomEffect;
import org.rajawali3d.postprocessing.passes.BlendPass;
import org.rajawali3d.postprocessing.passes.RenderPass;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.util.RajLog;

import java.util.Random;

public class BloomEffectFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new BloomEffectRenderer(getActivity(), this);
	}

	public static final class BloomEffectRenderer extends AExampleRenderer {
		private PostProcessingManager mEffects;

		public BloomEffectRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			DirectionalLight light = new DirectionalLight();
			light.setPower(1);
            light.setLookAt(0, 0, -1);
            light.enableLookAt();
			getCurrentScene().setBackgroundColor(Color.BLACK);
			getCurrentScene().addLight(light);

			//
			// -- Create a material for all cubes
			//

			Material material = new Material();
			material.enableLighting(true);
			material.setDiffuseMethod(new DiffuseMethod.Lambert());

			getCurrentCamera().setZ(10);

			Random random = new Random();

			//
			// -- Generate cubes with random x, y, z
			//

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
			// -- Create a post processing manager. We can add multiple passes to this.
			//

			mEffects = new PostProcessingManager(this);
			RenderPass renderPass = new RenderPass(getCurrentScene(), getCurrentCamera(), 0);
			mEffects.addPass(renderPass);

			BloomEffect bloomEffect = new BloomEffect(getCurrentScene(), getCurrentCamera(), getViewportWidth(), getViewportHeight(), 0x111111, 0xffffff, BlendPass.BlendMode.SCREEN);
			mEffects.addEffect(bloomEffect);

			bloomEffect.setRenderToScreen(true);

			RajLog.i("Viewport: " + getViewportWidth() + ", " + getViewportHeight());
		}

		@Override
		public void onRender(final long ellapsedTime, final double deltaTime) {
			mEffects.render(ellapsedTime, deltaTime);
		}
	}
}
