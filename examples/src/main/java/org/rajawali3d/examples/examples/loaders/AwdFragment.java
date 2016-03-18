package org.rajawali3d.examples.examples.loaders;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.math.vector.Vector3;

public class AwdFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new AwdRenderer(getActivity(), this);
	}

	private final class AwdRenderer extends AExampleRenderer {

		public AwdRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

		@Override
		protected void initScene() {

			try {
				final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_arrows);
				parser.parse();

				final Object3D obj = parser.getParsedObject();

				obj.setScale(0.25f);
				getCurrentScene().addChild(obj);

				final Animation3D anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -360);
				anim.setDurationDelta(4d);
				anim.setRepeatMode(Animation.RepeatMode.INFINITE);
				anim.setTransformable3D(obj);
				anim.play();
				getCurrentScene().registerAnimation(anim);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
