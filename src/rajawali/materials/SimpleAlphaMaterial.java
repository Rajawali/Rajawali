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
			"   vec4 dTex = texture2D(uDiffuseTexture, vTextureCoord);\n" +
			"   vec4 aTex = texture2D(uAlphaTexture, vTextureCoord);\n" +
			"	gl_FragColor.rgb = dTex.rgb;\n" +
			"	gl_FragColor.a =  aTex.r;\n" +
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
