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
package rajawali.postprocessing.passes;

import rajawali.materials.Material;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.VertexShader;
import rajawali.postprocessing.APass;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;


public class EffectPass extends APass {
	protected final String PARAM_OPACITY = "uOpacity";
	protected final String PARAM_TEXTURE = "uTexture";
	protected final String PARAM_DEPTH_TEXTURE = "uDepthTexture";
	protected boolean mRenderToScreen;
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
		setMaterial(new Material(mVertexShader, mFragmentShader));
	}
	
	public void setRenderToScreen(boolean renderToScreen)
	{
		mRenderToScreen = renderToScreen;
	}
	
	public void setShaderParams()
	{
		mFragmentShader.setUniform1f(PARAM_OPACITY, mOpacity);
		mMaterial.bindTextureByName(PARAM_TEXTURE, 0, mReadTarget.getTexture());
	}
	
	public void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget, RenderTarget readTarget, double deltaTime) {
		mReadTarget = readTarget;
		mWriteTarget = writeTarget;
		screenQuad.setMaterial(mMaterial);
		screenQuad.setEffectPass(this);
		
		if(mRenderToScreen == true)
			scene.render(deltaTime, null);
		else
			scene.render(deltaTime, writeTarget);
	}
	
	public void setOpacity(float value)
	{
		mOpacity = value;
	}
}
