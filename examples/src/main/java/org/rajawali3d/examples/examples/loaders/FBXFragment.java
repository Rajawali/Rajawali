package org.rajawali3d.examples.examples.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.fbx.LoaderFBX;
import org.rajawali3d.math.vector.Vector3;

public class FBXFragment extends AExampleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.BOTTOM);

		TextView label = new TextView(getActivity());
		label.setText(R.string.fbx_fragment_button_model_by);
		label.setTextSize(20);
		label.setGravity(Gravity.CENTER);
		label.setHeight(100);
		ll.addView(label);

		mLayout.addView(ll);

		return mLayout;
	}

	@Override
    public AExampleRenderer createRenderer() {
		return new FBXRenderer(getActivity(), this);
	}

	private final class FBXRenderer extends AExampleRenderer {
		private Animation3D mAnim;

		public FBXRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
			mAnim.setDurationMilliseconds(16000);
			mAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
			getCurrentScene().registerAnimation(mAnim);

			try {
				// -- Model by Sampo Rask
				// (http://www.blendswap.com/blends/characters/low-poly-rocks-character/)
				LoaderFBX parser = new LoaderFBX(this,
						R.raw.lowpolyrocks_character_blendswap);
				parser.parse();
				Object3D o = parser.getParsedObject();
				o.setY(-.5f);
				getCurrentScene().addChild(o);

				mAnim.setTransformable3D(o);
				mAnim.play();
			} catch (ParsingException e) {
				e.printStackTrace();
			}
		}

	}

}
