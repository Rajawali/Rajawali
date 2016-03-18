package org.rajawali3d.examples.examples.postprocessing;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.passes.BlurPass;
import org.rajawali3d.postprocessing.passes.EffectPass;
import org.rajawali3d.postprocessing.passes.RenderPass;
import org.rajawali3d.postprocessing.passes.SepiaPass;
import org.rajawali3d.primitives.Cube;

import java.util.Random;

public class MultiPassFragment extends AExampleFragment {
	@Override
    public AExampleRenderer createRenderer() {
		return new MultiPassRenderer(getActivity(), this);
	}

	public static final class MultiPassRenderer extends AExampleRenderer {
		private PostProcessingManager mEffects;

		public MultiPassRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			DirectionalLight light = new DirectionalLight();
            light.setLookAt(0, 0, -1);
            light.enableLookAt();
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

			for(int i=0; i<10; i++) {
				Cube cube = new Cube(1);
				cube.setPosition(-5 + random.nextFloat() * 10, -5 + random.nextFloat() * 10, random.nextFloat() * -10);
				cube.setMaterial(material);
				cube.setColor(0x666666 + random.nextInt(0x999999));
				getCurrentScene().addChild(cube);

				Vector3 randomAxis = new Vector3(random.nextFloat(), random.nextFloat(), random.nextFloat());
				randomAxis.normalize();

				RotateOnAxisAnimation anim = new RotateOnAxisAnimation(randomAxis, 360);
				anim.setTransformable3D(cube);
				anim.setDurationMilliseconds(3000 + (int)(random.nextDouble() * 5000));
				anim.setRepeatMode(Animation.RepeatMode.INFINITE);
				getCurrentScene().registerAnimation(anim);
				anim.play();
			}

			//
			// -- Create a post processing manager. We can add multiple passes to this.
			//

			mEffects = new PostProcessingManager(this);

			//
			// -- A render pass renders the current scene to a texture. This texture will
			//    be used for post processing
			//

			RenderPass renderPass = new RenderPass(getCurrentScene(), getCurrentCamera(), 0);
			mEffects.addPass(renderPass);

			//
			// -- Add a Sepia effect
			//

			EffectPass sepiaPass = new SepiaPass();
			mEffects.addPass(sepiaPass);

			//
			// -- Add a Gaussian blur effect. This requires a horizontal and a vertical pass.
			//

			EffectPass horizontalPass = new BlurPass(BlurPass.Direction.HORIZONTAL, 6, getViewportWidth(), getViewportHeight());
			mEffects.addPass(horizontalPass);
			EffectPass verticalPass = new BlurPass(BlurPass.Direction.VERTICAL, 6, getViewportWidth(), getViewportHeight());

			//
			// -- Important. The last pass should render to screen or nothing will happen.
			//

			verticalPass.setRenderToScreen(true);
			mEffects.addPass(verticalPass);
		}

        @Override
        public void onRender(final long ellapsedTime, final double deltaTime) {
			//
			// -- Important. Call render() on the post processing manager.
			//

			mEffects.render(ellapsedTime, deltaTime);
		}
	}
}
