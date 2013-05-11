package rajawali.effects;

import android.graphics.Color;
import android.opengl.GLES20;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import rajawali.util.ObjectColorPicker.ObjectColorPickerException;

public class RenderPass extends APass {
	protected RajawaliScene mScene;
	protected int mClearColor;
	protected int mOldClearColor;
	
	public RenderPass(RajawaliScene scene, int clearColor, float clearAlpha) {
		mScene = scene;
		mClearColor = clearColor;
		mOldClearColor = 0x00000000;
		
		mEnabled = true;
		mClear = true;
		mNeedsSwap = false;
	}
	
	public void render(RajawaliRenderer renderer, RenderTarget writeBuffer, RenderTarget readBuffer, double deltaTime) throws ObjectColorPickerException {
		// Set the background color with that of current render pass.
		if (mClearColor != 0x00000000) {
			mOldClearColor = renderer.getCurrentScene().getBackgroundColor();
			GLES20.glClearColor(Color.red(mClearColor)/255f, Color.green(mClearColor)/255f, Color.blue(mClearColor)/255f, Color.alpha(mClearColor)/255f);
		}
		
		// Render the current scene.
		mScene.render(deltaTime, readBuffer);
		
		// Restore the old background color.
		if (mClearColor != 0x00000000) {
			GLES20.glClearColor(Color.red(mOldClearColor)/255f, Color.green(mOldClearColor)/255f, Color.blue(mOldClearColor)/255f, Color.alpha(mOldClearColor)/255f);
		}
	}
}
