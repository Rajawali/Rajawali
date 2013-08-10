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
package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

public class SimpleMaterial extends AMaterial {

	public SimpleMaterial() {
		super(R.raw.simple_material_vertex, R.raw.simple_material_fragment);
	}
	
	public SimpleMaterial(int vertex_resID, int fragment_resID) {
		super(vertex_resID, fragment_resID);
	}

	public SimpleMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}

	public void setShaders(String vertexShader, String fragmentShader) {
		super.setShaders(vertexShader, fragmentShader);
	}
}
