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

import android.opengl.GLES20;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

import c.org.rajawali3d.gl.glsl.ShaderVariable;
import c.org.rajawali3d.textures.BaseTexture;
import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.textures.annotation.Wrap;

import java.util.List;


public abstract class ATextureFragmentShaderFragment extends AShader implements IShaderFragment {
	protected List<BaseTexture> mTextures;

	protected RSampler2D[] muTextures;
	protected RSamplerCube[] muCubeTextures;
	protected RSamplerExternalOES[] muVideoTextures;
	protected RFloat[] muInfluence;
	protected RVec2[] muRepeat, muOffset;
	protected int[] muTextureHandles, muInfluenceHandles, muRepeatHandles, muOffsetHandles;

	public ATextureFragmentShaderFragment(List<BaseTexture> textures)
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
			BaseTexture texture = mTextures.get(i);
			if(texture.getTextureType() == Type.CUBE_MAP)
				cubeTextureCount++;
			else if(texture.getTextureType() == Type.VIDEO_TEXTURE)
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
			BaseTexture texture = mTextures.get(i);
			if(texture.getTextureType() == Type.CUBE_MAP)
				muCubeTextures[textureCount++] = (RSamplerCube) addUniform(texture.getTextureName(), ShaderVariable.SAMPLERCUBE);
			else if(texture.getTextureType() == Type.VIDEO_TEXTURE)
				muVideoTextures[videoTextureCount++] = (RSamplerExternalOES) addUniform(texture.getTextureName(), ShaderVariable.SAMPLER_EXTERNAL_EOS);
			else
				muTextures[textureCount++] = (RSampler2D) addUniform(texture.getTextureName(), ShaderVariable.SAMPLER2D);

			muInfluence[i] = (RFloat) addUniform(DefaultShaderVar.U_INFLUENCE, texture.getTextureName());

			if(texture.getWrapType() == (Wrap.REPEAT_S | Wrap.REPEAT_T | Wrap.REPEAT_R))
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
			BaseTexture texture = mTextures.get(i);
			muTextureHandles[i] = getUniformLocation(programHandle, texture.getTextureName());
			muInfluenceHandles[i] = getUniformLocation(programHandle, DefaultShaderVar.U_INFLUENCE, texture.getTextureName());
			if(texture.getWrapType() == (Wrap.REPEAT_S | Wrap.REPEAT_T | Wrap.REPEAT_R))
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
			BaseTexture texture = mTextures.get(i);
			GLES20.glUniform1f(muInfluenceHandles[i], texture.getInfluence());
			if(texture.getWrapType() == (Wrap.REPEAT_S | Wrap.REPEAT_T | Wrap.REPEAT_R))
				GLES20.glUniform2fv(muRepeatHandles[i], 1, texture.getRepeat(), 0);
			if(texture.offsetEnabled())
				GLES20.glUniform2fv(muOffsetHandles[i], 1, texture.getOffset(), 0);
		}
	}

	@Override
	public void main() {
	}
}
