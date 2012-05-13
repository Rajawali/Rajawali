package rajawali.materials;

public class CubeMapMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
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
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	V = uMMatrix * aPosition;\n" +
		"	vec3 eyeDir = normalize(V.xyz - uCameraPosition.xyz);\n" +
		"	N = normalize(uNMatrix * aNormal);\n" +
		"	vReflectDir = reflect(eyeDir, N);\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	vNormal = aNormal;\n" +
		M_FOG_VERTEX_DEPTH +
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
		M_LIGHTS_VARS +

		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
		"		vec3 L = vec3(0.0);" +
		"		float attenuation = 1.0;" +
		"		if(uLightType[i] == POINT_LIGHT) {" +
		"			L = normalize(uLightPosition[i] - V.xyz);\n" +
		"			float dist = distance(V.xyz, uLightPosition[i]);\n" +
		"			attenuation = 1.0 / (uLightAttenuation[i][1] + uLightAttenuation[i][2] * dist + uLightAttenuation[i][3] * dist * dist);\n" +
		"		} else {" +
		"			L = -normalize(uLightDirection[i]);" +
		"		}" +
		"		intensity += uLightPower[i] * max(dot(N, L), 0.1) * attenuation;\n" +
		"	}\n" +
		"	gl_FragColor = textureCube(uCubeMapTexture, vReflectDir);\n" +
		"	gl_FragColor += uAmbientColor * uAmbientIntensity;" +
		M_FOG_FRAGMENT_CALC +
		"	gl_FragColor.rgb *= intensity;\n" +
		M_FOG_FRAGMENT_COLOR +	
		"}\n";
	
	public CubeMapMaterial() {
		super(mVShader, mFShader);
		usesCubeMap = true;
	}
}
