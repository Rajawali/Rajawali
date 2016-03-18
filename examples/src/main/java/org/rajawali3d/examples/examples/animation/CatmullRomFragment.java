package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;

import java.util.Stack;

public class CatmullRomFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new CatmullRomRenderer(getActivity(), this);
	}

	private final class CatmullRomRenderer extends AExampleRenderer {

		public CatmullRomRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			ALight light = new DirectionalLight(0, 0, -1);
			light.setPower(1);

			getCurrentScene().addLight(light);

			getCurrentCamera().setPosition(0, 0, 10);
			getCurrentCamera().setLookAt(0, 0, 0);
            getCurrentCamera().enableLookAt();

			Material material = new Material();

			// -- create a catmull-rom path. The first and the last point are control points.
			CatmullRomCurve3D path = new CatmullRomCurve3D();
			float r = 12;
			float rh = r * .5f;

			for (int i = 0; i < 16; i++) {
				path.addPoint(new Vector3(-rh + (Math.random() * r), -rh
						+ (Math.random() * r), -rh + (Math.random() * r)));
			}

			try {
				LoaderOBJ parser = new LoaderOBJ(mContext.getResources(),
												 mTextureManager, R.raw.arrow);
				parser.parse();
				Object3D arrow = parser.getParsedObject();
				arrow.setMaterial(material);
				arrow.setScale(.2f);
				arrow.setColor(0xffffff00);
				getCurrentScene().addChild(arrow);

				final SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
				anim.setDurationMilliseconds(12000);
				anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
				// -- orient to path
				anim.setOrientToPath(true);
				anim.setTransformable3D(arrow);
				getCurrentScene().registerAnimation(anim);
				anim.play();
			} catch (ParsingException e) {
				e.printStackTrace();
			}

			int numPoints = path.getNumPoints();

			for (int i = 0; i < numPoints; i++) {
				Sphere s = new Sphere(.2f, 6, 6);
				s.setMaterial(material);
				s.setPosition(path.getPoint(i));

				if (i == 0)
					s.setColor(0xffff0000);
				else if (i == numPoints - 1)
					s.setColor(0xff0066ff);
				else
					s.setColor(0xff999999);

				getCurrentScene().addChild(s);
			}

			// -- visualize the line
			Stack<Vector3> linePoints = new Stack<Vector3>();
			for (int i = 0; i < 100; i++) {
				Vector3 point = new Vector3();
				path.calculatePoint(point, i / 100f);
				linePoints.add(point);
			}
			Line3D line = new Line3D(linePoints, 1, 0xffffffff);
			line.setMaterial(material);
			getCurrentScene().addChild(line);

			EllipticalOrbitAnimation3D camAnim = new EllipticalOrbitAnimation3D(
					new Vector3(), new Vector3(26, 0, 0), 0, 360,
					EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);
			camAnim.setDurationMilliseconds(10000);
			camAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
			camAnim.setTransformable3D(getCurrentCamera());
			getCurrentScene().registerAnimation(camAnim);
			camAnim.play();
		}

	}

}
