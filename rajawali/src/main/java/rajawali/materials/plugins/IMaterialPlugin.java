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
package rajawali.materials.plugins;

import rajawali.materials.Material;
import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.IShaderFragment;


/**
 * A material plugin is a container for a vertex and fragment shader. It can be 
 * plugged into the {@link Material} class so it can be assembled into the main
 * shader. They can be used for skeletal animation, custom vertex animation effects,
 * fragment shader effects, etc.
 * 
 * Usage example:
 * <pre><code>
 * material.addPlugin(new MyMaterialPlugin());
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public interface IMaterialPlugin {
	/**
	 * Where to insert the plugin. The {@link Material} class needs to know at 
	 * what location the shader fragments should be inserted when assembling the
	 * shaders. The locations are defined in the {@link PluginInsertLocation}
	 * enum. 
	 * @return
	 */
	PluginInsertLocation getInsertLocation();
	/**
	 * Returns the vertex shader fragment. This should only be used by the {@link Material} class.
	 * @return
	 */
	IShaderFragment getVertexShaderFragment();
	/**
	 * Returns the fragment shader fragment. This should only be used by the {@link Material} class.
	 * @return
	 */
	IShaderFragment getFragmentShaderFragment();
	void bindTextures(int nextIndex);
	void unbindTextures();
}
