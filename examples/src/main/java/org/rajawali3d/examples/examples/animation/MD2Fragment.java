package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import org.rajawali3d.animation.mesh.VertexAnimationObject3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderMD2;
import org.rajawali3d.loader.ParsingException;

public class MD2Fragment extends AExampleFragment implements OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout mLinearLayout = new LinearLayout(getActivity());
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		mLinearLayout.setGravity(Gravity.TOP);

		Button button = new Button(getActivity());
		button.setId(R.id.md2_fragment_button_salute);
		button.setOnClickListener(this);
		button.setText(R.string.md2_fragment_button_salute);
		button.setTextSize(10);
		mLinearLayout.addView(button);

		button = new Button(getActivity());
		button.setId(R.id.md2_fragment_button_death);
		button.setOnClickListener(this);
		button.setText(R.string.md2_fragment_button_death);
		button.setTextSize(10);
		mLinearLayout.addView(button);

		button = new Button(getActivity());
		button.setId(R.id.md2_fragment_button_walk);
		button.setOnClickListener(this);
		button.setText(R.string.md2_fragment_button_walk);
		button.setTextSize(10);
		mLinearLayout.addView(button);

		button = new Button(getActivity());
		button.setId(R.id.md2_fragment_button_wave);
		button.setOnClickListener(this);
		button.setText(R.string.md2_fragment_button_wave);
		button.setTextSize(10);
		mLinearLayout.addView(button);

		button = new Button(getActivity());
		button.setId(R.id.md2_fragment_button_loop_all);
		button.setOnClickListener(this);
		button.setText(R.string.md2_fragment_button_loop_all);
		button.setTextSize(10);
		mLinearLayout.addView(button);

		mLayout.addView(mLinearLayout);

		return mLayout;
	}

	@Override
	public void onClick(View v) {
		switch (((Button) v).getId()) {
		case R.id.md2_fragment_button_salute:
			((MD2Renderer) mRenderer).playAnimation("salute");
			break;
		case R.id.md2_fragment_button_death:
			((MD2Renderer) mRenderer).playAnimation("death1");
			break;
		case R.id.md2_fragment_button_walk:
			((MD2Renderer) mRenderer).playAnimation("crwalk");
			break;
		case R.id.md2_fragment_button_wave:
			((MD2Renderer) mRenderer).playAnimation("wave");
			break;
		case R.id.md2_fragment_button_loop_all:
			((MD2Renderer) mRenderer).playAnimation("loop all");
			break;
		}
	}

	@Override
    public AExampleRenderer createRenderer() {
		return new MD2Renderer(getActivity(), this);
	}

	private final class MD2Renderer extends AExampleRenderer {
		private DirectionalLight mLight;
		private VertexAnimationObject3D mOgre;

		public MD2Renderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

		public void playAnimation(String name) {
			if (name.equals("loop all")) {
				mOgre.play();
			} else {
				mOgre.play(name, false);
			}
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight();
			mLight.setPower(1);
            mLight.setLookAt(0, 0, 0);
            mLight.enableLookAt();
			getCurrentScene().addLight(mLight);
			getCurrentCamera().setPosition(0, 0, 8);

			LoaderMD2 parser = new LoaderMD2(mContext.getResources(),
					mTextureManager, R.raw.ogro);
			try {
				parser.parse();

				mOgre = (VertexAnimationObject3D) parser
						.getParsedAnimationObject();
                //mOgre.getMaterial().enableLighting(false);
				mOgre.setScale(.07f);
				mOgre.setY(-1);

				getCurrentScene().addChild(mOgre);

				mOgre.play();
			} catch (ParsingException e) {
				e.printStackTrace();
			}
		}

	}

}
