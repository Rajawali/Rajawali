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
package rajawali.postprocessing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.FilterType;
import rajawali.materials.textures.ATexture.WrapType;
import rajawali.postprocessing.IPass.PassType;
import rajawali.postprocessing.IPostProcessingComponent.PostProcessingComponentType;
import rajawali.postprocessing.passes.CopyPass;
import rajawali.postprocessing.passes.EffectPass;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import rajawali.scenegraph.IGraphNode.GRAPH_TYPE;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;

public class PostProcessingManager {

	protected RajawaliRenderer mRenderer;
	protected RenderTarget mRenderTarget1;
	protected RenderTarget mRenderTarget2;
	public RenderTarget mReadBuffer;
	public RenderTarget mWriteBuffer;

	protected List<IPostProcessingComponent> mComponents;
	protected List<IPass> mPasses;
	protected boolean mComponentsDirty = false;
	protected int mNumPasses;
	protected int mWidth;
	protected int mHeight;
	
	protected EffectPass mCopyPass;

	protected ScreenQuad mScreenQuad;
	protected RajawaliScene mScene;

	public PostProcessingManager(RajawaliRenderer renderer) {
		this(renderer, -1, -1);
	}
	
	public PostProcessingManager(RajawaliRenderer renderer, int width, int height) {
		mRenderer = renderer;

		if(width == -1 && height == -1) {
			if (mRenderer.getSceneInitialized()) {
				width = mRenderer.getCurrentViewportWidth();
				height = mRenderer.getCurrentViewportHeight();
			} else {
				width = mRenderer.getViewportWidth();
				height = mRenderer.getViewportHeight();
			}
		}		

		mWidth = width;
		mHeight = height;
		
		mScreenQuad = new ScreenQuad();
		mScene = new RajawaliScene(mRenderer, GRAPH_TYPE.NONE);

		mRenderTarget1 = new RenderTarget("rt1" + hashCode(), width, height, 0, 0,
				false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
				FilterType.LINEAR, WrapType.CLAMP);
		mRenderTarget2 = new RenderTarget("rt2" + hashCode(), width, height, 0, 0,
				false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
				FilterType.LINEAR, WrapType.CLAMP);
		
		mWriteBuffer = mRenderTarget1;
		mReadBuffer = mRenderTarget2;

		mCopyPass = new EffectPass(new CopyPass());
		mComponents = Collections.synchronizedList(new CopyOnWriteArrayList<IPostProcessingComponent>());
		mPasses = Collections.synchronizedList(new CopyOnWriteArrayList<IPass>());

		mRenderer.addRenderTarget(mWriteBuffer);
		mRenderer.addRenderTarget(mReadBuffer);
		
		mScene.addChild(mScreenQuad);
		mRenderer.addScene(mScene);
	}

	/**
	 * Swaps read and write buffers.
	 */
	public void swapBuffers() {
		RenderTarget tmp = mReadBuffer;
		mReadBuffer = mWriteBuffer;
		mWriteBuffer = tmp;
	}

	public void addPass(IPass pass) {
		mComponents.add(pass);
		mComponentsDirty = true;
	}
	
	public void addEffect(IPostProcessingEffect multiPass) {
		multiPass.initialize(mRenderer);
		mComponents.addAll(multiPass.getPasses());
		mComponentsDirty = true;
	}

	public void insertPass(int index, IPass pass) {
		mComponents.add(index, pass);
		mComponentsDirty = true;
	}
	
	public void insertEffect(int index, IPostProcessingEffect multiPass) {
		multiPass.initialize(mRenderer);
		mComponents.addAll(index, multiPass.getPasses());
		mComponentsDirty = true;
	}

	public void removePass(IPass pass) {
		mComponents.remove(pass);
		mComponentsDirty = true;
	}
	
	public void removeEffect(IPostProcessingEffect multiPass) {
		mComponents.removeAll(multiPass.getPasses());
		mComponentsDirty = true;
	}
	
	public void setSize(int width, int height) {
		mRenderTarget1.setWidth(width);
		mRenderTarget1.setHeight(height);
		mRenderTarget2.setWidth(width);
		mRenderTarget2.setHeight(height);
		
		for(IPass pass : mPasses) {
			pass.setSize(width, height);
		}
		
		//reset(renderTarget);
		mWidth = width;
		mHeight = height;
	}

	public void reset(RenderTarget renderTarget) {
	}

	public void render(double deltaTime) {
		if(mComponentsDirty == true)
		{
			updatePassesList();
			mComponentsDirty = false;
		}
		
		mWriteBuffer = mRenderTarget1;
		mReadBuffer = mRenderTarget2;
		
		boolean maskActive = false;

		IPass pass;

		for (int i = 0; i < mNumPasses; i++) {
			pass = mPasses.get(i);
			if (!pass.isEnabled())
				continue;

			PassType type = pass.getPassType();
			
			pass.render(type == PassType.RENDER || type == PassType.DEPTH ? mRenderer.getCurrentScene() : mScene, mRenderer,
					mScreenQuad, mWriteBuffer, mReadBuffer, deltaTime);

			if (pass.needsSwap() && i < mNumPasses - 1) {
				if (maskActive) {
					GLES20.glStencilFunc(GLES20.GL_NOTEQUAL, 1, 0xffffffff);

					mCopyPass.render(mScene, mRenderer, mScreenQuad, mWriteBuffer, mReadBuffer, deltaTime);

					GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 0xffffffff);
				}

				swapBuffers();
			}

			// If the current pass is a mask pass, notify the next pass that mask is active.
			if (type == PassType.MASK)
				maskActive = true;
			else if (type == PassType.CLEAR)
				maskActive = false;
		}
	}
	
	public ATexture getTexture() {
		return mWriteBuffer.getTexture();
	}
	
	private void updatePassesList()
	{
		mPasses.clear();
		
		for(int i=0; i<mComponents.size(); i++)
		{
			IPostProcessingComponent component = mComponents.get(i);
			if(component.getType() == PostProcessingComponentType.PASS)
			{
				mPasses.add((IPass)component);
			}
			else if(component.getType() == PostProcessingComponentType.EFFECT)
			{
				IPostProcessingEffect effect = (IPostProcessingEffect)component;
				mPasses.addAll(effect.getPasses());
			}
		}
		
		mNumPasses = mPasses.size();
	}

	public boolean isEmpty() {
		return mComponents.isEmpty();
	}

	public RajawaliScene getScene() {
		return mScene;
	}
	
	protected void setComponentsDirty()
	{
		mComponentsDirty = true;
	}
}
