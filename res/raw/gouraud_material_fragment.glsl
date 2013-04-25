precision mediump float;

varying vec2 vTextureCoord;
varying float vSpecularIntensity;
varying float vDiffuseIntensity;
varying vec3 vLightColor;
varying vec4 vColor;

uniform sampler2D uDiffuseTexture;
uniform sampler2D uSpecularTexture;
uniform sampler2D uAlphaTexture;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity; 
uniform vec4 uSpecularColor;
uniform vec4 uSpecularIntensity;

#ifdef ALPHA_MASK
	uniform float uAlphaMaskingThreshold;
#endif

%FOG_FRAGMENT_VARS%
%LIGHT_VARS%

void main() {
#ifdef TEXTURED
   vec4 diffuse = texture2D(uDiffuseTexture, vTextureCoord);
   diffuse.rgb *= vLightColor * vDiffuseIntensity;
#else
   vec4 diffuse = vColor;
   diffuse.rgb *= vDiffuseIntensity;
#endif

#ifdef SPECULAR_MAP
   vec4 specular = uSpecularColor * vSpecularIntensity * uSpecularIntensity * texture2D(uSpecularTexture, vTextureCoord);
#else
   vec4 specular = uSpecularColor * vSpecularIntensity * uSpecularIntensity; 
#endif

   vec4 ambient = uAmbientIntensity * uAmbientColor;      
   vec4 color = diffuse + specular + ambient;
   float alpha = diffuse.a;

#ifdef ALPHA_MAP
   alpha = texture2D(uAlphaTexture, vTextureCoord).r;
   color.a = alpha;
#endif

#ifdef ALPHA_MASK
	if(alpha < uAlphaMaskingThreshold){
		discard;
	}
#endif

gl_FragColor = color;

M_FOG_FRAGMENT_COLOR
}