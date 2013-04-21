precision mediump float;

varying vec2 vTextureCoord;
varying vec4 vColor;

uniform sampler2D uDiffuseTexture;
uniform sampler2D uAlphaTexture;

void main() {
#ifdef TEXTURED
	gl_FragColor = texture2D(uDiffuseTexture, vTextureCoord);
#else
	gl_FragColor = vColor;
#endif

#ifdef ALPHA_MAP
	float alpha = texture2D(uAlphaTexture, vTextureCoord).r;
	gl_FragColor.a = alpha;
#endif
}