package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.Stack;

public class ColoredLinesFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new ColoredLinesRenderer(getActivity(), this);
	}

	private final class ColoredLinesRenderer extends AExampleRenderer {

		public ColoredLinesRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			getCurrentCamera().setPosition(0, 0, 10);
			getCurrentCamera().setLookAt(0, 0, 0);

			Stack<Vector3> points = new Stack<Vector3>();
			int[] colors = new int[2000];
			int colorCount = 0;
			for (int i = -1000; i < 1000; i++) {
				double j = i * .5;
				Vector3 v = new Vector3();
				v.x = Math.cos(j * .4);
				v.y = Math.sin(j * .3);
				v.z = j * .01;
				points.add(v);
				colors[colorCount++] = Color.argb(255,
						(int) (190.f * Math.sin(j)),
						(int) (190.f * Math.cos(j * .3f)),
						(int) (190.f * Math.sin(j * 2) * Math.cos(j)));
			}

			Line3D line = new Line3D(points, 1, colors);
			Material material = new Material();
			material.useVertexColors(true);
			line.setMaterial(material);
			getCurrentScene().addChild(line);

			RotateOnAxisAnimation lineAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 359);
			lineAnim.setDurationMilliseconds(10000);
			lineAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
			lineAnim.setTransformable3D(line);
			getCurrentScene().registerAnimation(lineAnim);
			lineAnim.play();

			TranslateAnimation3D camAnim = new TranslateAnimation3D(
					new Vector3(0, 0, 10), new Vector3(0, 0, -10));
			camAnim.setDurationMilliseconds(12000);
			camAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			camAnim.setTransformable3D(getCurrentCamera());
			getCurrentScene().registerAnimation(camAnim);
			camAnim.play();
		}

	}

}
