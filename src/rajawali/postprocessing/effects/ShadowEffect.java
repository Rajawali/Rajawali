package rajawali.postprocessing.effects;

import rajawali.Camera;
import rajawali.lights.DirectionalLight;
import rajawali.materials.textures.ATexture.FilterType;
import rajawali.materials.textures.ATexture.WrapType;
import rajawali.postprocessing.APostProcessingEffect;
import rajawali.postprocessing.passes.ShadowPass;
import rajawali.postprocessing.passes.ShadowPass.ShadowPassType;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;


public class ShadowEffect extends APostProcessingEffect {
	private RajawaliScene mScene;
	private Camera mCamera;
	private DirectionalLight mLight;
	private int mShadowMapSize;
	private RenderTarget mShadowRenderTarget;
	
	public ShadowEffect(RajawaliScene scene, Camera camera, DirectionalLight light, int shadowMapSize) {
		super();
		mScene = scene;
		mCamera = camera;
		mLight = light;
		mShadowMapSize = shadowMapSize;
	}

	@Override
	public void initialize(RajawaliRenderer renderer) {
		mShadowRenderTarget = new RenderTarget("shadowRT" + hashCode(), mShadowMapSize, mShadowMapSize, 0, 0,
				false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
				FilterType.LINEAR, WrapType.CLAMP);
		renderer.addRenderTarget(mShadowRenderTarget);
		
		ShadowPass pass1 = new ShadowPass(ShadowPassType.CREATE_SHADOW_MAP, mScene, mCamera, mLight, mShadowRenderTarget);
		addPass(pass1);
		ShadowPass pass2 = new ShadowPass(ShadowPassType.APPLY_SHADOW_MAP, mScene, mCamera, mLight, mShadowRenderTarget);
		pass2.setShadowMapMaterial(pass1.getShadowMapMaterial());
		addPass(pass2);
	}
}
