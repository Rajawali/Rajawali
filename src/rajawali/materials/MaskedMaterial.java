package rajawali.materials;


public class MaskedMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +

		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +

		"varying vec2 vTextureCoord;\n" +

		M_FOG_VERTEX_VARS +
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		M_FOG_VERTEX_DENSITY +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D uDiffuseTexture;\n" +
		
		M_FOG_FRAGMENT_VARS +		

		"void main() {\n" +
		"	vec4 tex = texture2D(uDiffuseTexture, vTextureCoord);\n" +
		"	if(tex.a < 0.5)\n" +
		"		discard;\n" +
		"	gl_FragColor = tex;\n" +
		M_FOG_FRAGMENT_COLOR +
		"}\n";
	
	public MaskedMaterial() {
		super(mVShader, mFShader, false);
	}
	
	public MaskedMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
	}
}