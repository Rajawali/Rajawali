precision mediump float;

uniform mat4 uMVPMatrix;
uniform mat4 uMMatrix;
uniform mat3 uNMatrix;
uniform vec3 uLightPos;
uniform vec3 uCameraPosition;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
attribute vec3 aNormal;
varying vec2 vTextureCoord;
varying vec3 vReflectDir;
varying vec3 N;
varying vec4 V;

%FOG_VERTEX_VARS%
%LIGHT_VARS%

void main() {
   float dist = 0.0;
   gl_Position = uMVPMatrix * aPosition;
   V = uMMatrix * aPosition;
   vec3 eyeDir = V.xyz - uCameraPosition * -1.0;
   N = normalize(uNMatrix * aNormal);
   vReflectDir = reflect(eyeDir, N);
   vTextureCoord = aTextureCoord;
%LIGHT_CODE%
M_FOG_VERTEX_DENSITY
}