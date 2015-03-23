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
package org.rajawali3d.materials.shaders.fragments.texture;

import java.util.List;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.textures.ATexture;


public class NormalMapFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "NORMAL_MAP_FRAGMENT";

	public NormalMapFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
		RVec3 texNormal = new RVec3("texNormal");
		RVec3 normal = (RVec3)getGlobal(DefaultShaderVar.G_NORMAL);
		
		for(int i=0; i<mTextures.size(); i++)
		{
			texNormal.assign(castVec3(texture2D(muTextures[i], textureCoord)));
			texNormal.assign(texNormal.rgb().multiply(2));
			texNormal.assignSubtract(1);
			texNormal.assign(normalize(texNormal));
			if(mTextures.get(i).getInfluence() != 1)
				texNormal.assignMultiply(mTextures.get(i).getInfluence());
			
			normal.assign(normalize(texNormal.add(normal)));
		}
	}
	
	public void bindTextures(int nextIndex) {}
	public void unbindTextures() {}

	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
}
