package rajawali.materials;

import rajawali.lights.ALight;

public class BumpmapMaterial extends AAdvancedMaterial {
		protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N;\n" +
			"varying vec4 V;\n" +
			"varying vec4 vColor;\n" +
			
			"uniform sampler2D uDiffuseTexture;\n" +
			"uniform sampler2D uNormalTexture;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" +
			
			M_FOG_FRAGMENT_VARS +
			"%LIGHT_VARS%" +

			"void main() {\n" +
			"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + N);" +
		    "	float intensity = 0.0;" +
		    "	vec3 L = vec3(0.0);\n" +
		    "%LIGHT_CODE%" +
			" 	vec3 color = intensity * texture2D(uDiffuseTexture, vTextureCoord).rgb;" +
		    "	gl_FragColor = vec4(color, 1.0) + uAmbientColor * uAmbientIntensity;\n" + 
		    M_FOG_FRAGMENT_COLOR +
			"}";

	public BumpmapMaterial() {
		this(false);
	}
	
	public BumpmapMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
	}
	
	public BumpmapMaterial(boolean isAnimated) {
		this(DiffuseMaterial.mVShader, mFShader, isAnimated);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer sb = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			if(light.getLightType() == ALight.POINT_LIGHT) {
				sb.append("L = normalize(uLightPosition").append(i).append(" - V.xyz);\n");
				vc.append("dist = distance(V.xyz, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				sb.append("L = -normalize(uLightDirection").append(i).append(");");				
			}
			sb.append("intensity += uLightPower").append(i).append(" * max(dot(bumpnormal, L), 0.1) * vAttenuation").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", sb.toString()));
	}

}
