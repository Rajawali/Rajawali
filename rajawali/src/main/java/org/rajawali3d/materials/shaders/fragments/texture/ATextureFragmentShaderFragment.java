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

import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.ATexture.TextureType;
import org.rajawali3d.materials.textures.ATexture.WrapType;
import android.opengl.GLES20;


public abstract class ATextureFragmentShaderFragment extends AShader implements IShaderFragment {
	protected List<ATexture> mTextures;
	
	protected RSampler2D[] muTextures;
	protected RSamplerCube[] muCubeTextures;
	protected RSamplerExternalOES[] muVideoTextures;
	protected RFloat[] muInfluence;
	protected RVec2[] muRepeat, muOffset;
	protected int[] muTextureHandles, muInfluenceHandles, muRepeatHandles, muOffsetHandles;

	public ATextureFragmentShaderFragment(List<ATexture> textures)
	{
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mTextures = textures;
		initialize();
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		if(mTextures == null) return;
		
		int numTextures = mTextures.size();

		int textureCount = 0, cubeTextureCount = 0, videoTextureCount = 0;
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			if(texture.getTextureType() == TextureType.CUBE_MAP)
				cubeTextureCount++;
			else if(texture.getTextureType() == TextureType.VIDEO_TEXTURE)
				videoTextureCount++;
			else
				textureCount++;
		}
		
		if(textureCount > 0)
			muTextures = new RSampler2D[textureCount];
		if(cubeTextureCount > 0)
			muCubeTextures = new RSamplerCube[cubeTextureCount];
		if(videoTextureCount > 0)
			muVideoTextures = new RSamplerExternalOES[videoTextureCount];
		muInfluence = new RFloat[numTextures];
		muRepeat = new RVec2[numTextures];
		muOffset = new RVec2[numTextures];
		muTextureHandles = new int[numTextures];
		muInfluenceHandles = new int[numTextures];
		muRepeatHandles = new int[numTextures];
		muOffsetHandles = new int[numTextures];

		textureCount = 0;
		cubeTextureCount = 0;
		videoTextureCount = 0;
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			if(texture.getTextureType() == TextureType.CUBE_MAP)
				muCubeTextures[textureCount++] = (RSamplerCube) addUniform(texture.getTextureName(), DataType.SAMPLERCUBE);
			else if(texture.getTextureType() == TextureType.VIDEO_TEXTURE)
				muVideoTextures[videoTextureCount++] = (RSamplerExternalOES) addUniform(texture.getTextureName(), DataType.SAMPLER_EXTERNAL_EOS);
			else
				muTextures[textureCount++] = (RSampler2D) addUniform(texture.getTextureName(), DataType.SAMPLER2D);			
			
			muInfluence[i] = (RFloat) addUniform(DefaultShaderVar.U_INFLUENCE, texture.getTextureName());
			
			if(texture.getWrapType() == WrapType.REPEAT)
				muRepeat[i] = (RVec2) addUniform(DefaultShaderVar.U_REPEAT, i);
			if(texture.offsetEnabled())
				muOffset[i] = (RVec2) addUniform(DefaultShaderVar.U_OFFSET, i);
		}
	}

	@Override
	public void setLocations(int programHandle) {
		if(mTextures == null) return;
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			muTextureHandles[i] = getUniformLocation(programHandle, texture.getTextureName());
			muInfluenceHandles[i] = getUniformLocation(programHandle, DefaultShaderVar.U_INFLUENCE, texture.getTextureName());
			if(texture.getWrapType() == WrapType.REPEAT)
				muRepeatHandles[i] = getUniformLocation(programHandle, DefaultShaderVar.U_REPEAT, i);
			if(texture.offsetEnabled())
				muOffsetHandles[i] = getUniformLocation(programHandle, DefaultShaderVar.U_OFFSET, i);
		}
	}

	@Override
	public void applyParams() {
		super.applyParams();
		
		if(mTextures == null) return;
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			GLES20.glUniform1f(muInfluenceHandles[i], texture.getInfluence());
			if(texture.getWrapType() == WrapType.REPEAT)
				GLES20.glUniform2fv(muRepeatHandles[i], 1, texture.getRepeat(), 0);
			if(texture.offsetEnabled())
				GLES20.glUniform2fv(muOffsetHandles[i], 1, texture.getOffset(), 0);
		}
	}
	
	@Override
	public void main() {
	}
}
