package rajawali.materials;

import rajawali.math.Number3D;
import android.graphics.Color;
import android.opengl.GLES20;


public class PhongMaterial extends AAdvancedMaterial {
	protected static final String mVShader =
		"precision mediump float;\n" +
		"precision mediump int;\n" +
		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat4 uVMatrix;\n" +
		
		"attribute vec4 aPosition;\n" +
		"attribute vec3 aNormal;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 vEyeVec;\n" +
		"varying vec4 vColor;\n" +

		M_FOG_VERTEX_VARS +
		
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
		
		"	vEyeVec = -vec3(uMMatrix  * position);\n" +
		"	vNormal = uNMatrix * normal;\n" +
		
		"	vColor = aColor;\n" +
		M_FOG_VERTEX_DEPTH +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +
		"precision mediump int;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 vLightDir["+MAX_LIGHTS+"];\n" +
		"varying float vAttenuation["+MAX_LIGHTS+"];\n" +
		"varying vec3 vEyeVec;\n" +
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
		"	float Kd = 0.0;\n" +
		"	float Ks = 0.0;\n" +
		"	vec3 N = normalize(vNormal);\n" +
		"	vec3 E = normalize(vEyeVec);\n" +
		
		"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {\n" +
		"		vec3 L = vec3(0);\n" +
		"		float attenuation = 1.0;\n" +
		
		"		if(uLightType[i] == POINT_LIGHT) {\n" +
		"			L = normalize(uLightPosition[i] + vEyeVec);\n" +
		"			float dist = distance(-vEyeVec, uLightPosition[i]);\n" +
		"			attenuation = 1.0 / (uLightAttenuation[i][1] + uLightAttenuation[i][2] * dist + uLightAttenuation[i][3] * dist * dist);\n" +
		"		} else {\n" +
		"			L = normalize(-uLightDirection[i]);\n" +
		"		}\n" +
		
		"		float NdotL = max(dot(N, L), 0.1);\n" +
		"		Kd += NdotL * attenuation * uLightPower[i];\n" + 
		"		Ks += pow(NdotL, uShininess) * attenuation * uLightPower[i];\n" +
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
	
	public void setSpecularColor(Number3D color) {
		mSpecularColor[0] = color.x;
		mSpecularColor[1] = color.y;
		mSpecularColor[2] = color.z;
		mSpecularColor[3] = 1;
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