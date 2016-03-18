package org.rajawali3d.examples.examples.materials;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.examples.examples.materials.materials.CustomVertexShaderMaterialPlugin;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class CustomVertexShaderFragment extends AExampleFragment implements
		OnClickListener {

	@Override
    public AExampleRenderer createRenderer() {
		return new VertexShaderRenderer(getActivity(), this);
	}

	@Override
	public void onClick(View v) {
		((VertexShaderRenderer) mRenderer).toggleWireframe();
	}

	private final class VertexShaderRenderer extends AExampleRenderer {

		private int mFrameCount = 0;
		private Material mMaterial;
		private Object3D mSphere;

		public VertexShaderRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

		@Override
		protected void initScene() {
			mMaterial = new Material();
			mMaterial.enableTime(true);
			mMaterial.addPlugin(new CustomVertexShaderMaterialPlugin());
			mSphere = new Sphere(2, 60, 60);
			mSphere.setMaterial(mMaterial);
			getCurrentScene().addChild(mSphere);

			Vector3 axis = new Vector3(2, 4, 1);
			axis.normalize();

			RotateOnAxisAnimation mAnim = new RotateOnAxisAnimation(axis, 360);
			mAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
			mAnim.setDurationMilliseconds(12000);
			mAnim.setTransformable3D(mSphere);
			getCurrentScene().registerAnimation(mAnim);
			mAnim.play();

			getCurrentCamera().setPosition(0, 0, 10);
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			mMaterial.setTime((float) mFrameCount++);
		}

		public void toggleWireframe() {
			mSphere.setDrawingMode(mSphere.getDrawingMode() == GLES20.GL_TRIANGLES ? GLES20.GL_LINES
					: GLES20.GL_TRIANGLES);
		}
	}
}
