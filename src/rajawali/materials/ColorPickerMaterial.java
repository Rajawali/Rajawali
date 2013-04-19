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
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public ColorPickerMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
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
