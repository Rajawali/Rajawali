/**
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
import java.util.ArrayList;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.ATexture.TextureType;
import org.rajawali3d.materials.textures.ATexture.WrapType;

public class LightMapFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "LIGHT_MAP_FRAGMENT";
	private ArrayList videoTextureMap = new ArrayList();
	private ArrayList textureMap = new ArrayList();

	public LightMapFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			if(texture.getTextureType() == TextureType.VIDEO_TEXTURE)
				videoTextureMap.add(i);
			else
				textureMap.add(i);
		}
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		super.main();
		RVec4 color = (RVec4)getGlobal(DefaultShaderVar.G_COLOR);
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
		RVec4 glowColor = new RVec4("glowColor");
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			if(texture.offsetEnabled())
				textureCoord.assignAdd(getGlobal(DefaultShaderVar.U_OFFSET, i));
			if(texture.getWrapType() == WrapType.REPEAT)
				textureCoord.assignMultiply(getGlobal(DefaultShaderVar.U_REPEAT, i));
			
			if(texture.getTextureType() == TextureType.VIDEO_TEXTURE)
				glowColor.assign(texture2D(muVideoTextures[videoTextureMap.indexOf(i)], textureCoord));
			else
				glowColor.assign(texture2D(muTextures[textureMap.indexOf(i)], textureCoord));
			glowColor.assignMultiply(muInfluence[i]);
			color.rgb().assignAdd(glowColor.rgb());
		}
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
	
	public void bindTextures(int nextIndex) {}
	public void unbindTextures() {}
}
