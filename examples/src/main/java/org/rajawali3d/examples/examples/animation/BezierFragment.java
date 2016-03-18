package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.CubicBezierCurve3D;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class BezierFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new BezierRenderer(getActivity(), this);
	}

	private final class BezierRenderer extends AExampleRenderer {
		private DirectionalLight mLight;

		public BezierRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(0, 1, -1);
			mLight.setPower(1);
			getCurrentScene().addLight(mLight);
			getCurrentCamera().setPosition(0, 0, 20);

			Object3D redSphere = new Sphere(1, 16, 16);
			redSphere.setPosition(0, -4, 0);
			redSphere.setColor(0xffff0000);

			Material phong = new Material();
			phong.enableLighting(true);
			phong.setDiffuseMethod(new DiffuseMethod.Lambert());
			phong.setSpecularMethod(new SpecularMethod.Phong());
			redSphere.setMaterial(phong);
			getCurrentScene().addChild(redSphere);

			Object3D yellowSphere = new Sphere(.6f, 16, 16);
			yellowSphere.setPosition(2, 4, 0);
			yellowSphere.setColor(0xffffff00);
			Material diffuse = new Material();
			diffuse.enableLighting(true);
			diffuse.setDiffuseMethod(new DiffuseMethod.Lambert());
			yellowSphere.setMaterial(diffuse);
			getCurrentScene().addChild(yellowSphere);

			CompoundCurve3D redBezierPath = new CompoundCurve3D();
			redBezierPath.addCurve(new CubicBezierCurve3D(
					new Vector3(0, -4, 0), new Vector3(-2, -4, .2f),
					new Vector3(4, 4, 4), new Vector3(-2, 4, 4.5f)));
			redBezierPath.addCurve(new CubicBezierCurve3D(new Vector3(-2, 4,
					4.5f), new Vector3(2, -2, -2), new Vector3(4, 4, 4),
					new Vector3(-2, 4, 4.5f)));

			CompoundCurve3D yellowBezierPath = new CompoundCurve3D();
			yellowBezierPath.addCurve(new CubicBezierCurve3D(new Vector3(2, 4,
					0), new Vector3(-8, 3, 4), new Vector3(-4, 0, -2),
					new Vector3(4, -3, 30)));
			yellowBezierPath.addCurve(new CubicBezierCurve3D(new Vector3(4, -3,
					30), new Vector3(6, 1, 2), new Vector3(4, 2, 3),
					new Vector3(-3, -3, -4.5f)));

			Animation3D redAnim = new SplineTranslateAnimation3D(redBezierPath);
			redAnim.setDurationMilliseconds(2000);
			redAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			redAnim.setTransformable3D(redSphere);
			getCurrentScene().registerAnimation(redAnim);
			redAnim.play();

			Animation3D yellowAnim = new SplineTranslateAnimation3D(yellowBezierPath);
			yellowAnim.setDurationMilliseconds(3800);
			yellowAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			yellowAnim.setTransformable3D(yellowSphere);
			getCurrentScene().registerAnimation(yellowAnim);
			yellowAnim.play();
		}
	}
}
