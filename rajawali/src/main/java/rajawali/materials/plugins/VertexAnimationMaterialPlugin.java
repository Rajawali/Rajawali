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
package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.AShaderBase.DataType;
import rajawali.materials.shaders.AShaderBase.IGlobalShaderVar;
import rajawali.materials.shaders.fragments.animation.VertexAnimationVertexShaderFragment;


public class VertexAnimationMaterialPlugin implements IMaterialPlugin {
	public static enum VertexAnimationShaderVar implements IGlobalShaderVar {
		A_NEXT_FRAME_POSITION("aNextFramePosition", DataType.VEC4),
		A_NEXT_FRAME_NORMAL("aNextFrameNormal", DataType.VEC3),
		U_INTERPOLATION("uInterpolation", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		VertexAnimationShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}

		public String getVarString() {
			return mVarString;
		}

		public DataType getDataType() {
			return mDataType;
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
