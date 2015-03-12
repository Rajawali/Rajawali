precision highp float;

uniform float uOpacity;
uniform sampler2D uTexture;

varying vec2 vTextureCoord;

void main() {
	gl_FragColor = uOpacity * texture2D(uTexture, vTextureCoord);
}