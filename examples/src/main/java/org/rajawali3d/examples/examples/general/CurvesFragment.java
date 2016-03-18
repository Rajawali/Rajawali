package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.CubicBezierCurve3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.curves.LinearBezierCurve3D;
import org.rajawali3d.curves.QuadraticBezierCurve3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.Stack;

public class CurvesFragment extends AExampleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.TOP);

		TextView label = new TextView(getActivity());
		label.setText(R.string.curves_fragment_text_view_curve_types);
		label.setTextSize(14);
		label.setGravity(Gravity.CENTER);
		label.setTextColor(0xFFFFFFFF);
		label.setHeight(100);
		ll.addView(label);

		mLayout.addView(ll);

		return mLayout;
	}

	@Override
    public AExampleRenderer createRenderer() {
		return new CurvesRenderer(getActivity(), this);
	}

	private final class CurvesRenderer extends AExampleRenderer {
		private final int NUM_POINTS = 100;

		public CurvesRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			getCurrentCamera().setZ(7);

			//
			// -- Quadratic Bezier Curve
			//

			ICurve3D curve = new CubicBezierCurve3D(new Vector3(-1, 0, 0),
					new Vector3(-1, 1.3f, 0), new Vector3(1, -1.9f, 0),
					new Vector3(1, 0, 0));

			drawCurve(curve, 0xffffff, new Vector3(0, 2, 0));

			//
			// -- Linear Bezier Curve
			//

			curve = new LinearBezierCurve3D(new Vector3(-1, 0, 0), new Vector3(
					1, 0, 0));

			drawCurve(curve, 0xffff00, new Vector3(0, 1f, 0));

			//
			// -- Quadratic Bezier Curve
			//

			curve = new QuadraticBezierCurve3D(new Vector3(-1, 0, 0),
					new Vector3(.3f, 1, 0), new Vector3(1, 0, 0));

			drawCurve(curve, 0x00ff00, new Vector3(0, 0, 0));

			//
			// -- Catmull Rom Curve
			//

			CatmullRomCurve3D catmull = new CatmullRomCurve3D();
			catmull.addPoint(new Vector3(-1.5f, 0, 0)); // control point 1
			catmull.addPoint(new Vector3(-1, 0, 0)); // start point
			catmull.addPoint(new Vector3(-.5f, .3f, 0));
			catmull.addPoint(new Vector3(-.2f, -.2f, 0));
			catmull.addPoint(new Vector3(.1f, .5f, 0));
			catmull.addPoint(new Vector3(.5f, -.3f, 0));
			catmull.addPoint(new Vector3(1, 0, 0)); // end point
			catmull.addPoint(new Vector3(1.5f, -1, 0)); // control point 2

			drawCurve(catmull, 0xff0000, new Vector3(0, -1, 0));

			//
			// -- Compound path
			//

			CompoundCurve3D compound = new CompoundCurve3D();
			compound.addCurve(new CubicBezierCurve3D(new Vector3(-1, 0, 0),
					new Vector3(-1, 1.3f, 0), new Vector3(-.5f, -1.9f, 0),
					new Vector3(-.5f, 0, 0)));
			compound.addCurve(new LinearBezierCurve3D(new Vector3(-.5f, 0, 0),
					new Vector3(0, 0, 0)));
			compound.addCurve(new QuadraticBezierCurve3D(new Vector3(0, 0, 0),
					new Vector3(.3f, 1, 0), new Vector3(.5f, 0, 0)));

			catmull = new CatmullRomCurve3D();
			catmull.addPoint(new Vector3(0, 1, 0)); // control point 1
			catmull.addPoint(new Vector3(.5f, 0, 0)); // start point
			catmull.addPoint(new Vector3(.7f, .3f, 0));
			catmull.addPoint(new Vector3(.75f, -.2f, 0));
			catmull.addPoint(new Vector3(.9f, .5f, 0));
			catmull.addPoint(new Vector3(1, 0, 0)); // end point
			catmull.addPoint(new Vector3(1.5f, -1, 0)); // control point 2

			compound.addCurve(catmull);

			drawCurve(compound, 0xff3333, new Vector3(0, -2, 0));
		}

		private void drawCurve(ICurve3D curve, int color, Vector3 position) {
			Material lineMaterial = new Material();

			Stack<Vector3> points = new Stack<Vector3>();
			for (int i = 0; i <= NUM_POINTS; i++) {
				Vector3 point = new Vector3();
				curve.calculatePoint(point, (float) i / (float) NUM_POINTS);
				points.add(point);
			}

			Line3D line = new Line3D(points, 1, color);
			line.setMaterial(lineMaterial);
			line.setPosition(position);
			getCurrentScene().addChild(line);
		}

	}

}
