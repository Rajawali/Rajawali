package org.rajawali3d.examples.examples.postprocessing;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;

public class FogFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new FogRenderer(getActivity(), this);
	}

	private final class FogRenderer extends AExampleRenderer {
		private DirectionalLight mLight;
		private Object3D mRoad;

		public FogRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(0, -1, -1);
			mLight.setPower(.5f);

			getCurrentScene().addLight(mLight);

			int fogColor = 0x999999;

			getCurrentScene().setBackgroundColor(fogColor);
			getCurrentScene().setFog(new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR, fogColor, 1, 15));

			LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
												mTextureManager, R.raw.road);
			try {
				objParser.parse();
				mRoad = objParser.getParsedObject();
				mRoad.setZ(5);
				mRoad.setRotY(180);
				getCurrentScene().addChild(mRoad);

				Material roadMaterial = new Material();
				roadMaterial.enableLighting(true);
				roadMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
				roadMaterial.addTexture(new Texture("roadTex", R.drawable.road));
				roadMaterial.setColorInfluence(0);
				mRoad.getChildByName("Road").setMaterial(roadMaterial);

				Material signMaterial = new Material();
				signMaterial.enableLighting(true);
				signMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
				signMaterial.addTexture(new Texture("rajawaliSign", R.drawable.sign));
				signMaterial.setColorInfluence(0);
				mRoad.getChildByName("WarningSign").setMaterial(signMaterial);

				Material warningMaterial = new Material();
				warningMaterial.enableLighting(true);
				warningMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
				warningMaterial.addTexture(new Texture("warning", R.drawable.warning));
				warningMaterial.setColorInfluence(0);
				mRoad.getChildByName("Warning").setMaterial(warningMaterial);
			} catch (Exception e) {
				e.printStackTrace();
			}

			TranslateAnimation3D camAnim = new TranslateAnimation3D(
					new Vector3(0, 2, 0),
					new Vector3(0, 2, -23));
			camAnim.setDurationMilliseconds(8000);
			camAnim.setInterpolator(new AccelerateDecelerateInterpolator());
			camAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			camAnim.setTransformable3D(getCurrentCamera());
			getCurrentScene().registerAnimation(camAnim);
			camAnim.play();
		}
	}
}
