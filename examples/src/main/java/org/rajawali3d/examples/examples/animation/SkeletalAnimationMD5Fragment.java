package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import androidx.annotation.Nullable;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;

public class SkeletalAnimationMD5Fragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new SkeletalAnimationMD5Renderer(getActivity(), this);
	}

	private final class SkeletalAnimationMD5Renderer extends AExampleRenderer {
		private DirectionalLight mLight;
		private SkeletalAnimationObject3D mObject;

		public SkeletalAnimationMD5Renderer(Context context, @Nullable AExampleFragment fragment) {
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
				LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this,
															 R.raw.boblampclean_mesh);
				meshParser.parse();

				LoaderMD5Anim animParser = new LoaderMD5Anim("attack2", this,
						R.raw.boblampclean_anim);
				animParser.parse();

				SkeletalAnimationSequence sequence = (SkeletalAnimationSequence) animParser
						.getParsedAnimationSequence();

				mObject = (SkeletalAnimationObject3D) meshParser
						.getParsedAnimationObject();
				mObject.setAnimationSequence(sequence);
				mObject.setScale(.04f);
				mObject.play();

				getCurrentScene().addChild(mObject);
			} catch (ParsingException e) {
				e.printStackTrace();
			}
		}

	}
}
