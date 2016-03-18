package org.rajawali3d.examples.examples.effects;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TouchRipplesFragment extends AExampleFragment implements OnTouchListener {
	private Point mScreenSize;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

        ((View) mRenderSurface).setOnTouchListener(this);

		Display display = getActivity().getWindowManager().getDefaultDisplay();
		mScreenSize = new Point();
		display.getSize(mScreenSize);

		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.BOTTOM);

		TextView tv = new TextView(getActivity());
		tv.setText(R.string.touch_ripples_fragment_button_touch_me);
		tv.setTextColor(0xffffffff);
		ll.addView(tv);

		return mLayout;
	}

	@Override
    public AExampleRenderer createRenderer() {
		return new TouchRipplesRenderer(getActivity(), this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			((TouchRipplesRenderer) mRenderer).setTouch(event.getX()
					/ mScreenSize.x, 1.0f - (event.getY() / mScreenSize.y));
		}
		return ((View) mRenderSurface).onTouchEvent(event);
	}

	private final class TouchRipplesRenderer extends AExampleRenderer {
		private final int NUM_CUBES_H = 2;
		private final int NUM_CUBES_V = 2;
		private final int NUM_CUBES = NUM_CUBES_H * NUM_CUBES_V;
		private Animation3D[] mAnims;
		//private TouchRippleFilter mFilter;
		private long frameCount;

		public TouchRipplesRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

		protected void initScene() {
			mAnims = new Animation3D[NUM_CUBES];

			getCurrentCamera().setPosition(0, 0, 10);

			DirectionalLight light = new DirectionalLight(0, 0, 1);
			light.setPower(1f);

			// TODO add post processing effects
			/*
			Object3D group = new Object3D();
			DiffuseMaterial material = new DiffuseMaterial();
			material.setUseSingleColor(true);

			Random rnd = new Random();

			for (int y = 0; y < NUM_CUBES_V; ++y) {
				for (int x = 0; x < NUM_CUBES_H; ++x) {
					Cube cube = new Cube(.7f);
					cube.setPosition(x * 2, y * 2, 0);
					cube.addLight(light);
					cube.setMaterial(material);
					cube.setColor((int) (0xffffff * rnd.nextFloat()));
					group.addChild(cube);
					Vector3 axis = new Vector3(rnd.nextFloat(),
							rnd.nextFloat(), rnd.nextFloat());
					Animation3D anim = new RotateAnimation3D(axis, 360);
					anim.setRepeatMode(RepeatMode.INFINITE);
					anim.setDuration(8000);
					anim.setTransformable3D(cube);

					int index = x + (NUM_CUBES_H * y);
					mAnims[index] = anim;
				}
			}

			group.setX(((NUM_CUBES_H - 1) * 2) * -.5f);
			group.setY(((NUM_CUBES_V - 1) * 2) * -.5f);
			group.setZ(-1f);

			addChild(group);

			SimpleMaterial planeMat = new SimpleMaterial();
			try {
				planeMat.addTexture(new Texture(R.drawable.utrecht));
			} catch (TextureException e) {
				e.printStackTrace();
			}
			Plane plane = new Plane(4, 4, 1, 1);
			plane.setRotZ(-90);
			plane.setScale(3.7f);
			plane.setMaterial(planeMat);
			addChild(plane);

			mFilter = new TouchRippleFilter();
			mFilter.setRippleSize(62);*/
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			//mFilter.setTime((float) frameCount++ * .05f);
		}

		@Override
		public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
			super.onRenderSurfaceSizeChanged(gl, width, height);
			//mFilter.setScreenSize(width, height);
		}

        @Override
        public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
            super.onRenderSurfaceCreated(config, gl, width, height);
			/*
			for (int i = 0; i < NUM_CUBES; ++i) {
				registerAnimation(mAnims[i]);
				mAnims[i].play();
			}
			mFilter.setScreenSize(mViewportWidth, mViewportHeight);*/
		}

		public void setTouch(float x, float y) {
			//mFilter.addTouch(x, y, frameCount * .05f);
		}

	}

}
