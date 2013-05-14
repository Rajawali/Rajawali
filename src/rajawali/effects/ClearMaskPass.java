package rajawali.effects;

import android.opengl.GLES20;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;

/**
 * Disables stencil test for previously masked rendering passes so that
 * next render passes are not masked.
 * @author andrewjo
 */
public class ClearMaskPass extends APass {
	public ClearMaskPass() {
		mEnabled = true;
	}
	
	@Override
	public void render(RajawaliRenderer renderer, RenderTarget writeBuffer, RenderTarget readBuffer, double deltaTime) {
		// Disable stencil test so next rendering pass won't be masked.
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
	}
}
