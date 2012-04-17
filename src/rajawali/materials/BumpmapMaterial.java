package rajawali.materials;

public class BumpmapMaterial extends AAdvancedMaterial {
		protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N;\n" +
			"varying vec4 vColor;\n" +
			"varying vec4 V;\n" +
			
			"uniform sampler2D uDiffuseTexture;\n" +
			"uniform sampler2D uNormalTexture;\n" +
			"uniform bool uUseTexture;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" +
			
			M_LIGHTS_VARS +

			"void main() {\n" +
			"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + N);" +
			
		    "	float intensity = 0.0;" +
		    "	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
			"		vec4 lightPos = vec4(uLightPos[i], 1.0);\n" +
			"		vec3 L = normalize(vec3(lightPos - V));\n" +
			"		intensity += uLightPower[i] * max(dot(bumpnormal, L), 0.0);\n" +
			"	}\n" +
			" 	vec3 color = intensity * texture2D(uDiffuseTexture, vTextureCoord).rgb;" +
		    "	gl_FragColor = vec4(color, 1.0) + uAmbientColor * uAmbientIntensity;\n" + 
			"}";

	public BumpmapMaterial() {
		super(DiffuseMaterial.mVShader, mFShader);
	}
}
