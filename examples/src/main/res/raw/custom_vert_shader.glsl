precision mediump float;

uniform mat4 uMVPMatrix;
uniform float uTime;

attribute vec4 aPosition;
attribute vec2 aTextureCoord;

varying vec4 vColor;
varying vec2 vTextureCoord;

void main() {
	vTextureCoord = aTextureCoord;

	vColor.a = 1.0;
	vColor.r = mod((1.0 + sin(uTime * 4.0)) / 2.0, 1.0);
	vColor.g = mod((1.0 + cos(uTime * 8.0)) / 2.0, 1.0);
	vColor.b = mod((1.0 + sin(uTime * 12.0)) / 2.0, 1.0);

	vec4 position;
	float time = sin(uTime);
	position.x = aPosition.x + sin(time * 10.0) * cos(aPosition.x * time) * cos(time * 20.0);
	position.y = aPosition.y - cos(time * 8.0) * sin((1.0 - aPosition.y) * time) * cos(time * 14.0);
	position.z = aPosition.z + sin(time * 6.0) * (1.0 - aPosition.z) * sin((1.0 - time) * 12.0);
	position.w = 1.0; 

	gl_Position = uMVPMatrix * position;
}