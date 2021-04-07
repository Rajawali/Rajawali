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
import org.rajawali3d.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;

public class OcclusionMapFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "OCCLUSION_MAP_FRAGMENT";
	private ArrayList videoTextureMap = new ArrayList();
	private ArrayList textureMap = new ArrayList();

	public OcclusionMapFragmentShaderFragment(List<ATexture> textures)
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
		RVec3 ambientColor = (RVec3) getGlobal(LightsShaderVar.G_AMBIENT_COLOR);
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
		RVec4 occlusion = new RVec4("ao");
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			if(texture.transformEnabled()) {
				RMat3 transform = (RMat3) getGlobal(DefaultShaderVar.U_TRANSFORM, i);
				textureCoord.assign(transform.multiply(textureCoord));
			}
			
			if(texture.getTextureType() == TextureType.VIDEO_TEXTURE)
				occlusion.assign(texture2D(muVideoTextures[videoTextureMap.indexOf(i)], textureCoord));
			else
				occlusion.assign(texture2D(muTextures[textureMap.indexOf(i)], textureCoord));
			occlusion.assignMultiply(muInfluences[i]);
			ambientColor.assignMultiply(occlusion.rgb());
		}
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
	
	public void bindTextures(int nextIndex) {}
	public void unbindTextures() {}
}
