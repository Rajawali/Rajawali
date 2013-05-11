package rajawali.effects;

/**
 * Code heavily referenced from Three.js post processing framework.
 */
import android.opengl.GLES20;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;

/**
 * Masked render pass for drawing to stencil buffer.
 * @author Andrew Jo (andrewjo@gmail.com / www.andrewjo.com)
 */
public class MaskPass extends APass {
	protected RajawaliScene mScene;
	protected boolean mInverse;
	
	public MaskPass(RajawaliScene scene) {
		mScene = scene;
		mEnabled = true;
		mClear = true;
		mNeedsSwap = false;
		mInverse = false;
	}
	
	/**
	 * Returns whether the stencil is inverted.
	 * @return True if inverted, false otherwise.
	 */
	public boolean isInverse() {
		return mInverse;
	}
	
	/**
	 * Sets whether to invert the stencil buffer.
	 * @param inverse True to invert, false otherwise.
	 */
	public void setInverse(boolean inverse) {
		mInverse = inverse;
	}
	
	@Override
	public void render(RajawaliRenderer render, RenderTarget writeBuffer, RenderTarget readBuffer, double deltaTime) {
		// Do not update color or depth.
		GLES20.glColorMask(false, false, false, false);
		GLES20.glDepthMask(false);
		
		// Set up stencil.
		int writeValue, clearValue;
		
		if (mInverse) {
			writeValue = 0;
			clearValue = 1;
		} else {
			writeValue = 1;
			clearValue = 0;
		}
		
		GLES20.glEnable(GLES20.GL_STENCIL_TEST);
		GLES20.glStencilOp(GLES20.GL_REPLACE, GLES20.GL_REPLACE, GLES20.GL_REPLACE);
		GLES20.glStencilFunc(GLES20.GL_ALWAYS, writeValue, 0xffffffff);
		GLES20.glClearStencil(clearValue);
		
		// Draw into the stencil buffer.
		mScene.render(deltaTime, readBuffer);
		mScene.render(deltaTime, writeBuffer);
		
		// Re-enable color and depth.
		GLES20.glColorMask(true, true, true, true);
		GLES20.glDepthMask(true);
		
		// Only render where stencil is set to 1.
		GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 0xffffffff);
		GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);
	}
}
