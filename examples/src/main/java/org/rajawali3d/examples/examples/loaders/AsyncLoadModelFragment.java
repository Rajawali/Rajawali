package org.rajawali3d.examples.examples.loaders;

import android.content.Context;
import androidx.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.util.RajLog;

public class AsyncLoadModelFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new LoadModelRenderer(getActivity(), this);
	}

	private final class LoadModelRenderer extends AExampleRenderer implements IAsyncLoaderCallback {
		private PointLight mLight;
        private Cube mBaseObject;
		private Animation3D mCameraAnim, mLightAnim;

		public LoadModelRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

		protected void initScene() {
			mLight = new PointLight();
			mLight.setPosition(0, 0, 4);
			mLight.setPower(3);

			getCurrentScene().addLight(mLight);
			getCurrentCamera().setZ(16);

            getCurrentScene().setBackgroundColor(0.7f, 0.7f, 0.7f, 1.0f);

            // Add the base object
            mBaseObject = new Cube(2.0f);
            mBaseObject.setPosition(-2.0, 3.0, 0.0);
            try {
                Material material = new Material();
                material.addTexture(new Texture("camdenTown", R.drawable.camden_town_alpha));
                material.setColorInfluence(0);
                mBaseObject.setMaterial(material);
                getCurrentScene().addChild(mBaseObject);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            //Begin loading
            final LoaderOBJ loaderOBJ = new LoaderOBJ(mContext.getResources(),
                mTextureManager, R.raw.multiobjects_obj);
            loadModel(loaderOBJ, this, R.raw.multiobjects_obj);

			mLightAnim = new EllipticalOrbitAnimation3D(new Vector3(),
					new Vector3(0, 10, 0), Vector3.getAxisVector(Vector3.Axis.Z), 0,
					360, EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);

			mLightAnim.setDurationMilliseconds(3000);
			mLightAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
			mLightAnim.setTransformable3D(mLight);

			getCurrentScene().registerAnimation(mLightAnim);
			mLightAnim.play();
		}

        @Override
        public void onModelLoadComplete(ALoader aLoader) {
            RajLog.d("Model load complete: " + aLoader);
            final LoaderOBJ obj = (LoaderOBJ) aLoader;
            final Object3D parsedObject = obj.getParsedObject();
            parsedObject.setPosition(Vector3.ZERO);
            getCurrentScene().addChild(parsedObject);

            mCameraAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
            mCameraAnim.setDurationMilliseconds(8000);
            mCameraAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
            mCameraAnim.setTransformable3D(parsedObject);

            getCurrentScene().registerAnimation(mCameraAnim);

            mCameraAnim.play();
        }

        @Override
        public void onModelLoadFailed(ALoader aLoader) {
            RajLog.e("Model load failed: " + aLoader);
        }
    }

}
