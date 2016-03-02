uniform mat4 uMVPMatrix;
uniform vec4 uSingleColor;

attribute vec4 aPosition;

varying vec4 vColor;

void main() {
	vec3 pos = vec3(aPosition.x, aPosition.y, aPosition.z);
	float distance = sqrt(pos.x * pos.x + pos.z * pos.z);
	float invDistance = 0.5 - distance;
	pos.y = cos(distance * 50.0) * (invDistance * invDistance) * 0.3;
	gl_Position = uMVPMatrix * vec4(pos, 1.0);
	vec4 color = vec4(invDistance * 2.0, invDistance * 2.0, 0.0, 1.0);
	vColor = color;
}