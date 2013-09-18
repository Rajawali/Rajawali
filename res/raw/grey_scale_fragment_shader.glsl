precision mediump float;

uniform float uOpacity;
uniform sampler2D uTexture;

varying vec2 vTextureCoord;

void main() {
	vec4 srcColor = texture2D(uTexture, vTextureCoord);
	float average = (srcColor.r + srcColor.g + srcColor.b) / 3.0;
	gl_FragColor = uOpacity * vec4(average, average, average, srcColor.a);
}