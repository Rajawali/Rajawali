package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.view.MotionEvent;
import org.rajawali3d.Object3D;
import org.rajawali3d.debug.GLDebugger;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DebugRendererFragment extends AExampleFragment {

	@Override
    public Renderer createRenderer() {
        final GLDebugger.Builder builder = new GLDebugger.Builder();
        builder.checkAllGLErrors();
        builder.checkSameThread();
        builder.enableLogArgumentNames();
		return new DebugRenderer(getActivity(), builder);
	}

	private final class DebugRenderer extends org.rajawali3d.debug.DebugRenderer {

		private DirectionalLight mLight;
		private Object3D mSphere;

		public DebugRenderer(Context context, GLDebugger.Builder config) {
            super(context, config, false);
            setFrameRate(60);
		}

        @Override
        public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
            showLoader();
            super.onRenderSurfaceCreated(config, gl, width, height);
            hideLoader();
        }

        @Override
        public void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {

        }

        @Override
        public void onTouchEvent(MotionEvent event) {

        }

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(1.0, 0.2, -1.0); // set the direction
			mLight.setColor(1.0f, 1.0f, 1.0f);
			mLight.setPower(2);

			getCurrentScene().addLight(mLight);

			try {
				Material material = new Material();
				material.addTexture(new Texture("earthColors",
												R.drawable.earthtruecolor_nasa_big));
				material.setColorInfluence(0);
				mSphere = new Sphere(1, 24, 24);
				mSphere.setMaterial(material);
				getCurrentScene().addChild(mSphere);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			getCurrentCamera().setLookAt(0, 0, 0);
			getCurrentCamera().setZ(6);
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
            mSphere.rotate(Vector3.Axis.Y, 1.0);
        }
	}
}
