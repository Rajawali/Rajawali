uniform mat4 uMVPMatrix;
uniform vec4 uPickingColor;

attribute vec4 aPosition;

varying vec4 vColor;	

void main() {
	gl_Position = uMVPMatrix * aPosition;
	vColor = uPickingColor;
}