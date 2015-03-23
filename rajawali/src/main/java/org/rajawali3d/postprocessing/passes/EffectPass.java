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
package org.rajawali3d.postprocessing.passes;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.postprocessing.APass;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.scene.RajawaliScene;


public class EffectPass extends APass {
	protected final String PARAM_OPACITY = "uOpacity";
	protected final String PARAM_TEXTURE = "uTexture";
	protected final String PARAM_DEPTH_TEXTURE = "uDepthTexture";
	protected final String PARAM_BLEND_TEXTURE = "uBlendTexture";
	
	protected VertexShader mVertexShader;
	protected FragmentShader mFragmentShader;
	protected RenderTarget mReadTarget;
	protected RenderTarget mWriteTarget;
	protected float mOpacity = 1.0f;
	
	public EffectPass()
	{
		mPassType = PassType.EFFECT;
		mNeedsSwap = true;
		mClear = false;
		mEnabled = true;
		mRenderToScreen = false;
	}
	
	public EffectPass(Material material) {
		this();
		setMaterial(material);
	}
	
	protected void createMaterial(int vertexShaderResourceId, int fragmentShaderResourceId)
	{
		mVertexShader = new VertexShader(vertexShaderResourceId);
		mFragmentShader = new FragmentShader(fragmentShaderResourceId);
		mVertexShader.setNeedsBuild(false);
		mFragmentShader.setNeedsBuild(false);
		setMaterial(new Material(mVertexShader, mFragmentShader));
	}
	
	
	public void setShaderParams()
	{
		mFragmentShader.setUniform1f(PARAM_OPACITY, mOpacity);
		mMaterial.bindTextureByName(PARAM_TEXTURE, 0, mReadTarget.getTexture());
	}
	
	public void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget, RenderTarget readTarget, long ellapsedTime, double deltaTime) {
		mReadTarget = readTarget;
		mWriteTarget = writeTarget;
		screenQuad.setMaterial(mMaterial);
		screenQuad.setEffectPass(this);
		
		if(mRenderToScreen)
			scene.render(ellapsedTime, deltaTime, null);
		else
			scene.render(ellapsedTime, deltaTime, writeTarget);
	}
	
	public void setOpacity(float value)
	{
		mOpacity = value;
	}
}
