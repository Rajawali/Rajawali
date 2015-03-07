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
package rajawali.materials.shaders.fragments.diffuse;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class LambertVertexShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LAMBERT_VERTEX";

	public LambertVertexShaderFragment() {
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
}
