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
import org.rajawali3d.curves.ArchimedeanSpiral3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.curves.LogarithmicSpiral3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.Stack;

public class SpiralsFragment extends AExampleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.TOP);

		TextView label = new TextView(getActivity());
		label.setText(R.string.spirals_fragment_text_view_spiral_types);
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
		return new SpiralsRenderer(getActivity(), this);
	}

	private final class SpiralsRenderer extends AExampleRenderer {
		private final int NUM_POINTS = 200;

		public SpiralsRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			getCurrentCamera().setZ(7);

            //
            // -- Logarithmic Spiral
            //
            final Vector3 start = new Vector3(1.0, 0.0, 0);
            LogarithmicSpiral3D logSpiral = new LogarithmicSpiral3D(0.0625, start, new Vector3(0, 1, 1), true);
            drawSpiralCurve(logSpiral, 0x0000ff, new Vector3(0, 1.5, 0));

            //
            // -- Archimedean Spiral
            //
            ArchimedeanSpiral3D aSpiral = new ArchimedeanSpiral3D(.03125, 1, start, new Vector3(0, 1, -1), false);
            drawSpiralCurve(aSpiral, 0x00ff00, new Vector3(0, -1.5, 0));

		}

        private void drawSpiralCurve(ICurve3D curve, int color, Vector3 position) {
            Material lineMaterial = new Material();

            final double angleStep = (20.0*Math.PI) / NUM_POINTS;
            Stack<Vector3> points = new Stack<Vector3>();
            for (int i = 0; i <= NUM_POINTS; i++) {
                Vector3 point = new Vector3();
                curve.calculatePoint(point, i * angleStep);
                points.add(point);
            }

            Line3D line = new Line3D(points, 1, color);
            line.setMaterial(lineMaterial);
            line.setPosition(position);
            getCurrentScene().addChild(line);
        }
	}

}
