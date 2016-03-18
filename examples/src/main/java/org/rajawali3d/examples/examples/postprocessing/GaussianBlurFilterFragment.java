/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
import org.rajawali3d.primitives.Cube;

import java.util.Random;

public class GaussianBlurFilterFragment extends AExampleFragment {
	@Override
    public AExampleRenderer createRenderer() {
		return new GaussianBlurFilterRenderer(getActivity(), this);
	}

	public static final class GaussianBlurFilterRenderer extends AExampleRenderer {
		private PostProcessingManager mEffects;

		public GaussianBlurFilterRenderer(Context context, @Nullable AExampleFragment fragment) {
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
			// -- Add a Gaussian blur effect. This requires a horizontal and a vertical pass.
			//

			EffectPass horizontalPass = new BlurPass(BlurPass.Direction.HORIZONTAL, 6, getViewportWidth(), getViewportHeight());
			mEffects.addPass(horizontalPass);
			EffectPass verticalPass = new BlurPass(BlurPass.Direction.VERTICAL, 6, getViewportWidth(), getViewportHeight());
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
