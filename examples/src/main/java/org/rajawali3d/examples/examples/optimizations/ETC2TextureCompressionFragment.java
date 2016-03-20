package org.rajawali3d.examples.examples.optimizations;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.examples.examples.ExceptionDialog;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Etc1Texture;
import org.rajawali3d.materials.textures.Etc2Texture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.util.Capabilities;

public class ETC2TextureCompressionFragment extends AExampleFragment {

    @Override
    public AExampleRenderer createRenderer() {
		return new ETC2TextureCompression(getActivity(), this);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.etc2_overlay, mLayout, true);
        mLayout.findViewById(R.id.etc2_overlay).bringToFront();
        return mLayout;
    }

    private void showExceptionDialog(String title, String message) {
        ExceptionDialog exceptionDialog = ExceptionDialog.newInstance(title, message);
        exceptionDialog.show(getFragmentManager(), ExceptionDialog.TAG);
    }

	private final class ETC2TextureCompression extends AExampleRenderer {
		private Object3D mPNGPlane;
        private Object3D mETC1Plane;
        private Object3D mETC2Plane;

		public ETC2TextureCompression(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
            if (Capabilities.getGLESMajorVersion() < 3) {
                showExceptionDialog("ETC2 Not Supported", "This device does not support OpenGL ES 3.0 and cannot use ETC2 textures.");
                return;
            }

			getCurrentCamera().setPosition(0, 0, 7);

            try {
                // This is a raw PNG image
                Texture texture0 = new Texture("png", R.drawable.rectangles);
                Material material0 = new Material();
                material0.addTexture(texture0);
                material0.setColorInfluence(0);
                mPNGPlane = new Plane(1.5f, 1.5f, 1, 1);
                mPNGPlane.setMaterial(material0);
                mPNGPlane.setPosition(0, -1.75f, 0);
                getCurrentScene().addChild(mPNGPlane);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

			try {
                // This is an ETC1 image
				Texture texture1 = new Texture("etc1", new Etc1Texture("etc1Tex", R.raw.rectangles_etc1, null));
				Material material1 = new Material();
				material1.addTexture(texture1);
				material1.setColorInfluence(0);
				mETC1Plane = new Plane(1.5f, 1.5f, 1, 1);
				mETC1Plane.setMaterial(material1);
				mETC1Plane.setPosition(0, 0, 0);
				getCurrentScene().addChild(mETC1Plane);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			try {
                // This is an ETC2 image
				Texture texture2 = new Texture("etc2", new Etc2Texture("etc2Tex", R.raw.rectangles_etc2, null));

				Material material2 = new Material();
				material2.addTexture(texture2);
				material2.setColorInfluence(0);

				mETC2Plane = new Plane(1.5f, 1.5f, 1, 1);
				mETC2Plane.setMaterial(material2);
				mETC2Plane.setPosition(0, 1.75f, 0);
				getCurrentScene().addChild(mETC2Plane);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}
		}

    }
}
