package rajawali.effects;

import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;

/**
 * Defines a rendering pass which is needed for multiple rendering passes.
 * @author Andrew Jo
 */
public abstract class APass {
	protected boolean mEnabled;
	protected boolean mClear;
	protected boolean mNeedsSwap;
	
	/**
	 * Returns whether this pass is to be rendered. If false, renderer skips this pass.
	 */
	public boolean isEnabled() {
		return mEnabled;
	}
	
	/**
	 * Returns whether the framebuffer should be cleared before rendering this pass.
	 */
	public boolean isClear() {
		return mClear;
	}
	
	/**
	 * Returns whether the write buffer and the read buffer needs to be swapped afterwards.
	 */
	public boolean needsSwap() {
		return mNeedsSwap;
	}
	
	public void render(RajawaliRenderer renderer, RenderTarget writeTarget, RenderTarget readTarget, double deltaTime) {}
}
