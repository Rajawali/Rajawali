package org.rajawali3d.examples.examples.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;

public class UIElementsFragment extends AExampleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);

		TextView label = new TextView(getActivity());
		label.setText(R.string.ui_elements_fragment_text_view_halo_dunia);
		label.setTextSize(20);
		label.setGravity(Gravity.CENTER);
		label.setHeight(100);
		ll.addView(label);

		ImageView image = new ImageView(getActivity());
		image.setImageResource(R.drawable.rajawali_outline);
		ll.addView(image);

		mLayout.addView(ll);

		return mLayout;
	}

	@Override
    public AExampleRenderer createRenderer() {
		return new UIElementsRenderer(getActivity(), this);
	}

	private final class UIElementsRenderer extends AExampleRenderer {
		private DirectionalLight mLight;
		private Object3D mMonkey;

		public UIElementsRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(0, 0, -1);
			mLight.setPower(.8f);

			getCurrentScene().addLight(mLight);
			getCurrentCamera().setPosition(0, 0, 8);

			try {
                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parser.parse();

                mMonkey = parser.getParsedObject();

                getCurrentScene().addChild(mMonkey);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Material material = new Material();
			material.enableLighting(true);
			material.setDiffuseMethod(new DiffuseMethod.Lambert());
			material.setSpecularMethod(new SpecularMethod.Phong());
            mMonkey.setMaterial(material);
            mMonkey.setColor(0xff99C224);
		}
	}
}
