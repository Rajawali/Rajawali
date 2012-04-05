package rajawali.materials;

public class BumpmapPhongMaterial extends PhongMaterial {
	protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N, L, E, H;\n" +
			"varying vec4 vColor;\n" +
			
			"uniform vec4 uSpecularColor;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" + 
			"uniform sampler2D uTexture0;\n" +
			"uniform sampler2D uNormalTexture;\n" +
			"uniform float uShininess;\n" +
			"uniform bool uUseTexture;\n" +

			"void main() {\n" +
			"	vec3 Normal = normalize(N);\n" +
			"	vec3 Light  = normalize(L);\n" +
			"	vec3 Eye    = normalize(E);\n" +
			"	vec3 Half   = normalize(H);\n" +
			
			"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + Normal);" +
			
			"	float Kd = max(dot(bumpnormal, Light), 0.0);\n" + 
			"	float Ks = pow(max(dot(Half, bumpnormal), 0.0), uShininess);\n" + 
		    "	vec4 diffuse  = uUseTexture ? Kd * texture2D(uTexture0, vTextureCoord) : Kd * vColor;\n" + 
		    "	vec4 specular = Ks * uSpecularColor;\n" + 
		    "	vec4 ambient  = uAmbientIntensity * uAmbientColor;\n" + 
		    "	gl_FragColor = ambient + diffuse + specular;\n" + 
			"}";
	
	public BumpmapPhongMaterial() {
		this(false);
	}
	
	public BumpmapPhongMaterial(boolean isAnimated) {
		super(PhongMaterial.mVShader, mFShader, isAnimated);
	}
}
