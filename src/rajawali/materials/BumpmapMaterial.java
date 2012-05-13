package rajawali.materials;

public class BumpmapMaterial extends AAdvancedMaterial {
		protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N;\n" +
			"varying vec4 V;\n" +
			"varying vec4 vColor;\n" +
			
			"uniform sampler2D uDiffuseTexture;\n" +
			"uniform sampler2D uNormalTexture;\n" +
			"uniform bool uUseTexture;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" +
			
			M_FOG_FRAGMENT_VARS +
			M_LIGHTS_VARS +

			"void main() {\n" +
			"	vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + N);" +
			
		    "	float intensity = 0.0;" +
			"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
			"		vec3 L = vec3(0.0);" +
			"		float attenuation = 1.0;" +
			"		if(uLightType[i] == POINT_LIGHT) {" +
			"			L = normalize(uLightPosition[i] - V.xyz);\n" +
			"			float dist = distance(V.xyz, uLightPosition[i]);\n" +
			"			attenuation = 1.0 / (uLightAttenuation[i][1] + uLightAttenuation[i][2] * dist + uLightAttenuation[i][3] * dist * dist);\n" +
			"		} else {" +
			"			L = -normalize(uLightDirection[i]);" +
			"		}" +
			"		intensity += uLightPower[i] * max(dot(bumpnormal, L), 0.1) * attenuation;\n" +
			"	}\n" +
			" 	vec3 color = intensity * texture2D(uDiffuseTexture, vTextureCoord).rgb;" +
			M_FOG_FRAGMENT_CALC +
		    "	gl_FragColor = vec4(color, 1.0) + uAmbientColor * uAmbientIntensity;\n" + 
		    M_FOG_FRAGMENT_COLOR +
			"}";

	public BumpmapMaterial() {
		super(DiffuseMaterial.mVShader, mFShader);
	}
}
