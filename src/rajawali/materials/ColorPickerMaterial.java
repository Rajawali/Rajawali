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
package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;

public class ColorPickerMaterial extends AMaterial {
	
	protected int muPickingColorHandle;
	protected float[] mPickingColor;
	
	public ColorPickerMaterial() {
		super(R.raw.color_picker_material_vertex, R.raw.color_picker_material_fragment);	
	}
	
	public ColorPickerMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muPickingColorHandle, 1, mPickingColor, 0);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muPickingColorHandle = GLES20.glGetUniformLocation(mProgram, "uPickingColor");
		if(muPickingColorHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uPickingColor");
		}
	}
	
	public void setPickingColor(float[] color) {
		mPickingColor = color;
	}
}
