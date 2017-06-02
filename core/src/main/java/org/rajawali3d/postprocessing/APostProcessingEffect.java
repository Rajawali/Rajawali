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

import java.util.ArrayList;
import java.util.List;

import org.rajawali3d.renderer.Renderer;


public abstract class APostProcessingEffect implements IPostProcessingEffect {
	protected List<IPass> mPasses;
	protected boolean     mEnabled;
	protected Renderer    mRenderer;

	@Override
	public void removePass(IPass pass) {
		mPasses.remove(pass);
	}

	@Override
	public void removeAllPasses() {
		mPasses.clear();
	}

	@Override
	public IPass addPass(IPass pass) {
		if(mPasses == null) mPasses = new ArrayList<IPass>();
		mPasses.add(pass);
		return pass;
	}

	@Override
	public List<IPass> getPasses() {
		return mPasses;
	}

	/**
	 * Returns whether this pass is to be rendered. If false, renderer skips this pass.
	 */
	public boolean isEnabled() {
		return mEnabled;
	}

	@Override
	public PostProcessingComponentType getType() {
		return PostProcessingComponentType.MULTIPASS;
	}

	@Override
	public void setRenderToScreen(boolean renderToScreen) {
		for(IPass pass : mPasses) {
			pass.setRenderToScreen(false);
		}
		if(renderToScreen)
		{
			mPasses.get(mPasses.size() - 1).setRenderToScreen(true);
		}
	}
}
