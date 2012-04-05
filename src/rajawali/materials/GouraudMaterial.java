package rajawali.materials;

import rajawali.wallpaper.Wallpaper;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;


public class GouraudMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat4 uVMatrix;\n" +
		"uniform vec3 uLightPos;\n" +
		
		"attribute vec4 aPosition;\n" +
		"attribute vec3 aNormal;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying float vSpecularIntensity;\n" +
		"varying float vDiffuseIntensity;\n" +
		"varying vec4 vColor;\n" +
		
		"\n#ifdef VERTEX_ANIM\n" +
		"attribute vec4 aNextFramePosition;\n" +
		"attribute vec3 aNextFrameNormal;\n" +
		"uniform float uInterpolation;\n" +
		"#endif\n\n" +
		
		"void main() {\n" +
		"	vec4 position = aPosition;\n" +
		"	vec3 normal = aNormal;\n" +
		"	#ifdef VERTEX_ANIM\n" +
		"	position = aPosition + uInterpolation * (aNextFramePosition - aPosition);\n" +
		"	normal = aNormal + uInterpolation * (aNextFrameNormal - aNormal);\n" +
		"	#endif\n" +
		
		"	gl_Position = uMVPMatrix * position;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		
		"	vec4 vertexPosCam = uMMatrix * position;\n" +
		"	vec3 normalCam = normalize(uNMatrix * normal);\n" +
		"	vec4 lightPosCam = vec4(uLightPos, 1.0);\n" +

		"	vec3 lightVert = normalize(vec3(lightPosCam - vertexPosCam));\n" +
		"	vec3 lightRefl = normalize(reflect(lightVert, normalCam));\n" +

		"	vDiffuseIntensity = max(dot(lightVert, normalCam), 0.0);\n" +
		"	vSpecularIntensity = max(dot(lightRefl, normalize(vec3(vertexPosCam))), 0.0);\n" +
		"	vSpecularIntensity = pow(vSpecularIntensity, 6.0);\n" +
		"	vColor = aColor;\n" +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying float vSpecularIntensity;\n" +
		"varying float vDiffuseIntensity;\n" +
		"varying vec4 vColor;\n" +
		
		"uniform vec4 uSpecularColor;\n" +
		"uniform sampler2D uTexture0;\n" +
		"uniform bool uUseTexture;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" + 

		"void main() {\n" +
		"	vec4 texColor = uUseTexture ? texture2D(uTexture0, vTextureCoord) : vColor;\n" +
		"	gl_FragColor = texColor * vDiffuseIntensity + uSpecularColor * vSpecularIntensity;\n" +
		"	gl_FragColor.a = texColor.a;\n" +
		"	gl_FragColor += uAmbientColor * uAmbientIntensity;" +
		"}";
	
	protected int muSpecularColorHandle;
	protected float[] mSpecularColor;
	
	public GouraudMaterial() {
		this(false);
	}
	
	public GouraudMaterial(boolean isAnimated) {
		super(mVShader, mFShader, isAnimated);
		mSpecularColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	}
	
	public GouraudMaterial(float[] specularColor) {
		this();
		mSpecularColor = specularColor;
	}

	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muSpecularColorHandle, 1, mSpecularColor, 0);
	}
	
	public void setSpecularColor(float[] color) {
		mSpecularColor = color;
	}
	
	public void setSpecularColor(float r, float g, float b, float a) {
		setSpecularColor(new float[] { r, g, b, a });
	}
	
	public void setSpecularColor(int color) {
		setSpecularColor(new float[] { Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color) });
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muSpecularColorHandle = GLES20.glGetUniformLocation(mProgram, "uSpecularColor");
		if(muSpecularColorHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uSpecularColor");
		}
	}
}