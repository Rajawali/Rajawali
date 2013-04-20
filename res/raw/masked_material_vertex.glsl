uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
attribute vec2 aTextureCoord;

varying vec2 vTextureCoord;

%FOG_VERTEX_VARS%

void main() {
   gl_Position = uMVPMatrix * aPosition;
   vTextureCoord = aTextureCoord;
M_FOG_VERTEX_DENSITY
}