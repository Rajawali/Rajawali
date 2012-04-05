package rajawali.materials;

import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;


public class PhongMaterial extends AAdvancedMaterial {
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
		"varying vec3 N, L, E, H;\n" +
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
		
		"	vec4 eyePosition = uMMatrix  * position;\n" + 
		"	vec4 eyeLightPos = vec4(uLightPos, 1.0);\n" +
		"	N = normalize(uNMatrix * normal);\n" +
		"	L = normalize(eyeLightPos.xyz - eyePosition.xyz);\n" + 
		"	E = -normalize(eyePosition.xyz);\n" +
		"	H = normalize(L + E);\n" +
		"	vColor = aColor;\n" +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N, L, E, H;\n" +
		"varying vec4 vColor;\n" +
		
		"uniform vec4 uSpecularColor;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" + 
		"uniform sampler2D uTexture0;\n" +
		"uniform sampler2D normalTexture;\n" +
		"uniform float uShininess;\n" +
		"uniform bool uUseTexture;\n" +

		"void main() {\n" +
		"	vec3 Normal = normalize(N);\n" +
		"	vec3 Light  = normalize(L);\n" +
		"	vec3 Eye    = normalize(E);\n" +
		"	vec3 Half   = normalize(H);\n" +
		
		"	float Kd = max(dot(Normal, Light), 0.0);\n" + 
		"	float Ks = pow(max(dot(Half, Normal), 0.0), uShininess);\n" + 
	    "	vec4 diffuse  = uUseTexture ? Kd * texture2D(uTexture0, vTextureCoord) : Kd * vColor;\n" + 
	    "	vec4 specular = Ks * uSpecularColor;\n" + 
	    "	vec4 ambient  = uAmbientIntensity * uAmbientColor;\n" + 
	    "	gl_FragColor = ambient + diffuse + specular;\n" + 
		"}";
	
	protected int muSpecularColorHandle;
	protected int muAmbientColorHandle;
	protected int muShininessHandle;
	
	protected float[] mSpecularColor;
	protected float[] mAmbientColor;
	protected float mShininess;
	
	public PhongMaterial() {
		this(false);
	}
	
	public PhongMaterial(boolean isAnimated) {
		this(mVShader, mFShader, isAnimated);
	}
	
	public PhongMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
		mSpecularColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		mAmbientColor = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
		mShininess = 96.0f;//
	}
	
	public PhongMaterial(float[] specularColor, float[] ambientColor, float shininess) {
		this();
		mSpecularColor = specularColor;
		mAmbientColor = ambientColor;
		mShininess = shininess;
	}

	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muSpecularColorHandle, 1, mSpecularColor, 0);
		GLES20.glUniform4fv(muAmbientColorHandle, 1, mAmbientColor, 0);
		GLES20.glUniform1f(muShininessHandle, mShininess);
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
	
	public void setAmbientcolor(float[] color) {
		mAmbientColor = color;
	}
	
	public void setAmbientcolor(float r, float g, float b, float a) {
		setAmbientcolor(new float[] { r, g, b, a });
	}
	
	public void setAmbientcolor(int color) {
		setAmbientcolor(new float[] { Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color) });
	}
	
	public void setShininess(float shininess) {
		mShininess = shininess;
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muSpecularColorHandle = GLES20.glGetUniformLocation(mProgram, "uSpecularColor");
		if(muSpecularColorHandle == -1) {
			Log.d(RajawaliRenderer.TAG, "Could not get uniform location for uSpecularColor");
		}
		muAmbientColorHandle = GLES20.glGetUniformLocation(mProgram, "uAmbientColor");
		if(muAmbientColorHandle == -1) {
			Log.d(RajawaliRenderer.TAG, "Could not get uniform location for uAmbientColor");
		}
		muShininessHandle = GLES20.glGetUniformLocation(mProgram, "uShininess");
		if(muShininessHandle == -1) {
			Log.d(RajawaliRenderer.TAG, "Could not get uniform location for uShininess");
		}
	}
}