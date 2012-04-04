package rajawali.filters;

import rajawali.materials.AMaterial;

public class BlurFilter extends AMaterial implements IPostProcessingFilter {
	protected static final String mVShader =
		"uniform mat4 uMVPMatrix;\n" +

		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +

		"varying vec2 vTextureCoord;\n" +

		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"}\n";
	
	protected static final String mFShader = 
			"precision mediump float;" +
			"varying vec2 vTextureCoord;" +
					
			"uniform sampler2D uFrameBufferTexture;" +

			"void main() {\n" +
			"	gl_FragColor = texture2D(uFrameBufferTexture, vTextureCoord);" +
			"}";
	
	public BlurFilter() {
		super(mVShader, mFShader);
	}
}
