package org.rajawali3d.examples.examples.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.view.SurfaceView;

public class TransparentSurfaceFragment extends AExampleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		ImageView iv = new ImageView(getActivity());
		iv.setImageResource(R.drawable.flickrpics);

		mLayout.addView(iv, 0);
        ((View) mRenderSurface).bringToFront();

		return mLayout;
	}

    @Override
    protected void onBeforeApplyRenderer() {
        ((SurfaceView) mRenderSurface).setTransparent(true);
        super.onBeforeApplyRenderer();
    }

    @Override
    public AExampleRenderer createRenderer() {
		return new TransparentSurfaceRenderer(getActivity(), this);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.rajawali_surfaceview_fragment;
	}

	private final class TransparentSurfaceRenderer extends AExampleRenderer {

		public TransparentSurfaceRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			DirectionalLight light = new DirectionalLight(0, 0, -1);
			light.setPower(1);

			getCurrentScene().addLight(light);
			getCurrentCamera().setPosition(0, 0, 16);

			try {
				Object3D monkey = new Cube(2.0f);
				Material material = new Material();
				material.enableLighting(true);
				material.setDiffuseMethod(new DiffuseMethod.Lambert());
				monkey.setMaterial(material);
				monkey.setColor(0xffff8C00);
				monkey.setScale(2);
				getCurrentScene().addChild(monkey);

				RotateOnAxisAnimation anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
				anim.setDurationMilliseconds(6000);
				anim.setRepeatMode(Animation.RepeatMode.INFINITE);
				anim.setInterpolator(new AccelerateDecelerateInterpolator());
				anim.setTransformable3D(monkey);
				getCurrentScene().registerAnimation(anim);
				anim.play();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// -- set the background color to be transparent
			// you need to have called setGLBackgroundTransparent(true); in the activity
			// for this to work.
			getCurrentScene().setBackgroundColor(0);
		}

	}

}
