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
package rajawali.effects;

import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;

/**
 * Defines a rendering pass which is needed for multiple rendering passes.
 * @author Andrew Jo
 */
public abstract class APass {
	protected boolean mEnabled;
	protected boolean mClear;
	protected boolean mNeedsSwap;
	
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
	
	public void render(RajawaliRenderer renderer, RenderTarget writeTarget, RenderTarget readTarget, double deltaTime) {}
}
