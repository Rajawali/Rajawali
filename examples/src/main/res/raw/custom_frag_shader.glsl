precision mediump float;

uniform float uColorInfluence;
uniform float uTime;
uniform float uInfluencemyTex;
uniform sampler2D myTex;

varying vec2 vTextureCoord;
varying vec4 vColor;

void main() {
	vec4 color = uColorInfluence * vColor;
	vec4 texColor = texture2D(myTex, vTextureCoord);
	texColor *= uInfluencemyTex;
	color += texColor;
	gl_FragColor = color;
}
