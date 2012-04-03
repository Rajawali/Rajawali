package rajawali.materials;



public class DiffuseMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat4 uVMatrix;\n" +
		"uniform vec3 uLightPos;\n" +
		
		"attribute vec4 aPosition;\n" +
		"attribute vec3 aNormal;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N, L;\n" +
		"varying vec4 vColor;\n" +
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	N = normalize(uNMatrix * aNormal);\n" +
		"	vec4 V = uMMatrix * aPosition;\n" +
		"   vec4 lightPos = vec4(uLightPos, 1.0);\n" +
		"	L = normalize(vec3(lightPos - V));\n" +
		"	vColor = aColor;\n" +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N, L;\n" +
		"varying vec4 vColor;\n" +
 
		"uniform sampler2D uTexture0;\n" +
		"uniform bool uUseTexture;\n" +
		"uniform float uLightPower;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" + 

		"void main() {\n" +
		"	float intensity = uLightPower * clamp(dot(N, L), 0.0, 1.0);\n" +
		"	if(uUseTexture==true) gl_FragColor = texture2D(uTexture0, vTextureCoord);\n" +
		"	else gl_FragColor = vColor;\n" +
		"	gl_FragColor = uAmbientIntensity * uAmbientColor + intensity * gl_FragColor;" +
		"}";
	
	public DiffuseMaterial() {
		super(mVShader, mFShader);
	}
	
	public DiffuseMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}
}