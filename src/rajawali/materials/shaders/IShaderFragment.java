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
package rajawali.materials.shaders;

import java.util.Hashtable;

import rajawali.materials.shaders.AShaderBase.ShaderVar;



public interface IShaderFragment {
	String getShaderId();
	Hashtable<String, ShaderVar> getUniforms();
	Hashtable<String, ShaderVar> getAttributes();
	Hashtable<String, ShaderVar> getVaryings();
	Hashtable<String, ShaderVar> getGlobals();
	Hashtable<String, ShaderVar> getConsts();
	void setStringBuilder(StringBuilder stringBuilder);
	void main();
	void applyParams();
	void setLocations(int programHandle);
}
