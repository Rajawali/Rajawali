package rajawali.materials;

import rajawali.lights.ALight;
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
		"%LIGHT_VARS%" +
		
		"\n#ifdef VERTEX_ANIM\n" +
		"attribute vec4 aNextFramePosition;\n" +
		"attribute vec3 aNextFrameNormal;\n" +
		"uniform float uInterpolation;\n" +
		"#endif\n\n" +
		
		"void main() {\n" +
		"	float dist = 0.0;\n" +
		"	vec4 position = aPosition;\n" +
		"	vec3 normal = aNormal;\n" +
		"	#ifdef VERTEX_ANIM\n" +
		"	position = aPosition + uInterpolation * (aNextFramePosition - aPosition);\n" +
		"	normal = aNormal + uInterpolation * (aNextFrameNormal - aNormal);\n" +
		"	#endif\n" +
		"	gl_Position = uMVPMatrix * position;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		
		"	vEyeVec = -vec3(uMMatrix  * position);\n" +
		"	vNormal = normalize(uNMatrix * normal);\n" +
		
		"%LIGHT_CODE%" +
		
		"	vColor = aColor;\n" +
		M_FOG_VERTEX_DENSITY +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +
		"precision mediump int;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 vEyeVec;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_FRAGMENT_VARS +
		"%LIGHT_VARS%" +
		
		"uniform float uShininess;\n" +
		"uniform vec4 uSpecularColor;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" + 
		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform sampler2D uNormalTexture;\n" +
		"uniform sampler2D uSpecularTexture;\n" +
		"uniform sampler2D uAlphaTexture;\n" +

		"void main() {\n" +
		"	vec4 Kd = vec4(0.0);\n" +
		"	float intensity = 0.0;\n" +
		"	float attenuation = 1.0;\n" +
		"	float Ks = 0.0;\n" +
		"	float NdotL = 0.0;\n" +
		"	vec3 N = normalize(vNormal);\n" +
		"	vec3 L = vec3(0.0);\n" +
		
		"#ifdef BUMP\n" +		
		"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);\n" +
		"	bumpnormal.z = -bumpnormal.z;\n" +
		"	N = normalize(N + bumpnormal);\n" +
	    "#endif\n" +
		
		"%LIGHT_CODE%" +
		
		"#ifdef TEXTURED\n" +
		"	vec4 diffuse = Kd * texture2D(uDiffuseTexture, vTextureCoord);\n" +
		"#else\n" +
	    "	vec4 diffuse = Kd * vColor;\n" +
	    "#endif\n" +
	    
		"#ifdef SPEC\n" +
		"   vec4 specular = Ks * uSpecularColor * texture2D(uSpecularTexture, vTextureCoord);\n" +
		"#else\n" +
	    "	vec4 specular = Ks * uSpecularColor;\n" + 
	    "#endif\n" +
		
	    "	vec4 ambient  = uAmbientIntensity * uAmbientColor;\n" + 
	    "	gl_FragColor = ambient + diffuse + specular;\n" + 	
	    
	    "#ifdef ALPHA\n" +
		"	float alpha = texture2D(uAlphaTexture, vTextureCoord).r;\n" +
	    "	gl_FragColor.a = alpha;\n" + 		
	    "#endif\n" +
	    
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
		mShininess = 96.0f;
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
		StringBuffer fc = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		fc.append("float normPower = 0.0;\n");
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);

			if(light.getLightType() == ALight.POINT_LIGHT) {
				fc.append("L = normalize(uLightPosition").append(i).append(" + vEyeVec);\n");
				vc.append("dist = distance(-vEyeVec, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				fc.append("L = normalize(-uLightDirection").append(i).append(");\n");
			}
<<<<<<< HEAD
			fc.append("normPower = uLightPower").append(i).append(" * max(dot(N, L), 0.1) * vAttenuation").append(i).append(";\n");
=======
			
			fc.append("NdotL = max(dot(N, L), 0.1);\n");
			fc.append("normPower = uLightPower").append(i).append(" * NdotL * vAttenuation").append(i).append(";\n");
>>>>>>> refs/heads/phong_ks_fix
			fc.append("intensity += normPower;\n"); 
			fc.append("Kd.rgb += uLightColor").append(i).append(" * normPower;\n"); 
			fc.append("Ks += pow(NdotL, uShininess) * vAttenuation").append(i).append(" * uLightPower").append(i).append(";\n");
		}
		
		super.setShaders(
				vertexShader.replace("%LIGHT_CODE%", vc.toString()), 
				fragmentShader.replace("%LIGHT_CODE%", fc.toString())
				);
		
		muSpecularColorHandle = getUniformLocation("uSpecularColor");
		muShininessHandle = getUniformLocation("uShininess");
	}
}