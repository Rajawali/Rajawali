package rajawali.filters;

import rajawali.materials.AMaterial;

public class FogFilter extends AMaterial implements IPostProcessingFilter {
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
	
	/**
	 * From the OpenGL Super Bible
	 */
	protected static final String mFShader = 
			"precision mediump float;" +
			"varying vec2 vTextureCoord;" +
			
			"uniform sampler2D uFrameBufferTexture;" +
			"uniform sampler2D uDepthBufferTexture;" +

			"void main() {\n" +
			" 	vec3 fragColor = texture2D(uFrameBufferTexture, vTextureCoord).rgb;" +
			" 	vec3 depthColor = texture2D(uDepthBufferTexture, vTextureCoord).rgb;" +
			"	float gray = dot(fragColor.rgb, vec3(0.299, 0.587, 0.114));" +
			//"	gl_FragColor = vec4(gray * vec3(1.2, 1.0, 0.8), 1.0);" +
			"	gl_FragColor.rgb = vec3(depthColor.r);" +
			"}";
			
	public FogFilter() {
		super(mVShader, mFShader, false);
	}
	
	public boolean usesDepthBuffer() {
		return true;
	}
	
	public void useProgram() {
		super.useProgram();
	}
	
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
	}
}
