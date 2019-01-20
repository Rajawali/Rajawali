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
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.scene.Scene;


public interface IPass extends IPostProcessingComponent {
	enum PassType {
		RENDER, DEPTH, EFFECT, MASK, CLEAR, MULTIPASS
	}

    boolean isClear();
	boolean needsSwap();
	void render(Scene scene, Renderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget, RenderTarget readTarget, long elapsedTime, double deltaTime);
	PassType getPassType();
	void setMaterial(Material material);
	void setRenderToScreen(boolean renderToScreen);
	boolean getRenderToScreen();
	void setWidth(int width);
	int getWidth();
	void setHeight(int height);
	int getHeight();
	void setSize(int width, int height);
}
