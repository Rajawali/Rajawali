package rajawali.materials;

import rajawali.lights.ALight;

public class BumpmapPhongMaterial extends PhongMaterial {
	protected static final String mFShader = 
			"precision mediump float;\n" +
			"precision mediump int;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 vNormal;\n" +
			"varying vec3 vEyeVec;\n" +
			"varying vec4 vColor;\n" +
			
			M_FOG_FRAGMENT_VARS +
			"%LIGHT_VARS%" +
			
			"uniform vec4 uSpecularColor;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" + 
			"uniform sampler2D uDiffuseTexture;\n" +
			"uniform sampler2D uNormalTexture;\n" +
			"uniform float uShininess;\n" +

			"void main() {\n" +
			"	float Kd = 0.0;" +
			"	float Ks = 0.0;" +
			"	float NdotL = 0.0;\n" +
			"	vec3 N = normalize(vNormal);\n" +
			"	vec3 E = normalize(vEyeVec);\n" +
			"	vec3 L = vec3(0.0);\n" +
			"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + N);" +
			
			"%LIGHT_CODE%" +
			
			"#ifdef TEXTURED\n" +
		    "	vec4 diffuse  = Kd * texture2D(uDiffuseTexture, vTextureCoord);\n" +
			"#else\n" +
		    "	vec4 diffuse  = Kd * vColor;\n" +
		    "#endif\n" +
		    "	vec4 specular = Ks * uSpecularColor;\n" + 
		    "	vec4 ambient  = uAmbientIntensity * uAmbientColor;\n" + 
		    "	gl_FragColor = ambient + diffuse + specular;\n" + 
		    M_FOG_FRAGMENT_COLOR +	
			"}";
	
	public BumpmapPhongMaterial() {
		this(false);
	}
	
	public BumpmapPhongMaterial(boolean isAnimated) {
		super(PhongMaterial.mVShader, mFShader, isAnimated);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		StringBuffer fc = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);

			if(light.getLightType() == ALight.POINT_LIGHT) {
				fc.append("L = normalize(uLightPosition").append(i).append(" + vEyeVec);\n");
				
				vc.append("dist = distance(-vEyeVec, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				fc.append("L = normalize(-uLightDirection").append(i).append(");\n");
			}
		
			fc.append("NdotL = max(dot(bumpnormal, L), 0.1);\n");
			fc.append("Kd += NdotL * vAttenuation").append(i).append(" * uLightPower").append(i).append(";\n"); 
			fc.append("Ks += pow(NdotL, uShininess) * vAttenuation").append(i).append(" * uLightPower").append(i).append(";\n");
		}
		
		super.setShaders(
				vertexShader.replace("%LIGHT_CODE%", vc.toString()), 
				fragmentShader.replace("%LIGHT_CODE%", fc.toString())
				);		
	}
}
