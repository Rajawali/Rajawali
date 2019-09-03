package org.rajawali3d.examples.examples.materials;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.AlphaMapTexture;
import org.rajawali3d.materials.textures.SpecularMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class SpecularAndAlphaFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new SpecularAndAlphaRenderer(getActivity(), this);
	}

	private final class SpecularAndAlphaRenderer extends AExampleRenderer {

		public SpecularAndAlphaRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			PointLight pointLight = new PointLight();
			pointLight.setPower(1);
			pointLight.setPosition(-1, 1, 4);

			getCurrentScene().addLight(pointLight);

			try {
				Texture earthTexture = new Texture("earthDiffuseTex", R.drawable.earth_diffuse);

				Material material = new Material();
				material.enableLighting(true);
				material.setDiffuseMethod(new DiffuseMethod.Lambert());
				material.setSpecularMethod(new SpecularMethod.Phong(Color.WHITE, 40));
				material.addTexture(earthTexture);
				material.addTexture(new SpecularMapTexture("earthSpecularTex", R.drawable.earth_specular));
				material.setColorInfluence(0);

				Sphere sphere = new Sphere(1, 32, 24);
				sphere.setMaterial(material);
				sphere.setY(1.2f);
				getCurrentScene().addChild(sphere);

				RotateOnAxisAnimation sphereAnim = new RotateOnAxisAnimation(Vector3.Axis.Y,
						359);
				sphereAnim.setDurationMilliseconds(14000);
				sphereAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
				sphereAnim.setTransformable3D(sphere);
				getCurrentScene().registerAnimation(sphereAnim);
				sphereAnim.play();

				material = new Material();
				material.enableLighting(true);
				material.setDiffuseMethod(new DiffuseMethod.Lambert());
				material.setSpecularMethod(new SpecularMethod.Phong());
				material.addTexture(earthTexture);
				material.addTexture(new AlphaMapTexture("alphaMapTex", R.drawable.camden_town_alpha));
				material.setColorInfluence(0);

				sphere = new Sphere(1, 32, 24);
				sphere.setMaterial(material);
				sphere.setDoubleSided(true);
				sphere.setY(-1.2f);
				getCurrentScene().addChild(sphere);

				sphereAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, -359);
				sphereAnim.setDurationMilliseconds(10000);
				sphereAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
				sphereAnim.setTransformable3D(sphere);
				getCurrentScene().registerAnimation(sphereAnim);
				sphereAnim.play();
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			TranslateAnimation3D lightAnim = new TranslateAnimation3D(
					new Vector3(-2, 3, 3), new Vector3(2, -1, 3));
			lightAnim.setDurationMilliseconds(3000);
			lightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
			lightAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			lightAnim.setTransformable3D(pointLight);
			getCurrentScene().registerAnimation(lightAnim);
			lightAnim.play();

			getCurrentCamera().setZ(6);
		}

	}

}
