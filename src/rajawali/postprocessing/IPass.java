package rajawali.postprocessing;

import rajawali.materials.Material;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;


public interface IPass extends IPostProcessingComponent {
	public static enum PassType {
		RENDER, EFFECT, MASK, CLEAR
	};
	
	boolean isClear();
	boolean needsSwap();
	void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget, RenderTarget readTarget, double deltaTime);
	PassType getPassType();
	IPostProcessingEffect getParent();
	void setMaterial(Material material);
}
