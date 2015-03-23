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
package org.rajawali3d.postprocessing.passes;

import android.opengl.GLES20;
import org.rajawali3d.R;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.util.RawShaderLoader;


public class CopyPass extends Material {
	public CopyPass()
	{
		super();
		mCustomVertexShader = new MinimalVertexShader();
		mCustomFragmentShader = new CopyFragmentShader();
	}
	
	public void setOpacity(float opacity)
	{
		((CopyFragmentShader)mCustomFragmentShader).setOpacity(opacity);
	}
	
	private class MinimalVertexShader extends VertexShader
	{
		public MinimalVertexShader() {
			super();
			mNeedsBuild = false;
			mShaderString = RawShaderLoader.fetch(R.raw.minimal_vertex_shader);
		}
	}
	
	private class CopyFragmentShader extends FragmentShader
	{
		private int muOpacityHandle;
		private float mOpacity;
		
		public CopyFragmentShader() {
			super();
			mNeedsBuild = false;
			mShaderString = RawShaderLoader.fetch(R.raw.copy_fragment_shader);
		}
		
		@Override
		public void setLocations(final int programHandle)
		{
			super.setLocations(programHandle);
			muOpacityHandle = getUniformLocation(programHandle, "uOpacity");
		}
		
		@Override
		public void applyParams()
		{
			super.applyParams();
			GLES20.glUniform1f(muOpacityHandle, mOpacity);
		}
		
		public void setOpacity(float opacity)
		{
			mOpacity = opacity;
		}
	}
}
