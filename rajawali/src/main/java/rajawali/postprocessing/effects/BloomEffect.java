/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */package rajawali.postprocessing.effects;

import rajawali.Camera;
import rajawali.postprocessing.APostProcessingEffect;
import rajawali.postprocessing.passes.BlendPass;
import rajawali.postprocessing.passes.BlendPass.BlendMode;
import rajawali.postprocessing.passes.BlurPass.Direction;
import rajawali.postprocessing.passes.BlurPass;
import rajawali.postprocessing.passes.ColorThresholdPass;
import rajawali.postprocessing.passes.CopyToNewRenderTargetPass;
import rajawali.postprocessing.passes.RenderPass;
import rajawali.renderer.RajawaliRenderer;
import rajawali.scene.RajawaliScene;

public class BloomEffect extends APostProcessingEffect {
	private RajawaliScene mScene;
	private Camera mCamera;
	private int mWidth;
	private int mHeight;
	private int mLowerThreshold;
	private int mUpperThreshold;
	private BlendMode mBlendMode;
	
	/**
	 * Bloom or glow is used to amplify light in a scene. It produces light bleeding. Bright light will extend
	 * to other parts of the scene. The colors that will bleed can be controlled by specifying the lower
	 * and upper threshold colors.
	 * 
	 * @param scene
	 * @param camera
	 * @param width
	 * @param height
	 * @param lowerThreshold
	 * @param upperThreshold
	 * @param blendMode
	 */
	public BloomEffect(RajawaliScene scene, Camera camera, int width, int height, int lowerThreshold, int upperThreshold, BlendMode blendMode) {
		super();
		mScene = scene;
		mCamera = camera;
		mWidth = width;
		mHeight = height;
		mLowerThreshold = lowerThreshold;
		mUpperThreshold = upperThreshold;
		mBlendMode = blendMode;
	}
	
	public void initialize(RajawaliRenderer renderer)
	{
		addPass(new ColorThresholdPass(mLowerThreshold, mUpperThreshold));
		addPass(new BlurPass(Direction.HORIZONTAL, 6, mWidth, mHeight));
		addPass(new BlurPass(Direction.VERTICAL, 6, mWidth, mHeight));
		CopyToNewRenderTargetPass copyPass = new CopyToNewRenderTargetPass("bloomPassTarget", renderer, mWidth, mHeight);
		addPass(copyPass);
		addPass(new RenderPass(mScene, mCamera, mScene.getBackgroundColor()));
		addPass(new BlendPass(mBlendMode, copyPass.getRenderTarget().getTexture()));
	}
}
