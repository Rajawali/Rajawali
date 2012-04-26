package rajawali.materials;

public class BumpmapPhongMaterial extends PhongMaterial {
	protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N;\n" +
			"varying vec3 L["+MAX_LIGHTS+"], H["+MAX_LIGHTS+"];\n" +
			"varying vec4 vColor;\n" +
			
			M_LIGHTS_VARS +
			
			"uniform vec4 uSpecularColor;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" + 
			"uniform sampler2D uDiffuseTexture;\n" +
			"uniform sampler2D uNormalTexture;\n" +
			"uniform float uShininess;\n" +
			"uniform bool uUseTexture;\n" +

			"void main() {\n" +
			"	float Kd = 0.0;" +
			"	float Ks = 0.0;" +
			
			"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + N);" +
			
			"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
			"		vec3 Half   = normalize(H[i]);\n" +
			"		vec3 Light  = normalize(L[i]);\n" +
			
			"		Kd += max(dot(bumpnormal * uLightPower[i], Light), 0.0)  * uLightPower[i];\n" + 
			"		Ks += pow(max(dot(Half, N), 0.0), uShininess);\n" + 
			"	}" +
		    "	vec4 diffuse  = uUseTexture ? Kd * texture2D(uDiffuseTexture, vTextureCoord) : Kd * vColor;\n" + 
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
