package rajawali.materials;

import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;

public class ColorPickerMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform vec4 uPickingColor;\n" +

		"attribute vec4 aPosition;\n" +

		"varying vec4 vColor;\n" +		

		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vColor = uPickingColor;\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec4 vColor;\n" +

		"void main() {\n" +
		"	gl_FragColor = vColor;\n" +
		"}\n";
	
	protected int muPickingColorHandle;
	protected float[] mPickingColor;
	
	public ColorPickerMaterial() {
		super(mVShader, mFShader, false);		
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
