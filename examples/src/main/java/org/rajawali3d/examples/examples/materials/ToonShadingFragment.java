package org.rajawali3d.examples.examples.materials;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;

public class ToonShadingFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new ToonShadingRenderer(getActivity(), this);
	}

	private final class ToonShadingRenderer extends AExampleRenderer {
		private DirectionalLight mLight;
		private Object3D mMonkey1, mMonkey2, mMonkey3;

		public ToonShadingRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
			getCurrentScene().setBackgroundColor(0xffeeeeee);
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(0, 0, -1);
			mLight.setPower(1);

			getCurrentScene().addLight(mLight);
			getCurrentCamera().setPosition(0, 0, 12);

			try {
				Material toonMat = new Material();
				toonMat.enableLighting(true);
				toonMat.setDiffuseMethod(new DiffuseMethod.Toon());

                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parser.parse();

                mMonkey1 = parser.getParsedObject();
				mMonkey1.setMaterial(toonMat);
				mMonkey1.setPosition(-1.5f, 2, 0);
				getCurrentScene().addChild(mMonkey1);

				toonMat = new Material();
				toonMat.enableLighting(true);
				toonMat.setDiffuseMethod(new DiffuseMethod.Toon(0xffffffff, 0xff000000, 0xff666666,
						0xff000000));

				mMonkey2 = mMonkey1.clone();
				mMonkey2.setMaterial(toonMat);
				mMonkey2.setPosition(1.5f, 2, 0);
				getCurrentScene().addChild(mMonkey2);

				toonMat = new Material();
				toonMat.enableLighting(true);
				toonMat.setDiffuseMethod(new DiffuseMethod.Toon(0xff999900, 0xff003300, 0xffff0000,
						0xffa60000));
				mMonkey3 = mMonkey1.clone();
				mMonkey3.setMaterial(toonMat);
				mMonkey3.setPosition(0, -2, 0);
				getCurrentScene().addChild(mMonkey3);
			} catch (Exception e) {
				e.printStackTrace();
			}

			RotateOnAxisAnimation anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
			anim.setDurationMilliseconds(6000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setTransformable3D(mMonkey1);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -360);
			anim.setDurationMilliseconds(6000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setTransformable3D(mMonkey2);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -360);
			anim.setDurationMilliseconds(6000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setTransformable3D(mMonkey3);
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

	}

}
