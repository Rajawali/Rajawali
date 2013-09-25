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
package rajawali.renderer;

import rajawali.materials.textures.ATexture.FilterType;
import rajawali.materials.textures.ATexture.WrapType;
import rajawali.materials.textures.TextureManager;
import android.graphics.Bitmap.Config;

/**
 * Defines a render target to be mapped to a cubemap texture. 
 * @author Andrew Jo (andrewjo@gmail.com)
 */
public class RenderTargetCube extends RenderTarget {
	// Refer to TextureManager.CUBE_FACES.
	protected int mActiveCubeFace;
	
	public RenderTargetCube(int width, int height, int offsetX, int offsetY, boolean depthBuffer,
			boolean stencilBuffer, boolean mipmaps, int glType, Config bitmapConfig, FilterType filterType,
			WrapType wrapType) {
		super(width, height, offsetX, offsetY, depthBuffer, stencilBuffer, mipmaps, glType, bitmapConfig, filterType, wrapType);
	}

	public RenderTargetCube(int width, int height) {
		super(width, height);
	}
	
	/**
	 * Returns the current active cubeface.
	 * @return Returns an integer value that maps to one of the constant values at {@link TextureManager.CUBE_FACES }.
	 */
	public int getActiveCubeFace() {
		return mActiveCubeFace;
	}
	
	/**
	 * Sets the current active cubeface.
	 * @param activeCubeFace Refer to {@link TextureManager.CUBE_FACES } for constant values.
	 */
	public void setActiveCubeFace(int activeCubeFace) {
		this.mActiveCubeFace = activeCubeFace;
	}
}
