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
		"uniform sampler2D uTexture0;\n" +
		"varying vec4 vColor;\n" +

		"uniform bool uUseTexture;\n" +

		"void main() {\n" +
		"	gl_FragColor = uUseTexture ? texture2D(uTexture0, vTextureCoord) : vColor;\n" +
		"}\n";
	
	public SimpleMaterial() {
		super(mVShader, mFShader, false);
	}
	
	public SimpleMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
	}
}
