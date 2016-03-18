package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;

import java.util.List;
import java.util.Stack;

public class UniformDistributionFragment extends AExampleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.TOP);

		TextView label = new TextView(getActivity());
		label.setText(R.string.uniform_distribution_fragment_text_view_info);
		label.setTextSize(16);
		label.setGravity(Gravity.CENTER);
		label.setTextColor(0xFFFFFFFF);
		label.setHeight(140);
		ll.addView(label);

		mLayout.addView(ll);

		return mLayout;
	}

	@Override
    public AExampleRenderer createRenderer() {
		return new UniformDistributionRenderer(getActivity(), this);
	}

	private final class UniformDistributionRenderer extends AExampleRenderer {
		private final int NUM_POINTS = 200;
		private final int ANIMATION_DURATION = 20000;
		private final int CURVE1_COLOR = 0xffffee;
		private final int CURVE2_COLOR = 0xff0000;

		public UniformDistributionRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			Material material = new Material();

			// -- "curve1" will be the original curve. Note that we create two curves for
			// demonstration purposes only. You'd typically create one curve and then
			// reparametrize it.
			CatmullRomCurve3D curve1 = new CatmullRomCurve3D();
			CatmullRomCurve3D curve2 = new CatmullRomCurve3D();

			for (int i = 0; i < 16; i++) {
				// -- generate a random point within certain limits
				Vector3 pos = new Vector3(-1 + (Math.random() * 2), -1.2f
						+ (Math.random() * 2.4f), 0);
				curve1.addPoint(pos);
				curve2.addPoint(pos);

				// -- add a wireframe cube so we can see what the original
				// points were
				Cube s = new Cube(.06f);
				s.setMaterial(material);
				s.setColor(CURVE1_COLOR);
				s.setPosition(pos);
				s.setDrawingMode(GLES20.GL_LINES);
				getCurrentScene().addChild(s);
			}

			// -- draw the first curve
			drawCurve(curve1, CURVE1_COLOR, new Vector3());

			Object3D pathFollowObject = new Sphere(.04f, 16, 16);
			pathFollowObject.setColor(CURVE1_COLOR);
			pathFollowObject.setMaterial(material);
			getCurrentScene().addChild(pathFollowObject);

			// -- animate a sphere that follow the first curve.
			// This shows the non constant speed of a non parametrized curve.
			Animation3D anim = new SplineTranslateAnimation3D(curve1);
			anim.setDurationMilliseconds(ANIMATION_DURATION);
			anim.setTransformable3D(pathFollowObject);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			// -- reparametrize the curve for uniform distribution
			curve2.reparametrizeForUniformDistribution(curve2.getPoints()
					.size() * 4);
			List<Vector3> points = curve2.getPoints();

			// -- put spheres on the curve where the new points are
			for (int i = 0; i < points.size(); i++) {
				Vector3 pos = points.get(i);
				Sphere s = new Sphere(.02f, 4, 4);
				s.setMaterial(material);
				s.setColor(CURVE2_COLOR);
				s.setPosition(pos);
				getCurrentScene().addChild(s);
			}

			// -- draw the second, reparametrized, curve
			drawCurve(curve2, CURVE2_COLOR, new Vector3());

			pathFollowObject = new Sphere(.04f, 16, 16);
			pathFollowObject.setColor(CURVE2_COLOR);
			pathFollowObject.setMaterial(material);
			getCurrentScene().addChild(pathFollowObject);

			// -- animate a sphere on the second curve.
			// This shows a more or less constant speed of a parametrized curve.
			anim = new SplineTranslateAnimation3D(curve2);
			anim.setDurationMilliseconds(ANIMATION_DURATION);
			anim.setTransformable3D(pathFollowObject);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

		private void drawCurve(ICurve3D curve, int color, Vector3 position) {
			Material lineMaterial = new Material();

			Stack<Vector3> points = new Stack<Vector3>();
			for (int i = 0; i <= NUM_POINTS; i++) {
				Vector3 pos = new Vector3();
				curve.calculatePoint(pos, (float) i / (float) NUM_POINTS);
				points.add(pos);
			}

			Line3D line = new Line3D(points, 1, color);
			line.setMaterial(lineMaterial);
			line.setPosition(position);
			getCurrentScene().addChild(line);
		}

	}

}
