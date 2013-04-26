precision mediump float;

varying vec2 vTextureCoord;
uniform sampler2D uDiffuseTexture;
uniform sampler2D uAlphaTexture;
varying vec4 vColor;

void main() {
#ifdef TEXTURED
	gl_FragColor.rgb = texture2D(uDiffuseTexture, vTextureCoord).rgb;
	gl_FragColor.a = texture2D(uAlphaTexture, vTextureCoord).r;
#else
	gl_FragColor = vColor;
#endif
}
