package org.rajawali3d.examples.examples.lights;

import android.content.Context;
import androidx.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;

public class MultipleLightsFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new MultipleLightsRenderer(getActivity(), this);
	}

	private final class MultipleLightsRenderer extends AExampleRenderer {

		public MultipleLightsRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			PointLight light1 = new PointLight();
			light1.setPower(1.5f);
			PointLight light2 = new PointLight();
			light2.setPower(1.5f);

			getCurrentScene().addLight(light1);
			getCurrentScene().addLight(light2);

			getCurrentCamera().setPosition(0, 2, 4);
			getCurrentCamera().setLookAt(0, 0, 0);

			try {
                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parser.parse();

                Object3D suzanne = parser.getParsedObject();
				Material material = new Material();
				material.setDiffuseMethod(new DiffuseMethod.Lambert());
                material.setColor(0xff990000);
				material.enableLighting(true);
				suzanne.setMaterial(material);
				getCurrentScene().addChild(suzanne);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Animation3D anim = new TranslateAnimation3D(
					new Vector3(-10, -10, 5), new Vector3(-10, 10, 5));
			anim.setDurationMilliseconds(4000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			anim.setTransformable3D(light1);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new TranslateAnimation3D(new Vector3(10, 10, 5),
					new Vector3(10, -10, 5));
			anim.setDurationMilliseconds(2000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			anim.setTransformable3D(light2);
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

	}
}
