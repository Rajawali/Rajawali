package rajawali.materials;

import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;


public class ParticleMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform float uPointSize;\n" +
		"uniform mat4 uMMatrix;\n" +
		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"varying vec2 vTextureCoord;\n" +

		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"   vec4 pos = uMMatrix * aPosition;\n" +
		"	gl_PointSize = uPointSize;\n" + // / (pos.z + 4.0);\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D uTexture0;\n" +

		"void main() {\n" +
		"	gl_FragColor = texture2D(uTexture0, gl_PointCoord);\n" +
		"}\n";
	
	protected float mPointSize = 10.0f;
	protected int muPointSizeHandle;
	
	public ParticleMaterial() {
		super(mVShader, mFShader);
	}
	
	public void setPointSize(float pointSize) {
		mPointSize = pointSize;
       	GLES20.glUniform1f(muPointSizeHandle, mPointSize);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muPointSizeHandle = GLES20.glGetUniformLocation(mProgram, "uPointSize");
		if(muPointSizeHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uPointSize");
		}
	}
}
