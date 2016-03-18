package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.view.ISurface;

import java.util.Stack;

public class LinesFragment extends AExampleFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

    @Override
    protected void onBeforeApplyRenderer() {
        mRenderSurface.setAntiAliasingMode(ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING);
        mRenderSurface.setSampleCount(2);
        super.onBeforeApplyRenderer();
    }

    @Override
    public AExampleRenderer createRenderer() {
		return new LinesRenderer(getActivity(), this);
	}

	private final class LinesRenderer extends AExampleRenderer {

		public LinesRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			ALight light1 = new DirectionalLight(0, 0, -1);
			light1.setPower(.3f);
			getCurrentCamera().setPosition(0, 0, 27);

			Stack<Vector3> points = createWhirl(6, 6f, 0, 0, .05f);

			/**
			 * A Line3D takes a Stack of <Number3D>s, thickness and a color
			 */
			Line3D whirl = new Line3D(points, 1, 0xffffff00);
			Material material = new Material();
			whirl.setMaterial(material);
			getCurrentScene().addChild(whirl);

			Vector3 axis = new Vector3(2, .4f, 1);
			axis.normalize();
			RotateOnAxisAnimation anim = new RotateOnAxisAnimation(axis, 360);
			anim.setDurationMilliseconds(8000);
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			anim.setTransformable3D(whirl);
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

		private Stack<Vector3> createWhirl(int numSides, float scaleFactor,
				float centerX, float centerY, float rotAngle) {
			Stack<Vector3> points = new Stack<Vector3>();
			Vector3[] sidePoints = new Vector3[numSides];
			float rotAngleSin = (float) Math.sin(rotAngle);
			float rotAngleCos = (float) Math.cos(rotAngle);
			float a = (float) Math.PI * (1f - 2f / (float) numSides);
			float c = (float) Math.sin(a)
					/ (rotAngleSin + (float) Math.sin(a + rotAngle));

			for (int k = 0; k < numSides; k++) {
				float t = (2f * (float) k + 1f) * (float) Math.PI
						/ (float) numSides;
				sidePoints[k] = new Vector3(Math.sin(t), Math.cos(t), 0);
			}

			for (int n = 0; n < 64; n++) {
				for (int l = 0; l < numSides; l++) {
					Vector3 p = sidePoints[l];
					points.add(new Vector3((p.x * scaleFactor) + centerX,
							(p.y * scaleFactor) + centerY, 8 - (n * .25f)));
				}
				for (int m = 0; m < numSides; m++) {
					Vector3 p = sidePoints[m];
					double z = p.x;
					p.x = (p.x * rotAngleCos - p.y * rotAngleSin) * c;
					p.y = (z * rotAngleSin + p.y * rotAngleCos) * c;
				}
			}

			return points;
		}

	}

}
