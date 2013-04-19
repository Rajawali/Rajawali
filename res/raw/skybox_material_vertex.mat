uniform mat4 uMVPMatrix;
					
attribute vec4 aPosition;
attribute vec3 aTextureCoord;
attribute vec4 aColor;
attribute vec3 aNormal;

varying vec3 vTextureCoord;
varying vec4 vColor;		

void main() {
	gl_Position = uMVPMatrix * aPosition;
	vTextureCoord = aTextureCoord;
	vColor = aColor;
}