precision highp float;

uniform float uOpacity;
uniform sampler2D uTexture;
uniform sampler2D uBlendTexture;

varying vec2 vTextureCoord;

void main() {
	vec4 src = texture2D(uTexture, vTextureCoord);
	vec4 dst = texture2D(uBlendTexture, vTextureCoord);

	gl_FragColor = (src - dst) * uOpacity;
}