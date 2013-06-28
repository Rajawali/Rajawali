precision mediump float;

varying vec2 vTextureCoord;
varying vec3 N;
varying vec4 V;
varying vec4 vColor;
 
uniform sampler2D uDiffuseTexture;
#ifdef ALPHA_MASK
	uniform sampler2D uAlphaTexture;
	uniform float uAlphaMaskingThreshold;
#endif
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity;
uniform float uColorBlendFactor
;

%FOG_FRAGMENT_VARS%
%LIGHT_VARS%

void main() {
   float intensity = 0.0;
   float power = 0.0;
   float NdotL = 0.0;
   float dist = 0.0;
   vec3 Kd = vec3(0.0);
   vec3 L = vec3(0.0);
#ifdef TEXTURED
   	vec4 color = texture2D(uDiffuseTexture, vTextureCoord);
   	#ifdef USE_COLOR
	color *= (1.0 - uColorBlendFactor); 
	color += vColor * uColorBlendFactor;
	#endif
	gl_FragColor = color;
#else
   gl_FragColor = vColor;
#endif

#ifdef ALPHA_MAP
	color.a = texture2D(uAlphaTexture, vTextureCoord).r;
#endif

#ifdef ALPHA_MASK
	if(color.a < uAlphaMaskingThreshold){
		discard;	
	}
#endif

%LIGHT_CODE%
   vec3 ambient = uAmbientIntensity.rgb * uAmbientColor.rgb;
   vec3 diffuse = Kd * gl_FragColor.rgb;
   gl_FragColor.rgb = ambient + diffuse;
M_FOG_FRAGMENT_COLOR
}