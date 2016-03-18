package org.rajawali3d.examples.examples.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.loader.LoaderGCode;
import org.rajawali3d.materials.Material;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.util.RajLog;

public class LoaderGCodeFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new GCodeRenderer(getActivity(), this);
	}

	private final class GCodeRenderer extends AExampleRenderer {
		public GCodeRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			setFrameRate(60);
			LoaderGCode gCodeParser = new LoaderGCode(getResources(), getTextureManager(), R.raw.calibrationcube_404020_psm_pla35);
			try {
				Object3D gCode3D = gCodeParser.parse().getParsedObject();
				if (null != gCode3D) {
					Line3D firstPos = (Line3D) gCode3D.getChildAt(0);
					Line3D lastPos = null;
					if (gCode3D.getNumChildren() < 2) {
						lastPos = firstPos;
					} else {
						lastPos = (Line3D) gCode3D.getChildAt(gCode3D
								.getNumChildren() - 1);
					}
					getCurrentCamera().setPosition(0, 0,
							lastPos.getPoint(0).z + 150);
					getCurrentCamera().setLookAt(0, 0, 0);
					float scaleFactor = 0.7f;
					gCode3D.setScale(scaleFactor);
					gCode3D.setPosition(-firstPos.getPoint(0).x
							* scaleFactor, -firstPos.getPoint(0).y
							* scaleFactor, 0);
					gCode3D.setRotation(360 - 35, 0, 360 - 15);
					Material mat = new Material();
					for (int i = 0; i < gCode3D.getNumChildren(); i++) {
						gCode3D.getChildAt(i).setMaterial(mat);
					}
					getCurrentScene().addChild(gCode3D);
				}
			} catch (Exception e) {
				RajLog.e(new StringBuilder()
						.append("error init'ing gcode GL scene:\n")
						.append(Log.getStackTraceString(e)).toString());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.BOTTOM);

		TextView label = new TextView(getActivity());
		label.setText(R.string.gcode_fragment_by);
		label.setTextSize(20);
		label.setGravity(Gravity.CENTER);
		label.setHeight(100);
		ll.addView(label);

		mLayout.addView(ll);

		return mLayout;
	}

}
