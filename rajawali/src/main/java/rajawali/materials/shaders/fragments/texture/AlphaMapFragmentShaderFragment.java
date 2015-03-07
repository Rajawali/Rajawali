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
package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.textures.ATexture;


public class AlphaMapFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "ALPHA_MAP_FRAGMENT";

	public AlphaMapFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		super.main();
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
		RVec4 alphaMaskColor = new RVec4("alphaMaskColor");
		
		for(int i=0; i<mTextures.size(); i++)
		{
			alphaMaskColor.assign(texture2D(muTextures[i], textureCoord));
			startif(new Condition(alphaMaskColor.r(), Operator.LESS_THAN, .5f));
			{
				discard();
			}
			endif();
		}
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
	
	public void bindTextures(int nextIndex) {}
	public void unbindTextures() {}
}
