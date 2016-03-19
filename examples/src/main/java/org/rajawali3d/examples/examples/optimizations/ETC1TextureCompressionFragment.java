package org.rajawali3d.examples.examples.optimizations;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Etc1Texture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;

public class ETC1TextureCompressionFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new ETC1TextureCompression(getActivity(), this);
	}

	private final class ETC1TextureCompression extends AExampleRenderer {
		private Object3D mMipmappedPlane;
		private Object3D mPlane;

		public ETC1TextureCompression(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			getCurrentCamera().setPosition(0, 0, 7);

			try {
				Texture texture1 = new Texture("texture1", new Etc1Texture(
						"etc1Tex1", R.raw.rajawali_tex_mip_0, null));
				Material material1 = new Material();
				material1.addTexture(texture1);
				material1.setColorInfluence(0);
				mPlane = new Plane(2, 2, 1, 1);
				mPlane.setMaterial(material1);
				mPlane.setPosition(0, -1.25f, 0);
				mPlane.setDoubleSided(true);
				getCurrentScene().addChild(mPlane);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			try {
				int[] resourceIds = new int[] { R.raw.rajawali_tex_mip_0,
						R.raw.rajawali_tex_mip_1, R.raw.rajawali_tex_mip_2,
						R.raw.rajawali_tex_mip_3, R.raw.rajawali_tex_mip_4,
						R.raw.rajawali_tex_mip_5, R.raw.rajawali_tex_mip_6,
						R.raw.rajawali_tex_mip_7, R.raw.rajawali_tex_mip_8,
						R.raw.rajawali_tex_mip_9 };
				Texture texture2 = new Texture("texture2", new Etc1Texture(
						"etc1Tex2", resourceIds));

				Material material2 = new Material();
				material2.addTexture(texture2);
				material2.setColorInfluence(0);

				mMipmappedPlane = new Plane(2, 2, 1, 1);
				mMipmappedPlane.setMaterial(material2);
				mMipmappedPlane.setPosition(0, 1.25f, 0);
				mMipmappedPlane.setDoubleSided(true);
				getCurrentScene().addChild(mMipmappedPlane);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			// Rotate the plane to showcase difference between a mipmapped
			// texture and non-mipmapped texture.
			if (mMipmappedPlane != null) {
				mMipmappedPlane.setRotX(mMipmappedPlane.getRotX() - 0.1f);
				mMipmappedPlane.setRotY(mMipmappedPlane.getRotY() - 0.1f);
			}
			if (mPlane != null) {
				mPlane.setRotX(mPlane.getRotX() + 0.1f);
				mPlane.setRotY(mPlane.getRotY() + 0.1f);
			}
		}

	}

}
