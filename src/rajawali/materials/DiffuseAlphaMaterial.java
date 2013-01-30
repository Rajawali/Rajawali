package rajawali.materials;

import rajawali.lights.ALight;

public class DiffuseAlphaMaterial extends DiffuseMaterial {
	protected static final String mFShader =
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec4 vColor;\n" +
 
		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform sampler2D uAlphaTexture;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" +
		
		M_FOG_FRAGMENT_VARS +		
		"%LIGHT_VARS%" +
		
		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"	float dist = 0.0;\n" +
		"	vec3 L = vec3(0.0);\n" +

		"	gl_FragColor = texture2D(uDiffuseTexture, vTextureCoord);\n" +
		"	gl_FragColor.a = texture2D(uAlphaTexture, vTextureCoord).r;\n" +

	    "%LIGHT_CODE%" +

		"	gl_FragColor.rgb = uAmbientIntensity.rgb * uAmbientColor.rgb + intensity * gl_FragColor.rgb;\n" +
		M_FOG_FRAGMENT_COLOR +		
		"}";
	
	public DiffuseAlphaMaterial() {
		this(false);
	}
	
	public DiffuseAlphaMaterial(boolean isAnimated) {
		super(DiffuseMaterial.mVShader, mFShader, isAnimated);
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
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
				sb.append("L = -normalize(uLightDirection").append(i).append(");\n");				
			}
			sb.append("intensity += uLightPower").append(i).append(" * max(dot(N, L), 0.1) * vAttenuation").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", sb.toString()));
	}
}