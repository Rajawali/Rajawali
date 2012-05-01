package rajawali.materials;

import android.graphics.Color;
import android.opengl.GLES20;


public class PhongMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat4 uVMatrix;\n" +
		
		M_LIGHTS_VARS +
		M_FOG_VERTEX_VARS +
		
		"attribute vec4 aPosition;\n" +
		"attribute vec3 aNormal;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N;\n" +
		"varying vec3 L["+MAX_LIGHTS+"], H["+MAX_LIGHTS+"];\n" +
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
		"	N = normalize(uNMatrix * normal);\n" +
		"	vec3 E = -normalize(eyePosition.xyz);\n" +
		
		"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
		"		vec4 eyeLightPos = vec4(uLightPos[i], 1.0);\n" +
		"		L[i] = normalize(eyeLightPos.xyz - eyePosition.xyz);\n" + 
		"		H[i] = normalize(L[i] + E);\n" +
		"	}" +
		"	vColor = aColor;\n" +
		M_FOG_VERTEX_DEPTH +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N;\n" +
		"varying vec3 L["+MAX_LIGHTS+"], H["+MAX_LIGHTS+"];\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_FRAGMENT_VARS +
		M_LIGHTS_VARS +
		
		"uniform vec4 uSpecularColor;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" + 
		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform float uShininess;\n" +
		"uniform bool uUseTexture;\n" +

		"void main() {\n" +
		"	float Kd = 0.0;" +
		"	float Ks = 0.0;" +

		"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
		"		vec3 Half   = normalize(H[i]);\n" +
		"		vec3 Light  = normalize(L[i]);\n" +
		
		"		Kd += max(dot(N, Light), 0.0) * uLightPower[i];\n" + 
		"		Ks += pow(max(dot(Half, N), 0.0), uShininess) * uLightPower[i];\n" +
		"	}" +
	    "	vec4 diffuse  = uUseTexture ? Kd * texture2D(uDiffuseTexture, vTextureCoord) : Kd * vColor;\n" + 
	    "	vec4 specular = Ks * uSpecularColor;\n" + 
	    "	vec4 ambient  = uAmbientIntensity * uAmbientColor;\n" + 
	    M_FOG_FRAGMENT_CALC +
	    "	gl_FragColor = ambient + diffuse + specular;\n" + 
	    M_FOG_FRAGMENT_COLOR +
		"}";
	
	protected int muSpecularColorHandle;
	protected int muShininessHandle;
	
	protected float[] mSpecularColor;
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
	
	public void setShininess(float shininess) {
		mShininess = shininess;
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muSpecularColorHandle = getUniformLocation("uSpecularColor");
		muShininessHandle = getUniformLocation("uShininess");
	}
}