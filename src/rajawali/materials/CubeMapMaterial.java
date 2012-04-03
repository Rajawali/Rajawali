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
		"varying vec3 N, L;\n" +
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vec4 transfPos = uMMatrix * aPosition;\n" +
		"	vec3 eyeDir = normalize(transfPos.xyz - uCameraPosition.xyz);\n" +
		"	N = uNMatrix * aNormal;\n" +
		"	vReflectDir = reflect(eyeDir, N);\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	L = uLightPos.xyz - aPosition.xyz;\n" +
		"	vNormal = aNormal;\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"uniform samplerCube uTexture0;\n" +
		"varying vec3 N, L;\n" +
		"varying vec3 vNormal;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" + 

		"void main() {\n" +
		"	float intensity = max(0.0, dot(normalize(N), normalize(L)));\n" +
		"	gl_FragColor = textureCube(uTexture0, vReflectDir);\n" +
		"	gl_FragColor += uAmbientColor * uAmbientIntensity;" +
		"	gl_FragColor.rgb *= intensity;\n" +
		"}\n";
	
	public CubeMapMaterial() {
		super(mVShader, mFShader);
		usesCubeMap = true;
	}
}
