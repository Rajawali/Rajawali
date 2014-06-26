package rajawali.postprocessing.passes;

import rajawali.Camera;
import rajawali.lights.DirectionalLight;
import rajawali.postprocessing.materials.ShadowMapMaterial;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import rajawali.util.RajLog;


public class ShadowPass extends RenderPass {
	public static enum ShadowPassType {
		CREATE_SHADOW_MAP, APPLY_SHADOW_MAP
	}
	
	private ShadowMapMaterial mShadowMapMaterial;
	private ShadowPassType mShadowPassType;

	public ShadowPass(ShadowPassType shadowPassType, RajawaliScene scene, Camera camera, DirectionalLight light) {
		super(scene, camera, 0);
		mShadowPassType = shadowPassType;
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
		if(mShadowPassType == ShadowPassType.APPLY_SHADOW_MAP)
			mShadowMapMaterial.setShadowMapTexture(readBuffer.getTexture());
		
		super.render(scene, renderer, screenQuad, writeBuffer, readBuffer, deltaTime);
	}
	
	public ShadowMapMaterial getShadowMapMaterial() {
		return mShadowMapMaterial;
	}
	
	public void setShadowMapMaterial(ShadowMapMaterial shadowMapMaterial) {
		mShadowMapMaterial = shadowMapMaterial;
	}
}
