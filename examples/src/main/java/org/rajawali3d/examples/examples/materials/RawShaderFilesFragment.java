package org.rajawali3d.examples.examples.materials;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.examples.examples.materials.materials.CustomRawFragmentShader;
import org.rajawali3d.examples.examples.materials.materials.CustomRawVertexShader;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Sphere;

public class RawShaderFilesFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new RawShaderFilesRenderer(getActivity(), this);
	}

	public class RawShaderFilesRenderer extends AExampleRenderer {
		private DirectionalLight mLight;
		private Object3D mCube;
		private float mTime;
		private Material mMaterial;

		public RawShaderFilesRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(0, 1, 1);

			getCurrentScene().addLight(mLight);

			mMaterial = new Material(new CustomRawVertexShader(), new CustomRawFragmentShader());
			mMaterial.enableTime(true);
			try {
				Texture texture = new Texture("myTex", R.drawable.flickrpics);
				texture.setInfluence(.5f);
				mMaterial.addTexture(texture);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}
			mMaterial.setColorInfluence(.5f);
			mCube = new Sphere(2, 64, 64);
			mCube.setMaterial(mMaterial);
			getCurrentScene().addChild(mCube);

			getCurrentCamera().setPosition(0, 0, 10);

			mTime = 0;
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			mTime += .007f;
			mMaterial.setTime(mTime);
		}
	}

}
