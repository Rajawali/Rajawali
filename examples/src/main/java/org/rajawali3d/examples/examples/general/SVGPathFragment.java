package org.rajawali3d.examples.examples.general;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.animation.BounceInterpolator;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.curves.SVGPath;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.List;
import java.util.Stack;

public class SVGPathFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new SVGPathRenderer(getActivity(), this);
	}

	private final class SVGPathRenderer extends AExampleRenderer {

		public SVGPathRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			final String path = "M22.395-127.223c-4.492,11.344-4.688,33.75,0,44.883"
					+ "c-11.328-4.492-33.656-4.579-44.789,0.109c4.491-11.354,4.688-33.75,0-44.892"
					+ "C-11.066-122.63,11.262-122.536,22.395-127.223z";
			SVGPath svgPath = new SVGPath();

			List<CompoundCurve3D> paths = svgPath.parseResourceString(mContext,
																	  R.raw.lavatories_svg_path);
			paths.addAll(svgPath.parseString(path));
			Stack<Stack<Vector3>> pathPoints = new Stack<Stack<Vector3>>();

			for (int i = 0; i < paths.size(); i++) {
				ICurve3D subPath = paths.get(i);
				Stack<Vector3> points = new Stack<Vector3>();
				int subdiv = 1000;
				for (int j = 0; j <= subdiv; j++) {
					Vector3 point = new Vector3();
					subPath.calculatePoint(point, (float) j	/ (float) subdiv);
					points.add(point);
				}

				pathPoints.add(points);
				Line3D line = new Line3D(points, 1);
				Material material = new Material();
				line.setMaterial(material);
				getCurrentScene().addChild(line);

				TranslateAnimation3D anim = new TranslateAnimation3D(
						new Vector3(line.getPosition().x, line.getPosition().y,
								line.getPosition().z - 80));
				anim.setInterpolator(new BounceInterpolator());
				anim.setDurationDelta(1 + Math.random() * 5);
				anim.setDelayDelta(.5f + Math.random());
				anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
				anim.setTransformable3D(line);
				getCurrentScene().registerAnimation(anim);
				anim.play();
			}
			getCurrentCamera().setFarPlane(2000);
			getCurrentCamera().setY(50);
			getCurrentCamera().setZ(520);
		}

	}

}
