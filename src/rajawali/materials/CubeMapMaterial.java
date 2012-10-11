package rajawali.materials;

import rajawali.lights.ALight;

public class CubeMapMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"precision mediump float;\n" +

		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform vec3 uLightPos;\n" +
		"uniform vec3 uCameraPosition;\n" +
		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec3 aNormal;\n" +
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		
		M_FOG_VERTEX_VARS +
		"%LIGHT_VARS%" +
		
		"void main() {\n" +
		"	float dist = 0.0;\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	V = uMMatrix * aPosition;\n" +
		"	vec3 eyeDir = normalize(V.xyz - uCameraPosition.xyz);\n" +
		"	N = normalize(uNMatrix * aNormal);\n" +
		"	vReflectDir = reflect(eyeDir, N);\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	vNormal = aNormal;\n" +
		"%LIGHT_CODE%" +
		M_FOG_VERTEX_DENSITY +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"uniform samplerCube uCubeMapTexture;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec3 vNormal;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" +
		
		M_FOG_FRAGMENT_VARS +
		"%LIGHT_VARS%" +

		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"%LIGHT_CODE%" +
		"	gl_FragColor = textureCube(uCubeMapTexture, vReflectDir);\n" +
		"	gl_FragColor += uAmbientColor * uAmbientIntensity;" +
		"	gl_FragColor.rgb *= intensity;\n" +
		M_FOG_FRAGMENT_COLOR +	
		"}\n";
	
	public CubeMapMaterial() {
		super(mVShader, mFShader);
		usesCubeMap = true;
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer sb = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			sb.append("vec3 L = vec3(0.0);\n");
			
			if(light.getLightType() == ALight.POINT_LIGHT) {
				sb.append("L = normalize(uLightPosition").append(i).append(" - V.xyz);\n");
				vc.append("dist = distance(V.xyz, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
				sb.append("L = -normalize(uLightDirection").append(i).append(");");				
			}
			sb.append("intensity += uLightPower").append(i).append(" * max(dot(N, L), 0.1) * vAttenuation").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", sb.toString()));
	}
}
