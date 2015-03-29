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
package org.rajawali3d.materials.methods;

import java.util.List;

import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.IShaderFragment;


public interface IDiffuseMethod {
	/**
	 * Returns the vertex shader fragment. This should only be used by the {@link Material} class.
	 * @return
	 */
	IShaderFragment getVertexShaderFragment();
	/**
	 * Returns the fragmetn shader fragment. This should only be used by the {@link Material} class.
	 * @return
	 */
	IShaderFragment getFragmentShaderFragment();
	void setLights(List<ALight> lights);
}
