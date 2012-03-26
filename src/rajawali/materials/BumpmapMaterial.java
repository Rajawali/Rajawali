package rajawali.materials;

import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.wallpaper.Wallpaper;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

public class BumpmapMaterial extends AMaterial {
	protected static final String mVShader = 
			"uniform mat4 uMVPMatrix;\n" +
			"uniform mat3 uNMatrix;\n" +
			"uniform mat4 uMMatrix;\n" +
			"uniform mat4 uVMatrix;\n" +
			
			"attribute vec4 aPosition;\n" +
			"attribute vec2 aTextureCoord;\n" +
			
			"varying vec2 vTextureCoord;\n" +
			
			"void main() {\n" +
			"	gl_Position = uMVPMatrix * aPosition;\n" +
			"	vTextureCoord = aTextureCoord;\n" +
			"}";
			
		protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			
			"uniform sampler2D uTexture0;\n" +
			"uniform sampler2D normalTexture;\n" +
			"uniform vec3 uLightPos;\n" +

			"void main() {\n" +
			"	vec3 normal = normalize(texture2D(normalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	normal.z = -normal.z;" +
		    "	float diffuse = max(dot(normal, normalize(uLightPos)), 0.0);" +
			" 	vec3 color = diffuse * texture2D(uTexture0, vTextureCoord).rgb;" +
		    "	gl_FragColor = vec4(color, 1.0);\n" + 
			"}";
	
	protected int muLightPosHandle;
	protected int muNormalMatrixHandle;

	protected float[] mLightPos;

	public BumpmapMaterial() {
		super(mVShader, mFShader);
		mLightPos = new float[3];
	}
	
	@Override
	public void setLight(ALight light) {
		super.setLight(light);

		DirectionalLight dirLight = (DirectionalLight)light;
		mLightPos[0] = dirLight.getPosition().x;
		mLightPos[1] = dirLight.getPosition().y;
		mLightPos[2] = dirLight.getPosition().z;
		GLES20.glUniform3fv(muLightPosHandle, 1, mLightPos, 0);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muLightPosHandle = GLES20.glGetUniformLocation(mProgram, "uLightPos");
		if(muLightPosHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uLightPos");
		}
	}
}
