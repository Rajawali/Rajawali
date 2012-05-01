package rajawali.materials;



public class DiffuseMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat4 uVMatrix;\n" +
		
		"attribute vec4 aPosition;\n" +
		"attribute vec3 aNormal;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_VERTEX_VARS +
		
		"\n#ifdef VERTEX_ANIM\n" +
		"attribute vec4 aNextFramePosition;\n" +
		"attribute vec3 aNextFrameNormal;\n" +
		"uniform float uInterpolation;\n" +
		"#endif\n\n" +
		
		"void main() {\n" +
		"	vec4 position = aPosition;\n" +
		"	vec3 normal = aNormal;\n" +
		"	#ifdef VERTEX_ANIM\n" +
		"	position = aPosition + uInterpolation * (aNextFramePosition - aPosition);\n" +
		"	normal = aNormal + uInterpolation * (aNextFrameNormal - aNormal);\n" +
		"	#endif\n" +
		"	gl_Position = uMVPMatrix * position;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	N = normalize(uNMatrix * normal);\n" +
		"	V = uMMatrix * position;\n" +
		"	vColor = aColor;\n" +
		M_FOG_VERTEX_DEPTH +
		"}";
		
	protected static final String mFShader =
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec4 vColor;\n" +
 
		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform bool uUseTexture;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" +
		
		M_FOG_FRAGMENT_VARS +		
		M_LIGHTS_VARS +
		
		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
		"  		vec4 lightPos = vec4(uLightPos[i], 1.0);\n" +
		"		vec3 L = normalize(vec3(lightPos - V));\n" +
		"		intensity += uLightPower[i] * dot(N, L);\n" +
		"	}\n" +
		"	if(uUseTexture==true) gl_FragColor = texture2D(uDiffuseTexture, vTextureCoord);\n" +
		"	else gl_FragColor = vColor;\n" +
		
		M_FOG_FRAGMENT_CALC +
		
		"	gl_FragColor = uAmbientIntensity * uAmbientColor + intensity * gl_FragColor;" +
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
	
	public DiffuseMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}
}