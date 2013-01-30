package rajawali.materials;

public class SimpleAlphaMaterial extends SimpleMaterial {
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform sampler2D uAlphaTexture;\n" +
		"varying vec4 vColor;\n" +

		"void main() {\n" +
		"#ifdef TEXTURED\n" +
		"	gl_FragColor.rgb = texture2D(uDiffuseTexture, vTextureCoord).rgb;\n" +
		"	gl_FragColor.a = texture2D(uAlphaTexture, vTextureCoord).r;\n" +
		"#else\n" +
		"	gl_FragColor = vColor;\n" +
		"#endif\n" +
		"}\n";
	
	public SimpleAlphaMaterial() {
		super(SimpleMaterial.mVShader, mFShader);
		setShaders();
	}
	
	public SimpleAlphaMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
		setShaders();
	}
}
