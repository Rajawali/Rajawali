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

import rajawali.materials.Material;
import rajawali.materials.textures.ATexture.FilterType;
import rajawali.materials.textures.ATexture.WrapType;
import rajawali.postprocessing.IPass.PassType;
import rajawali.postprocessing.IPostProcessingComponent.PostProcessingComponentType;
import rajawali.postprocessing.passes.EffectPass;
import rajawali.primitives.ScreenQuad;
import rajawali.primitives.Sphere;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import rajawali.scenegraph.IGraphNode.GRAPH_TYPE;
import rajawali.util.RajLog;
import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.opengl.GLES20;
import android.view.Display;
import android.view.WindowManager;

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
	
	protected EffectPass mCopyPass;

	protected ScreenQuad mScreenQuad;
	protected RajawaliScene mScene;

	public PostProcessingManager(RajawaliRenderer renderer) {
		mScreenQuad = new ScreenQuad();
		mRenderer = renderer;
		mScene = new RajawaliScene(mRenderer, GRAPH_TYPE.NONE);

		int width, height;
		if (renderer.getSceneInitialized()) {
			width = mRenderer.getViewportWidth();
			height = mRenderer.getViewportHeight();
		} else {
			WindowManager wm = (WindowManager) renderer.getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			width = size.x;
			height = size.y;
		}

		mRenderTarget1 = new RenderTarget("renderTarget1", width, height, 0, 0, true,
				false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
				FilterType.LINEAR, WrapType.CLAMP);
		mRenderTarget2 = new RenderTarget("renderTarget2", width, height, 0, 0, true,
				false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
				FilterType.LINEAR, WrapType.CLAMP);
		
		mWriteBuffer = mRenderTarget1;
		mReadBuffer = mRenderTarget2;

		mCopyPass = new EffectPass(new CopyEffect());
		mComponents = Collections.synchronizedList(new CopyOnWriteArrayList<IPostProcessingComponent>());
		mPasses = Collections.synchronizedList(new CopyOnWriteArrayList<IPass>());

		mRenderer.addRenderTarget(mWriteBuffer);
		mRenderer.addRenderTarget(mReadBuffer);
		
		//mRenderer.getTextureManager().addTexture(mWriteBuffer.getTexture());
		//mRenderer.getTextureManager().addTexture(mWriteBuffer.getDepthTexture());
		//mRenderer.getTextureManager().addTexture(mReadBuffer.getTexture());
		//mRenderer.getTextureManager().addTexture(mReadBuffer.getDepthTexture());
		
		mScene.addChild(mScreenQuad);
		mRenderer.addScene(mScene);
		
		Sphere sphere = new Sphere(1, 20, 20);
		sphere.setColor(0xff0000);
		Material sphereMaterial = new Material();
		sphere.setMaterial(sphereMaterial);
		mScene.addChild(sphere);
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

	public void insertPass(int index, IPass pass) {
		mComponents.add(index, pass);
		mComponentsDirty = true;
	}

	public void removePass(IPass pass) {
		mComponents.remove(pass);
		mComponentsDirty = true;
	}
	
	public void addEffect(IPostProcessingEffect effect) {
		mComponents.add(effect);
		mComponentsDirty = true;
		effect.setManager(this);
	}
	
	public void insertEffect(IPostProcessingEffect effect) {
		mComponents.add(effect);
		mComponentsDirty = true;
		effect.setManager(this);
	}
	
	public void removeEffect(IPostProcessingEffect effect) {
		mComponents.remove(effect);
		mComponentsDirty = true;
		effect.setManager(null);
	}

	public void setSize(int width, int height) {
		RenderTarget renderTarget = mRenderTarget1.clone();
		renderTarget.setWidth(width);
		renderTarget.setHeight(height);
		reset(renderTarget);
	}

	public void reset(RenderTarget renderTarget) {
		if (renderTarget == null) {
			int width, height;
			if (mRenderer.getSceneInitialized()) {
				width = mRenderer.getViewportWidth();
				height = mRenderer.getViewportHeight();
			} else {
				WindowManager wm = (WindowManager) mRenderer.getContext()
						.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				width = size.x;
				height = size.y;
			}
			mRenderTarget1 = new RenderTarget(width, height, 0, 0, false,
					false, false, GLES20.GL_UNSIGNED_BYTE, Config.ARGB_8888,
					FilterType.LINEAR, WrapType.CLAMP);
		} else {
			mRenderTarget1 = renderTarget;
		}

		mRenderTarget2 = mRenderTarget1.clone();
		mWriteBuffer = mRenderTarget1;
		mReadBuffer = mRenderTarget2;
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
			RajLog.i("________________ PASS " + i + ", " + pass.getPassType());
			if (!pass.isEnabled())
				continue;

			PassType type = pass.getPassType();
			
			pass.render(type == PassType.RENDER ? mRenderer.getCurrentScene() : mScene, mRenderer,
					mScreenQuad, mWriteBuffer, mReadBuffer, deltaTime);

			if (pass.needsSwap()) {
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
			
			RajLog.i("________________ END PASS");
		}
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
