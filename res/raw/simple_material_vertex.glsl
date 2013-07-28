uniform mat4 uMVPMatrix;
#ifdef USE_SINGLE_COLOR
uniform vec4 uSingleColor;
#endif

attribute vec4 aPosition;
attribute vec2 aTextureCoord;
#ifdef USE_VERTEX_COLOR
attribute vec4 aColor;
#endif

varying vec2 vTextureCoord;
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