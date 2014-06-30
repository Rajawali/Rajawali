package rajawali.postprocessing.passes;

import rajawali.Camera;
import rajawali.lights.DirectionalLight;
import rajawali.postprocessing.materials.ShadowMapMaterial;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;


public class ShadowPass extends RenderPass {
	private RenderTarget mShadowRenderTarget;
	private int mShadowMapSize;
	
	public static enum ShadowPassType {
		CREATE_SHADOW_MAP, APPLY_SHADOW_MAP
	}
	
	private ShadowMapMaterial mShadowMapMaterial;
	private ShadowPassType mShadowPassType;

	public ShadowPass(ShadowPassType shadowPassType, RajawaliScene scene, Camera camera, DirectionalLight light, RenderTarget renderTarget) {
		super(scene, camera, 0);
		mShadowPassType = shadowPassType;
		mShadowRenderTarget = renderTarget;
		mShadowMapSize = renderTarget.getWidth();
		if(shadowPassType == ShadowPassType.CREATE_SHADOW_MAP) {
			mShadowMapMaterial = new ShadowMapMaterial();
			mShadowMapMaterial.setLight(light);
			mShadowMapMaterial.setCamera(camera);
			mShadowMapMaterial.setScene(scene);
			setMaterial(mShadowMapMaterial);
		}
	}
	
	@Override
	public void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad, RenderTarget writeBuffer, RenderTarget readBuffer, double deltaTime) {
		if(mShadowPassType == ShadowPassType.APPLY_SHADOW_MAP) {
			mShadowMapMaterial.setShadowMapTexture(mShadowRenderTarget.getTexture());
			super.render(scene, renderer, screenQuad, writeBuffer, readBuffer, deltaTime);
		} else {
			int oldWidth = renderer.getCurrentViewportWidth();
			int oldHeight = renderer.getCurrentViewportHeight();
			renderer.setViewPort(mShadowMapSize, mShadowMapSize);
			super.render(scene, renderer, screenQuad, mShadowRenderTarget, readBuffer, deltaTime);
			renderer.setViewPort(oldWidth, oldHeight);
		}
	}
	
	public ShadowMapMaterial getShadowMapMaterial() {
		return mShadowMapMaterial;
	}
	
	public void setShadowMapMaterial(ShadowMapMaterial shadowMapMaterial) {
		mShadowMapMaterial = shadowMapMaterial;
	}
}
