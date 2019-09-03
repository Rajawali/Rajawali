package org.rajawali3d.examples.examples.general;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class BasicFragment extends AExampleFragment {

	private static final String TAG = "BasicFragment";

	@Override
    public AExampleRenderer createRenderer() {
		return new BasicRenderer(getActivity(), this);
	}

	public static final class BasicRenderer extends AExampleRenderer {

		private Object3D mSphere;

		public BasicRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
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

			Log.d(TAG, "Camera initial orientation: " + getCurrentCamera().getOrientation());
            getCurrentCamera().enableLookAt();
            getCurrentCamera().setLookAt(0, 0, 0);
            getCurrentCamera().setZ(6);
			getCurrentCamera().setOrientation(getCurrentCamera().getOrientation().inverse());
        }

        @Override
        public void onRender(final long elapsedTime, final double deltaTime) {
			super.onRender(elapsedTime, deltaTime);
			mSphere.rotate(Vector3.Axis.Y, 1.0);
		}
	}
}
