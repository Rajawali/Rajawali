package org.rajawali3d.examples.examples.interactive;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.util.GLU;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;
import org.rajawali3d.util.RajLog;

import javax.microedition.khronos.opengles.GL10;

public class TouchAndDragFragment extends AExampleFragment implements
		OnTouchListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
        ((View) mRenderSurface).setOnTouchListener(this);
		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.BOTTOM);

		TextView label = new TextView(getActivity());
		label.setText(R.string.touch_and_drag_fragment_info);
		label.setTextSize(20);
		label.setGravity(Gravity.CENTER_HORIZONTAL);
		label.setHeight(100);
		ll.addView(label);

		mLayout.addView(ll);

		return mLayout;
	}

	@Override
    public AExampleRenderer createRenderer() {
		return new TouchAndDragRenderer(getActivity(), this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			((TouchAndDragRenderer) mRenderer).getObjectAt(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			((TouchAndDragRenderer) mRenderer).moveSelectedObject(event.getX(),
					event.getY());
			break;
		case MotionEvent.ACTION_UP:
			((TouchAndDragRenderer) mRenderer).stopMovingSelectedObject();
			break;
		}
		return true;
	}

	private final class TouchAndDragRenderer extends AExampleRenderer implements
        OnObjectPickedListener {
		private ObjectColorPicker mPicker;
		private Object3D mSelectedObject;
		private int[] mViewport;
		private double[] mNearPos4;
		private double[] mFarPos4;
		private Vector3 mNearPos;
		private Vector3 mFarPos;
		private Vector3 mNewObjPos;
		private Matrix4 mViewMatrix;
		private Matrix4 mProjectionMatrix;

		public TouchAndDragRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mViewport = new int[] { 0, 0, getViewportWidth(), getViewportHeight() };
			mNearPos4 = new double[4];
			mFarPos4 = new double[4];
			mNearPos = new Vector3();
			mFarPos = new Vector3();
			mNewObjPos = new Vector3();
			mViewMatrix = getCurrentCamera().getViewMatrix();
			mProjectionMatrix = getCurrentCamera().getProjectionMatrix();

			mPicker = new ObjectColorPicker(this);
			mPicker.setOnObjectPickedListener(this);

			try {
				DirectionalLight light= new DirectionalLight(-1, 0, -1);
				light.setPower(1.5f);
				getCurrentScene().addLight(light);
				getCurrentCamera().setPosition(0, 0, 4);

				Material material = new Material();
				material.enableLighting(true);
				material.setDiffuseMethod(new DiffuseMethod.Lambert());

				for (int i = 0; i < 20; i++) {
					Sphere sphere = new Sphere(.3f, 12, 12);
					sphere.setMaterial(material);
					sphere.setColor(0x333333 + (int) (Math.random() * 0xcccccc));
					sphere.setX(-4 + (Math.random() * 8));
					sphere.setY(-4 + (Math.random() * 8));
					sphere.setZ(-2 + (Math.random() * -6));
					sphere.setDrawingMode(GLES20.GL_TRIANGLES);
					mPicker.registerObject(sphere);
					getCurrentScene().addChild(sphere);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
			super.onRenderSurfaceSizeChanged(gl, width, height);
			mViewport[2] = getViewportWidth();
			mViewport[3] = getViewportHeight();
			mViewMatrix = getCurrentCamera().getViewMatrix();
			mProjectionMatrix = getCurrentCamera().getProjectionMatrix();
		}

		public void getObjectAt(float x, float y) {
			mPicker.getObjectAt(x, y);
		}

		public void onObjectPicked(@NonNull Object3D object) {
			mSelectedObject = object;
		}

		@Override
		public void onNoObjectPicked() {
			RajLog.w("No object picked!");
		}

		public void moveSelectedObject(float x, float y) {
			if (mSelectedObject == null)
				return;

			//
			// -- unproject the screen coordinate (2D) to the camera's near plane
			//

			GLU.gluUnProject(x, getViewportHeight() - y, 0, mViewMatrix.getDoubleValues(), 0,
                mProjectionMatrix.getDoubleValues(), 0, mViewport, 0, mNearPos4, 0);

			//
			// -- unproject the screen coordinate (2D) to the camera's far plane
			//

			GLU.gluUnProject(x, getViewportHeight() - y, 1.f, mViewMatrix.getDoubleValues(), 0,
					mProjectionMatrix.getDoubleValues(), 0, mViewport, 0, mFarPos4, 0);

			//
			// -- transform 4D coordinates (x, y, z, w) to 3D (x, y, z) by dividing
			// each coordinate (x, y, z) by w.
			//

			mNearPos.setAll(mNearPos4[0] / mNearPos4[3], mNearPos4[1]
					/ mNearPos4[3], mNearPos4[2] / mNearPos4[3]);
			mFarPos.setAll(mFarPos4[0] / mFarPos4[3],
					mFarPos4[1] / mFarPos4[3], mFarPos4[2] / mFarPos4[3]);

			//
			// -- now get the coordinates for the selected object
			//

			double factor = (Math.abs(mSelectedObject.getZ()) + mNearPos.z)
					/ (getCurrentCamera().getFarPlane() - getCurrentCamera()
							.getNearPlane());

			mNewObjPos.setAll(mFarPos);
			mNewObjPos.subtract(mNearPos);
			mNewObjPos.multiply(factor);
			mNewObjPos.add(mNearPos);

			mSelectedObject.setX(mNewObjPos.x);
			mSelectedObject.setY(mNewObjPos.y);
		}

		public void stopMovingSelectedObject() {
			mSelectedObject = null;
		}

	}

}
