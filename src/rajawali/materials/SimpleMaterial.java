package rajawali.materials;


public class SimpleMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +

		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec4 vColor;\n" +		
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	vColor = aColor;\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec4 vColor;\n" +

		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform sampler2D uAlphaTexture;\n" +

		"void main() {\n" +
		"#ifdef TEXTURED\n" +
		"	gl_FragColor = texture2D(uDiffuseTexture, vTextureCoord);\n" +
		"#else\n" +
		"	gl_FragColor = vColor;\n" +
		"#endif\n" +

	    "#ifdef ALPHA\n" +
		"	float alpha = texture2D(uAlphaTexture, vTextureCoord).r;\n" +
	    "	gl_FragColor.a = alpha;\n" + 		
	    "#endif\n" +		
		"}\n";
	
	public SimpleMaterial() {
		this(mVShader, mFShader);
	}
	
	public SimpleMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		super.setShaders(vertexShader, fragmentShader);
	}
}
