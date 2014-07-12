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

import rajawali.framework.R;
import rajawali.materials.textures.ATexture.FilterType;
import rajawali.materials.textures.ATexture.WrapType;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;

public class CopyToNewRenderTargetPass extends EffectPass {
	private RenderTarget mRenderTarget;
	
	public CopyToNewRenderTargetPass(String name, RajawaliRenderer renderer, int width, int height) {
		super();
		mNeedsSwap = false;
		mRenderTarget = new RenderTarget(name, width, height, 0, 0,
				false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
				FilterType.LINEAR, WrapType.CLAMP);
		renderer.addRenderTarget(mRenderTarget);
		
		createMaterial(R.raw.minimal_vertex_shader, R.raw.copy_fragment_shader);
	}
	
	public RenderTarget getRenderTarget() {
		return mRenderTarget;
	}
	
	public void render(RajawaliScene scene, RajawaliRenderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget, RenderTarget readTarget, double deltaTime) {
		super.render(scene, renderer, screenQuad, mRenderTarget, readTarget, deltaTime);
	}
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		mRenderTarget.setWidth(width);
		mRenderTarget.setHeight(height);
	}
}
