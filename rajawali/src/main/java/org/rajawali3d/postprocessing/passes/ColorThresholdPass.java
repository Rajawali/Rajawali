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

import android.graphics.Color;
import org.rajawali3d.R;


public class ColorThresholdPass extends EffectPass {
	private float[] mLowerThreshold;
	private float[] mUpperThreshold;
	
	public ColorThresholdPass(int lowerThreshold, int upperThreshold) {
		super();
		createMaterial(R.raw.minimal_vertex_shader, R.raw.color_threshold_shader);
		mLowerThreshold = new float[] {
			Color.red(lowerThreshold) / 255.f,
			Color.green(lowerThreshold) / 255.f,
			Color.blue(lowerThreshold) / 255.f
		};
		mUpperThreshold = new float[] {
			Color.red(upperThreshold) / 255.f,
			Color.green(upperThreshold) / 255.f,
			Color.blue(upperThreshold) / 255.f
		};
	}
	
	public void setShaderParams()
	{
		super.setShaderParams();
		mFragmentShader.setUniform3fv("uLowerThreshold", mLowerThreshold);
		mFragmentShader.setUniform3fv("uUpperThreshold", mUpperThreshold);
	}
}
