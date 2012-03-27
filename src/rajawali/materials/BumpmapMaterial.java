package rajawali.materials;

public class BumpmapMaterial extends AAdvancedMaterial {
		protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N, L;\n" +
			"varying vec4 vColor;\n" +
			
			"uniform sampler2D uTexture0;\n" +
			"uniform sampler2D uNormalTexture;\n" +
			"uniform bool uUseTexture;\n" +
			"uniform float uLightPower;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" + 

			"void main() {\n" +
			"	vec3 normal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	normal.z = -normal.z;" +
			"	normal = normalize(normal + normalize(N));" +
		    "	float intensity = max(dot(normal, L), 0.0);" +
			" 	vec3 color = intensity * texture2D(uTexture0, vTextureCoord).rgb;" +
		    "	gl_FragColor = vec4(color, 1.0) + uAmbientColor * uAmbientIntensity;\n" + 
			"}";

	public BumpmapMaterial() {
		super(DiffuseMaterial.mVShader, mFShader);
	}
}
