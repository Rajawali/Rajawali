package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.ColorAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.AlphaMapTexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;

public class ColorAnimationFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new ColorAnimationRenderer(getActivity(), this);
	}

	private final class ColorAnimationRenderer extends AExampleRenderer {

		public ColorAnimationRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

		@Override
		protected void initScene() {
			//
			// -- First cube
			//

			Material material1 = new Material();

			Cube cube1 = new Cube(1);
			cube1.setMaterial(material1);
			cube1.setTransparent(true);
			cube1.setX(-1);
			getCurrentScene().addChild(cube1);

			Animation3D anim = new ColorAnimation3D(0xaaff1111, 0xffffff11);
			anim.setTransformable3D(cube1);
			anim.setDurationMilliseconds(2000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 359);
			anim.setTransformable3D(cube1);
			anim.setDurationMilliseconds(6000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			//
			// -- second cube
			//

			Material material2 = new Material();
			try {
				AlphaMapTexture alphaTex = new AlphaMapTexture("camdenTown", R.drawable.camden_town_alpha);
				alphaTex.setInfluence(.5f);
				material2.addTexture(alphaTex);
				material2.setColorInfluence(0);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}
			material2.setColorInfluence(.5f);

			Cube cube2 = new Cube(1);
			cube2.setMaterial(material2);
			cube2.setX(1);
			cube2.setDoubleSided(true);
			getCurrentScene().addChild(cube2);

			anim = new ColorAnimation3D(0xaaff1111, 0xff0000ff);
			anim.setTransformable3D(cube2);
			anim.setDurationMilliseconds(2000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -359);
			anim.setTransformable3D(cube2);
			anim.setDurationMilliseconds(6000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			getCurrentCamera().setPosition(0, 4, 8);
			getCurrentCamera().setLookAt(0, 0, 0);
		}

	}

}
