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
package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.shaders.IShaderFragment;
import c.org.rajawali3d.gl.glsl.ShaderVariable;
import org.rajawali3d.materials.shaders.AShaderBase.IGlobalShaderVar;
import org.rajawali3d.materials.shaders.fragments.animation.VertexAnimationVertexShaderFragment;


public class VertexAnimationMaterialPlugin implements IMaterialPlugin {
	public static enum VertexAnimationShaderVar implements IGlobalShaderVar {
		A_NEXT_FRAME_POSITION("aNextFramePosition", ShaderVariable.VEC4),
		A_NEXT_FRAME_NORMAL("aNextFrameNormal", ShaderVariable.VEC3),
		U_INTERPOLATION("uInterpolation", ShaderVariable.FLOAT);
		
		private String mVarString;
		private ShaderVariable mShaderVariable;

		VertexAnimationShaderVar(String varString, ShaderVariable shaderVariable) {
			mVarString = varString;
			mShaderVariable = shaderVariable;
		}

		public String getVarString() {
			return mVarString;
		}

		public ShaderVariable getShaderVariable() {
			return mShaderVariable;
		}
	}	
	
	private VertexAnimationVertexShaderFragment mVertexShader; 
	
	public VertexAnimationMaterialPlugin()
	{
		mVertexShader = new VertexAnimationVertexShaderFragment();
	}
	
	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	public IShaderFragment getFragmentShaderFragment() {
		return null;
	}
	
	public void setNextFrameVertices(final int vertexBufferHandle)
	{
		mVertexShader.setNextFrameVertices(vertexBufferHandle);
	}
	
	public void setNextFrameNormals(final int normalBufferHandle)
	{
		mVertexShader.setNextFrameNormals(normalBufferHandle);
	}
	
	public void setInterpolation(double interpolation) {
		mVertexShader.setInterpolation(interpolation);
	}

	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.PRE_LIGHTING;
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}
}
