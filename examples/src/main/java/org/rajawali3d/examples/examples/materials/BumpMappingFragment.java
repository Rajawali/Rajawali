package org.rajawali3d.examples.examples.materials;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.NormalMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;

public class BumpMappingFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new BumpMappingRenderer(getActivity(), this);
	}

	private final class BumpMappingRenderer extends AExampleRenderer {
		private PointLight mLight;
		private Object3D mEarth;
		private Animation3D mLightAnim;

		public BumpMappingRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new PointLight();
			mLight.setPosition(-2, -2, 0);
			mLight.setPower(2f);

			getCurrentScene().addLight(mLight);
			getCurrentCamera().setPosition(0, 0, 6);

			try {
				Plane cube = new Plane(18, 12, 2, 2);
				Material material1 = new Material();
				material1.setDiffuseMethod(new DiffuseMethod.Lambert());
				material1.enableLighting(true);
				material1.addTexture(new Texture("wallDiffuseTex", R.drawable.masonry_wall_texture));
				material1.addTexture(new NormalMapTexture("wallNormalTex", R.drawable.masonry_wall_normal_map));
				material1.setColorInfluence(0);
				cube.setMaterial(material1);
				cube.setZ(-2);
				getCurrentScene().addChild(cube);

				RotateOnAxisAnimation anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -5, 5);
				anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
				anim.setDurationMilliseconds(5000);
				anim.setTransformable3D(cube);
				getCurrentScene().registerAnimation(anim);
				anim.play();

				mEarth = new Sphere(1, 32, 32);
				mEarth.setZ(-.5f);
				getCurrentScene().addChild(mEarth);

				Material material2 = new Material();
				material2.setDiffuseMethod(new DiffuseMethod.Lambert());
				material2.setSpecularMethod(new SpecularMethod.Phong(Color.WHITE, 150));
				material2.enableLighting(true);
				material2.addTexture(new Texture("earthDiffuseTex", R.drawable.earth_diffuse));
				material2.addTexture(new NormalMapTexture("eartNormalTex", R.drawable.earth_normal));
				material2.setColorInfluence(0);
				mEarth.setMaterial(material2);

				RotateOnAxisAnimation earthAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 359);
				earthAnim.setDurationMilliseconds(6000);
				earthAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
				earthAnim.setTransformable3D(mEarth);
				getCurrentScene().registerAnimation(earthAnim);
				earthAnim.play();

			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			mLightAnim = new TranslateAnimation3D(new Vector3(-2, 2, 2),
					new Vector3(2, -2, 2));
			mLightAnim.setDurationMilliseconds(4000);
			mLightAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			mLightAnim.setTransformable3D(mLight);
			mLightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
			getCurrentScene().registerAnimation(mLightAnim);
			mLightAnim.play();
		}

	}

}
