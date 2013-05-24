uniform mat4 uMVPMatrix;
					
attribute vec4 aPosition;
attribute vec3 aTextureCoord;
#ifdef USE_SINGLE_COLOR
uniform vec4 uSingleColor;
#endif
#ifdef USE_VERTEX_COLOR
attribute vec4 aColor;
#endif
attribute vec3 aNormal;

varying vec3 vTextureCoord;
varying vec4 vColor;	

void main() {
	gl_Position = uMVPMatrix * aPosition;
	vTextureCoord = aTextureCoord;
#ifdef USE_VERTEX_COLOR
	vColor = aColor;
#endif
#ifdef USE_SINGLE_COLOR
	vColor = uSingleColor;
#endif
}