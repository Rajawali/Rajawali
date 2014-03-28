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

import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureType;


public class SkyTextureFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "SKY_TEXTURE_FRAGMENT";
	
	public SkyTextureFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
	}
	
	@Override
	public void main() {
		super.main();
		
		RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
		RVec4 skyColor = new RVec4("skyColor");
		RVec3 texCoord = (RVec3) getGlobal(DefaultShaderVar.V_CUBE_TEXTURE_COORD);
		
		int cubeMapCount = 0;
		
		for(int i=0; i<mTextures.size(); i++)
		{
			if(mTextures.get(i).getTextureType() == TextureType.CUBE_MAP)
			{
				skyColor.assign(textureCube(muCubeTextures[cubeMapCount++], texCoord));
			}
			
			skyColor.assignMultiply(muInfluence[i]);
			color.assignAdd(skyColor);
		}
	}
}
