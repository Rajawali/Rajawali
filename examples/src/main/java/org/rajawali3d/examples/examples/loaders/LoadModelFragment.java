package org.rajawali3d.examples.examples.loaders;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;

public class LoadModelFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new LoadModelRenderer(getActivity(), this);
	}

	private final class LoadModelRenderer extends AExampleRenderer {
		private PointLight mLight;
		private Object3D mObjectGroup;
		private Animation3D mCameraAnim, mLightAnim;

		public LoadModelRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new PointLight();
			mLight.setPosition(0, 0, 4);
			mLight.setPower(3);

			getCurrentScene().addLight(mLight);
			getCurrentCamera().setZ(16);

			LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
												mTextureManager, R.raw.multiobjects_obj);
			try {
				objParser.parse();
				mObjectGroup = objParser.getParsedObject();
				getCurrentScene().addChild(mObjectGroup);

				mCameraAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
				mCameraAnim.setDurationMilliseconds(8000);
				mCameraAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
				mCameraAnim.setTransformable3D(mObjectGroup);
			} catch (ParsingException e) {
				e.printStackTrace();
			}

			mLightAnim = new EllipticalOrbitAnimation3D(new Vector3(),
					new Vector3(0, 10, 0), Vector3.getAxisVector(Vector3.Axis.Z), 0,
					360, EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);

			mLightAnim.setDurationMilliseconds(3000);
			mLightAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
			mLightAnim.setTransformable3D(mLight);

			getCurrentScene().registerAnimation(mCameraAnim);
			getCurrentScene().registerAnimation(mLightAnim);

			mCameraAnim.play();
			mLightAnim.play();
		}

	}

}
