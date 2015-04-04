package org.rajawali3d.postprocessing.passes;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.postprocessing.materials.ShadowMapMaterial;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.scene.RajawaliScene;


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
	public void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad, RenderTarget writeBuffer, RenderTarget readBuffer, long ellapsedTime, double deltaTime) {
		if(mShadowPassType == ShadowPassType.APPLY_SHADOW_MAP) {
			mShadowMapMaterial.setShadowMapTexture(mShadowRenderTarget.getTexture());
			super.render(scene, renderer, screenQuad, writeBuffer, readBuffer, ellapsedTime, deltaTime);
		} else {
            renderer.setOverrideViewportDimensions(mShadowMapSize, mShadowMapSize);
			super.render(scene, renderer, screenQuad, mShadowRenderTarget, readBuffer, ellapsedTime, deltaTime);
            renderer.clearOverrideViewportDimensions();
		}
	}
	
	public ShadowMapMaterial getShadowMapMaterial() {
		return mShadowMapMaterial;
	}
	
	public void setShadowMapMaterial(ShadowMapMaterial shadowMapMaterial) {
		mShadowMapMaterial = shadowMapMaterial;
	}
}
