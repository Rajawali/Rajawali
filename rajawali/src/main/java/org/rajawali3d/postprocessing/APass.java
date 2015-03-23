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
 */
package org.rajawali3d.postprocessing;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.MaterialManager;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.scene.RajawaliScene;

/**
 * Defines a rendering pass which is needed for multiple rendering passes.
 * 
 * @author Andrew Jo
 * @author dennis.ippel
 */
public abstract class APass implements IPass {

	protected boolean mEnabled;
	protected boolean mClear;
	protected boolean mNeedsSwap;
	protected PassType mPassType;
	protected Material mMaterial;
	protected boolean mRenderToScreen;
	protected int mWidth;
	protected int mHeight;	

	/**
	 * Returns whether this pass is to be rendered. If false, renderer skips this pass.
	 */
	public boolean isEnabled() {
		return mEnabled;
	}

	/**
	 * Returns whether the framebuffer should be cleared before rendering this pass.
	 */
	public boolean isClear() {
		return mClear;
	}

	/**
	 * Returns whether the write buffer and the read buffer needs to be swapped afterwards.
	 */
	public boolean needsSwap() {
		return mNeedsSwap;
	}
	
	public abstract void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad,
			RenderTarget writeTarget, RenderTarget readTarget, long ellapsedTime, double deltaTime);

	public PassType getPassType() {
		return mPassType;
	}

	public PostProcessingComponentType getType() {
		return PostProcessingComponentType.PASS;
	}

	public void setMaterial(Material material) {
		mMaterial = material;
		MaterialManager.getInstance().addMaterial(material);
	}

	public void setRenderToScreen(boolean renderToScreen) {
		mRenderToScreen = renderToScreen;
	}
	
	public void setWidth(int width) {
		mWidth = width;
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public void setHeight(int height) {
		mHeight = height;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public void setSize(int width, int height) {
		mWidth = width;
		mHeight = height;
	}
}
