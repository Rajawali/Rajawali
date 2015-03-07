package rajawali.postprocessing.passes;

import android.opengl.GLES20;
import rajawali.Camera;
import rajawali.materials.Material;
import rajawali.materials.plugins.DepthMaterialPlugin;
import rajawali.postprocessing.APass;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;


public class DepthPass extends APass {
	protected RajawaliScene mScene;
	protected Camera mCamera;
	protected Camera mOldCamera;
	protected DepthMaterialPlugin mDepthPlugin;

	public DepthPass(RajawaliScene scene, Camera camera) {
		mPassType = PassType.DEPTH;
		mScene = scene;
		mCamera = camera;
		
		mEnabled = true;
		mClear = true;
		mNeedsSwap = true;
		
		Material mat = new Material();
		mDepthPlugin = new DepthMaterialPlugin();
		mat.addPlugin(mDepthPlugin);
		setMaterial(mat);
	}
	
	@Override
	public void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget,
			RenderTarget readTarget, double deltaTime) {
		GLES20.glClearColor(0, 0, 0, 1);
		mDepthPlugin.setFarPlane((float)mCamera.getFarPlane());
		mOldCamera = mScene.getCamera();
		mScene.switchCamera(mCamera);
		mScene.render(deltaTime, writeTarget, mMaterial);
		mScene.switchCamera(mOldCamera);
	}
}
