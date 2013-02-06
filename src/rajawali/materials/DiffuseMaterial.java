package rajawali.materials;

import rajawali.lights.ALight;

public class DiffuseMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"precision mediump float;\n" +
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
		
		"	vEyeVec = vec3(uMMatrix * position);\n" +
		"	vNormal = normalize(uNMatrix * normal);\n" +
		
		"%LIGHT_CODE%" +

		"	vColor = aColor;\n" +
		M_FOG_VERTEX_DENSITY +
		"}";
		
	protected static final String mFShader =
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 vEyeVec;\n" +
		"varying vec4 vColor;\n" +
 
		M_FOG_FRAGMENT_VARS +		
		"%LIGHT_VARS%" +
		
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" +
		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform sampler2D uNormalTexture;\n" +
		"uniform sampler2D uAlphaTexture;\n" +
		
		"void main() {\n" +
		"	vec4 Kd = vec4(0.0);\n" +
		"	float intensity = 0.0;\n" +
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

		"	vec4 ambient = uAmbientIntensity * uAmbientColor;\n" +
<<<<<<< HEAD
		"	gl_FragColor = ambient + diffuse;\n" +
=======
		"	gl_FragColor = diffuse + ambient;\n" +
>>>>>>> refs/heads/phong_ks_fix
		
	    "#ifdef ALPHA\n" +
		"	float alpha = texture2D(uAlphaTexture, vTextureCoord).r;\n" +
	    "	gl_FragColor.a = alpha;\n" + 
	    "#endif\n" +
		
		M_FOG_FRAGMENT_COLOR +		
		"}";
	
	public DiffuseMaterial() {
		this(false);
	}
	
	public DiffuseMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
	}
	
	public DiffuseMaterial(boolean isAnimated) {
		this(mVShader, mFShader, isAnimated);
	}
	
	public DiffuseMaterial(int parameters) {
		super(mVShader, mFShader, parameters);
	}
	
	public DiffuseMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer fc = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		fc.append("float normPower = 0.0;\n");

		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			if(light.getLightType() == ALight.POINT_LIGHT) {
				fc.append("L = normalize(uLightPosition").append(i).append(" - vEyeVec);\n");
				vc.append("dist = distance(vEyeVec, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
				fc.append("L = -normalize(uLightDirection").append(i).append(");\n");				
			}
			fc.append("normPower = uLightPower").append(i).append(" * max(dot(N, L), 0.1) * vAttenuation").append(i).append(";\n");
			fc.append("intensity += normPower;\n");
			fc.append("Kd.rgb += uLightColor").append(i).append(" * normPower;\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", fc.toString()));
	}
}