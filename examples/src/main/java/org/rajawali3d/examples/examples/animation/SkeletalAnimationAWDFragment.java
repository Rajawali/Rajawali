package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;

public class SkeletalAnimationAWDFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new SkeletalAnimationAWDRenderer(getActivity(), this);
	}

	private final class SkeletalAnimationAWDRenderer extends AExampleRenderer {
		private DirectionalLight mLight;
		private SkeletalAnimationObject3D mObject;

		public SkeletalAnimationAWDRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(0, -0.2f, -1.0f); // set the direction
			mLight.setColor(1.0f, 1.0f, 1.0f);
			mLight.setPower(2);

			getCurrentScene().addLight(mLight);
			getCurrentCamera().setY(1);
			getCurrentCamera().setZ(6);

			try {
				final LoaderAWD parser =
					new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.boblampclean_anim_awd);

				parser.parse();

				mObject = (SkeletalAnimationObject3D) parser.getParsedObject();
                mObject.rotate(Vector3.Y, -90.0);

				mObject.setAnimationSequence(0);
				mObject.setScale(.04f);
				mObject.play();

				getCurrentScene().addChild(mObject);
			} catch (ParsingException e) {
				e.printStackTrace();
			}
		}

	}
}
