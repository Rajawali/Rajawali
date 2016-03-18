package org.rajawali3d.examples.examples.optimizations;

import android.content.Context;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import org.rajawali3d.BufferInfo;
import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateAroundAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class UpdateVertexBufferFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new UpdateVertexBufferRenderer(getActivity(), this);
	}

	public static final class UpdateVertexBufferRenderer extends AExampleRenderer {

		/**
		 * The size of the vertex buffer for the dynamic object
		 */
		private final int NUM_VERTICES = 240;
		/**
		 * The curve that will determine the shape for the dynamic object
		 */
		private CatmullRomCurve3D mCurve;
		/**
		 * The interpolated normalized time. This will be used to get the {@link Vector3}s from the
		 * curve
		 */
		private float mInterpolation;
		/**
		 * The start time will be used to calculate the delta time. This will allow us to have frame
		 * rate independent constant animation speed.
		 */
		private long mStartTime;
		/**
		 * The dynamic object
		 */
		private Object3D mCurveTris;
		/**
		 * The dynamic object's vertex buffer info. We'll need this reference to update the vertex
		 * buffer on each frame.
		 */
		private BufferInfo mVertexBufferInfo;
		/**
		 * The vertex buffer that will store the vertices that we'll generate based on the spline
		 * and some randomness
		 */
		private FloatBuffer mVertexBuffer;
		/**
		 * Keeps track of the vertex position in the object's vertex buffer
		 */
		private int mCurrentVertexIndex;
		/**
		 * This vertex will be generated on each frame
		 */
		private Vector3 mTmpVec;
		/**
		 * Stores a vertex that was used by the previous triangle. This way we can connect all
		 * vertices and create a ribbon.
		 */
		private Vector3 mPrevVec1;
		/**
		 * Stores a vertex that was used by the previous triangle. This way we can connect all
		 * vertices and create a ribbon.
		 */
		private Vector3 mPrevVec2;

		public UpdateVertexBufferRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
			mTmpVec = new Vector3();
		}

        @Override
		public void initScene() {
			getCurrentScene().setBackgroundColor(0xffffff);

			//
			// -- Create curve. The points are random within a certain limit.
			//

			mCurve = new CatmullRomCurve3D();
			float r = 6;
			float rh = r * .5f;

			for (int i = 0; i < 128; i++) {
				mCurve.addPoint(new Vector3(-rh + (Math.random() * r), -rh
						+ (Math.random() * r), -rh + (Math.random() * r)));
			}
			mCurve.addPoint(mCurve.getPoint(1));
			mCurve.addPoint(mCurve.getPoint(0));

			//
			// -- Create buffer data. Vertices, indices and colors is all we'll need.
			// All vertices will be initialized to (0, 0, 0) so they won't be visible
			// on application startup.
			//

			float[] vertices = new float[NUM_VERTICES * 3];
			int[] indices = new int[NUM_VERTICES];
			float[] colors = new float[NUM_VERTICES * 4];

			for (int i = 0; i < NUM_VERTICES; i++) {
				indices[i] = i;
				int index = i * 4;
				colors[index] = (float) Math.random();
				colors[index + 1] = (float) Math.random();
				colors[index + 2] = (float) Math.random();
				colors[index + 3] = (float) Math.random();
			}

			//
			// -- Create an empty object
			//

			mCurveTris = new Object3D();
			mCurveTris.setData(vertices, null, null, colors, indices, true);
			// -- We need to set this or else we won't see anyting
			mCurveTris.isContainer(false);
			// -- Vertices have some randomness and no normals so we need
			// to set this to true
			mCurveTris.setDoubleSided(true);
			mCurveTris.setTransparent(true);

			Material material = new Material();
			material.useVertexColors(true);
			mCurveTris.setMaterial(material);
			// -- Get the BufferInfo object. We'll need this reference to update the
			// vertices on each frame.
			mVertexBufferInfo = mCurveTris.getGeometry().getVertexBufferInfo();
			// -- Create a FloatBuffer that can hold a single triangle (3 vertices).
			// We'll create a new triangle on each frame so that's why the size
			// of this buffer is restricted to one triangle.
			mVertexBuffer = ByteBuffer
					.allocateDirect(9 * Geometry3D.FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			// -- Indicate that we are going to update the vertex buffer very frequently
			mCurveTris.getGeometry().changeBufferUsage(mVertexBufferInfo,
					GLES20.GL_DYNAMIC_DRAW);

			getCurrentScene().addChild(mCurveTris);

			mStartTime = SystemClock.elapsedRealtime();

			getCurrentCamera().setZ(10);
			getCurrentCamera().setLookAt(0, 0, 0);
            getCurrentCamera().enableLookAt();

			//
			// -- Animate the camera
			//

			RotateAroundAnimation3D anim = new RotateAroundAnimation3D(
					new Vector3(), Vector3.Axis.Y, 10);
			anim.setDurationMilliseconds(200000);
			anim.setTransformable3D(getCurrentCamera());
			anim.setRepeatMode(Animation.RepeatMode.INFINITE);
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			//
			// -- Calculate interpolation
			//

			final double updateTime = (SystemClock.elapsedRealtime() - mStartTime) / 1000d;
			mInterpolation += updateTime / 20f;

			// -- Get the current curve position

			Vector3 point = new Vector3();
			mCurve.calculatePoint(point, mInterpolation);

			if (mPrevVec1 == null) {
				// -- This is the first triangle.
				mPrevVec1 = new Vector3();
				generateNextVertex(mPrevVec1, point);
				mPrevVec2 = new Vector3();
				generateNextVertex(mPrevVec2, point);
			}

			// -- Generate a new vertex

			generateNextVertex(mTmpVec, point);

			// -- Add the vertices to the FloatBuffer that can hold a single triangle.

			addVertexToBuffer(mPrevVec1, 0);
			addVertexToBuffer(mPrevVec2, 1);
			addVertexToBuffer(mTmpVec, 2);

			// -- Update the vertex buffer. The first parameter is a reference to the vertex
			// buffer information. The second parameter is the FloatBuffer that holds the new
			// triangle. The third parameter is the vertex offset in the object's vertex buffer
			// the fourth parameter indicates how many vertices we will be updating.

			mCurveTris.getGeometry().changeBufferData(mVertexBufferInfo,
					mVertexBuffer, mCurrentVertexIndex * 3, 9);

			if (mInterpolation > 1)
				mInterpolation = 0;
			mCurrentVertexIndex += 3;
			if (mCurrentVertexIndex >= NUM_VERTICES)
				mCurrentVertexIndex = 0;
			mStartTime = SystemClock.elapsedRealtime();

			mPrevVec1.setAll(mPrevVec2);
			mPrevVec2.setAll(mTmpVec);
		}

		private void generateRandomVertex(Vector3 vertex, float size) {
			float halfSize = size * .5f;
			vertex.x = -halfSize + (float) (Math.random() * size);
			vertex.y = -halfSize + (float) (Math.random() * size);
			vertex.z = -halfSize + (float) (Math.random() * size);
		}

		private void generateNextVertex(Vector3 target, Vector3 curvePoint) {
			generateRandomVertex(target, .1f + (float) (Math.random() * .4f));
			target.add(curvePoint);
		}

		private void addVertexToBuffer(Vector3 vertex, int index) {
			int vertIndex = index * 3;

			mVertexBuffer.put(vertIndex, (float) vertex.x);
			mVertexBuffer.put(vertIndex + 1, (float) vertex.y);
			mVertexBuffer.put(vertIndex + 2, (float) vertex.z);
		}

	}

}
