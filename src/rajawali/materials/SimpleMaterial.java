package rajawali.materials;

public class SimpleMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"varying vec2 vTextureCoord;\n" +

		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D uTexture0;\n" +

		"void main() {\n" +
		"	gl_FragColor = texture2D(uTexture0, vTextureCoord);\n" +
		"}\n";
	
	public SimpleMaterial() {
		super(mVShader, mFShader);
	}
}
