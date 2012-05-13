package rajawali.materials;

public class BumpmapPhongMaterial extends PhongMaterial {
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
			"uniform sampler2D uNormalTexture;\n" +
			"uniform float uShininess;\n" +
			"uniform bool uUseTexture;\n" +

			"void main() {\n" +
			"	float Kd = 0.0;" +
			"	float Ks = 0.0;" +
			"	vec3 N = normalize(vNormal);\n" +
			"	vec3 E = normalize(vEyeVec);\n" +
			
			"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + N);" +
			
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
			
			"		float NdotL = max(dot(bumpnormal, L), 0.1);\n" +
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
	
	public BumpmapPhongMaterial() {
		this(false);
	}
	
	public BumpmapPhongMaterial(boolean isAnimated) {
		super(PhongMaterial.mVShader, mFShader, isAnimated);
	}
}
