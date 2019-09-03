package org.rajawali3d.examples.examples.lights;

import android.content.Context;
import androidx.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.SpotLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class SpotLightFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new SpotLightRenderer(getActivity(), this);
	}

	private final class SpotLightRenderer extends AExampleRenderer {

		public SpotLightRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			final SpotLight spotLight = new SpotLight();
			spotLight.setPower(1.5f);
            spotLight.enableLookAt();
            spotLight.setPosition(0, 4.0, 0.0);
            spotLight.setLookAt(0, 0, 0);
			getCurrentScene().addLight(spotLight);

            getCurrentCamera().setPosition(0, 2, 6);
            getCurrentCamera().setLookAt(0, 0, 0);
            getCurrentCamera().enableLookAt();
            getCurrentCamera().resetToLookAt();

			Material sphereMaterial = new Material();
			sphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
			SpecularMethod.Phong phongMethod = new SpecularMethod.Phong();
			phongMethod.setShininess(180);
			sphereMaterial.setSpecularMethod(phongMethod);
			sphereMaterial.setAmbientIntensity(0, 0, 0);
			sphereMaterial.enableLighting(true);

			Sphere rootSphere = new Sphere(.2f, 12, 12);
			rootSphere.setMaterial(sphereMaterial);
			rootSphere.setRenderChildrenAsBatch(true);
			rootSphere.setVisible(false);
			getCurrentScene().addChild(rootSphere);

			// -- inner ring

			float radius = .8f;
			int count = 0;

			for (int i = 0; i < 360; i += 36) {
				double radians = MathUtil.degreesToRadians(i);
				int color = 0xfed14f;
				if (count % 3 == 0)
					color = 0x10a962;
				else if (count % 3 == 1)
					color = 0x4184fa;
				count++;

				Object3D sphere = rootSphere.clone(false);
				sphere.setPosition((float) Math.sin(radians) * radius, 0,
						(float) Math.cos(radians) * radius);
				sphere.setMaterial(sphereMaterial);
				sphere.setColor(color);
				rootSphere.addChild(sphere);
			}

			// -- outer ring

			radius = 2.4f;
			count = 0;

			for (int i = 0; i < 360; i += 12) {
				double radians = MathUtil.degreesToRadians(i);
				int color = 0xfed14f;
				if (count % 3 == 0)
					color = 0x10a962;
				else if (count % 3 == 1)
					color = 0x4184fa;
				count++;

				Object3D sphere = rootSphere.clone(false);
				sphere.setPosition((float) Math.sin(radians) * radius, 0,
						(float) Math.cos(radians) * radius);
				sphere.setMaterial(sphereMaterial);
				sphere.setColor(color);
				rootSphere.addChild(sphere);
			}

			final Object3D target = new Object3D();

			EllipticalOrbitAnimation3D anim = new EllipticalOrbitAnimation3D(
					new Vector3(0, .2f, 0), new Vector3(1, .2f, 1), 0, 359);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setDurationMilliseconds(6000);
			anim.setTransformable3D(target);
            anim.registerListener(new IAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationUpdate(Animation animation, double v) {
                    spotLight.setLookAt(target.getWorldPosition());
                }
            });
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}
    }
}
