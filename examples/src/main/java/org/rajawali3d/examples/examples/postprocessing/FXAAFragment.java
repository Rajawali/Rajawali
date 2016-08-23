package org.rajawali3d.examples.examples.postprocessing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.passes.FXAAPass;
import org.rajawali3d.postprocessing.passes.RenderPass;

public class FXAAFragment extends AExampleFragment {

    boolean fxaaEnabled = true;

	@Override
    public AExampleRenderer createRenderer() {
		return new FXAARenderer(getActivity(), this);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.fxaa_button_bar, mLayout, true);

        final Button button = (Button) mLayout.findViewById(R.id.button1);
        button.setText(R.string.fxaa_fragment_button_disable_fxaa);
        button.setTextSize(20);
        button.setGravity(Gravity.CENTER);
        button.setHeight(100);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fxaaEnabled = !fxaaEnabled;
                if (fxaaEnabled) {
                    ((FXAARenderer) mRenderer).enableFXAA();
                    button.setText(R.string.fxaa_fragment_button_disable_fxaa);
                } else {
                    ((FXAARenderer) mRenderer).disableFXAA();
                    button.setText(R.string.fxaa_fragment_button_enable_fxaa);
                }
            }
        });

        return mLayout;
    }

    private final class FXAARenderer extends AExampleRenderer {
		private PostProcessingManager mEffects;
        private RenderPass mRenderPass;
        private FXAAPass mFXAAPass;

		public FXAARenderer(Context context, @Nullable AExampleFragment fragment) {
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
				//anim.play();
				//getCurrentScene().registerAnimation(anim);

				//
				// -- Create a post processing manager. We can add multiple passes to this.
				//

				mEffects = new PostProcessingManager(this, 1.3333);
				mRenderPass = new RenderPass(getCurrentScene(), getCurrentCamera(), 0);
				mEffects.addPass(mRenderPass);

                mFXAAPass = new FXAAPass();
                mFXAAPass.setRenderToScreen(true);
                mEffects.addPass(mFXAAPass);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

        @Override
        public void onRender(final long ellapsedTime, final double deltaTime) {
            mEffects.render(ellapsedTime, deltaTime);
        }

        public void enableFXAA() {
            mRenderPass.setRenderToScreen(false);
            mFXAAPass.setRenderToScreen(true);
            mEffects.addPass(mFXAAPass);
        }

        public void disableFXAA() {
            mFXAAPass.setRenderToScreen(false);
            mRenderPass.setRenderToScreen(true);
            mEffects.removePass(mFXAAPass);
        }
	}

}
