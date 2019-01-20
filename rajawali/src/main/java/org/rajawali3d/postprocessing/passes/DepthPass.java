package org.rajawali3d.postprocessing.passes;

import android.opengl.GLES20;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.DepthMaterialPlugin;
import org.rajawali3d.postprocessing.APass;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.scene.Scene;


public class DepthPass extends APass {
	protected Scene               mScene;
	protected Camera              mCamera;
	protected Camera              mOldCamera;
	protected DepthMaterialPlugin mDepthPlugin;

	public DepthPass(Scene scene, Camera camera) {
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
	public void render(Scene scene, Renderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget,
					   RenderTarget readTarget, long elapsedTime, double deltaTime) {
		GLES20.glClearColor(0, 0, 0, 1);
		mDepthPlugin.setFarPlane((float)mCamera.getFarPlane());
		mOldCamera = mScene.getCamera();
		mScene.switchCamera(mCamera);
		mScene.render(elapsedTime, deltaTime, writeTarget, mMaterial);
		mScene.switchCamera(mOldCamera);
	}
}
