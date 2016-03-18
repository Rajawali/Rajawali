package org.rajawali3d.examples.examples.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.examples.examples.materials.materials.CustomMaterialPlugin;
import org.rajawali3d.materials.Material;
import org.rajawali3d.primitives.ScreenQuad;

public class TwoDimensionalFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new TwoDimensionalRenderer(getActivity(), this);
	}

	private final class TwoDimensionalRenderer extends AExampleRenderer {

		private float mTime;
		private Material mCustomMaterial;

		public TwoDimensionalRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mCustomMaterial = new Material();
			mCustomMaterial.enableTime(true);
			mCustomMaterial.addPlugin(new CustomMaterialPlugin());

			ScreenQuad screenQuad = new ScreenQuad();
			screenQuad.setMaterial(mCustomMaterial);
			getCurrentScene().addChild(screenQuad);
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			mTime += .007f;
			mCustomMaterial.setTime(mTime);
		}

	}

}
