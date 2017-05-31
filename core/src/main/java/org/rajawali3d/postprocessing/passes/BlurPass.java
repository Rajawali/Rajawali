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

import org.rajawali3d.R;

public class BlurPass extends EffectPass {
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	protected Direction mBlurDirection;
	protected float[] mDirection;
	protected float mRadius;
	protected float mResolution;
	
	public BlurPass(Direction direction, float radius, int screenWidth, int screenHeight) {
		super();
		mDirection = direction == Direction.HORIZONTAL ? new float[]{1, 0} : new float[]{0, 1};
		mRadius = radius;
		mBlurDirection = direction;
		setSize(screenWidth, screenHeight);
		createMaterial(R.raw.minimal_vertex_shader, R.raw.blur_fragment_shader);
	}
	
	public void setShaderParams()
	{
		super.setShaderParams();
		mFragmentShader.setUniform2fv("uDirection", mDirection);
		mFragmentShader.setUniform1f("uRadius", mRadius);
		mFragmentShader.setUniform1f("uResolution", mResolution);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		mResolution = mBlurDirection == Direction.HORIZONTAL ? width : height;
	}
}
