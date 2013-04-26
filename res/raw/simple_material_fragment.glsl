precision mediump float;

varying vec2 vTextureCoord;
varying vec4 vColor;

uniform sampler2D uDiffuseTexture;
uniform sampler2D uAlphaTexture;
#ifdef ALPHA_MASK
	uniform float uAlphaMaskingThreshold;
#endif

void main() {

#ifdef TEXTURED
	vec4 color = texture2D(uDiffuseTexture, vTextureCoord);
#else
	vec4 color = vColor;
#endif

#ifdef ALPHA_MAP
	color.a = texture2D(uAlphaTexture, vTextureCoord).r;
#endif

#ifdef ALPHA_MASK
	if(color.a < uAlphaMaskingThreshold){
		discard;	
	}
#endif

gl_FragColor = color;
}