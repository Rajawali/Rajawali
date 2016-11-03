package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.support.annotation.Nullable;
import c.org.rajawali3d.textures.TextureDataReference;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import c.org.rajawali3d.textures.BaseTexture;
import c.org.rajawali3d.textures.Texture2D;
import c.org.rajawali3d.textures.TextureException;
import org.rajawali3d.primitives.ScreenQuad;

public class ThreeSixtyImagesFragment extends AExampleFragment {

    @Override
    public AExampleRenderer createRenderer() {
        return new ThreeSixtyImagesRenderer(getActivity(), this);
    }

    private final class ThreeSixtyImagesRenderer extends AExampleRenderer {
        private BaseTexture[] mTextures;
        private ScreenQuad    mScreenQuad;
        private int           mFrameCount;
        private Material      mMaterial;
        private final static int NUM_TEXTURES = 80;

        public ThreeSixtyImagesRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            if (mTextureManager != null)
            //mTextureManager.reset();
            {
                if (mMaterial != null) {
                    mMaterial.getTextureList().clear();
                }
            }

            getCurrentScene().setBackgroundColor(0xffffff);

            mMaterial = new Material();

            mScreenQuad = new ScreenQuad();
            mScreenQuad.setMaterial(mMaterial);
            getCurrentScene().addChild(mScreenQuad);

            if (mTextures == null) {
                // -- create an array that will contain all TextureInfo objects
                mTextures = new BaseTexture[NUM_TEXTURES];
            }
            mFrameCount = 0;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inInputShareable = true;

            for (int i = 1; i <= NUM_TEXTURES; ++i) {
                // -- load all the textures from the drawable folder
                int resourceId = mContext.getResources().getIdentifier(
                        i < 10 ? "m0" + i : "m" + i, "drawable",
                        "org.rajawali3d.examples");

                Bitmap bitmap = BitmapFactory.decodeResource(
                        mContext.getResources(), resourceId, options);

                BaseTexture texture = new Texture2D("bm" + i, new TextureDataReference(bitmap, null, GLES20.GL_RGBA,
                                                                                       GLES20.GL_UNSIGNED_BYTE,
                                                                                       bitmap.getWidth(),
                                                                                       bitmap.getHeight()));
                texture.setMipmaped(false);
                texture.willRecycle(true);
                //mTextures[i - 1] = mTextureManager.addTexture(texture);
            }
            try {
                mMaterial.addTexture(mTextures[0]);
                mMaterial.setColorInfluence(0);
            } catch (TextureException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
            // -- get the texture info list and remove the previous TextureInfo object
            mMaterial.getTextureList().remove(
                    mTextures[mFrameCount++ % NUM_TEXTURES]);
            // -- add a new TextureInfo object
            mMaterial.getTextureList().add(
                    mTextures[mFrameCount % NUM_TEXTURES]);
        }

    }

}
