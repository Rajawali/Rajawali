package org.rajawali3d.postprocessing.effects;

import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import org.rajawali3d.cameras.Camera;
import c.org.rajawali3d.sceneview.lights.DirectionalLight;
import org.rajawali3d.postprocessing.APostProcessingEffect;
import org.rajawali3d.postprocessing.materials.ShadowMapMaterial;
import org.rajawali3d.postprocessing.passes.ShadowPass;
import org.rajawali3d.postprocessing.passes.ShadowPass.ShadowPassType;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.Wrap;


public class ShadowEffect extends APostProcessingEffect {
	private Scene             mScene;
	private Camera            mCamera;
	private DirectionalLight  mLight;
	private int               mShadowMapSize;
	private RenderTarget      mShadowRenderTarget;
	private float             mShadowInfluence;
	private ShadowMapMaterial mShadowMapMaterial;

	public ShadowEffect(Scene scene, Camera camera, DirectionalLight light, int shadowMapSize) {
		super();
		mScene = scene;
		mCamera = camera;
		mLight = light;
		mShadowMapSize = shadowMapSize;
	}

	public void setShadowInfluence(float influence) {
		mShadowInfluence = influence;
		if(mShadowMapMaterial != null)
			mShadowMapMaterial.setShadowInfluence(influence);
	}

	@Override
	public void initialize(Renderer renderer) {
		mShadowRenderTarget = new RenderTarget("shadowRT" + hashCode(), mShadowMapSize, mShadowMapSize, 0, 0,
											   false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
											   Filter.BILINEAR, (Wrap.CLAMP_S | Wrap.CLAMP_T | Wrap.CLAMP_R));
		renderer.addRenderTarget(mShadowRenderTarget);

		ShadowPass pass1 = new ShadowPass(ShadowPassType.CREATE_SHADOW_MAP, mScene, mCamera, mLight, mShadowRenderTarget);
		addPass(pass1);
		ShadowPass pass2 = new ShadowPass(ShadowPassType.APPLY_SHADOW_MAP, mScene, mCamera, mLight, mShadowRenderTarget);
		mShadowMapMaterial = pass1.getShadowMapMaterial();
		mShadowMapMaterial.setShadowInfluence(mShadowInfluence);
		pass2.setShadowMapMaterial(pass1.getShadowMapMaterial());
		addPass(pass2);
	}
}
