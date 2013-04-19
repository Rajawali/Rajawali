precision mediump float;

uniform mat4 uMVPMatrix;
uniform mat4 uMMatrix;
uniform mat3 uNMatrix;
uniform vec3 uLightPos;
uniform vec3 uCameraPosition;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
attribute vec3 aNormal;
attribute vec4 aColor;
varying vec2 vTextureCoord;
varying vec2 vReflectTextureCoord;
varying vec3 vReflectDir;
varying vec3 vNormal;
varying vec3 N;
varying vec4 V;
varying vec4 vColor;

%FOG_VERTEX_VARS%
%LIGHT_VARS%

void main() {
   float dist = 0.0;
   gl_Position = uMVPMatrix * aPosition;
   V = uMMatrix * aPosition;
   vec3 eyeDir = normalize(V.xyz - uCameraPosition.xyz);
   N = normalize(uNMatrix * aNormal);
   vReflectDir = reflect(eyeDir, N);
   float m = 2.0 * sqrt(vReflectDir.x*vReflectDir.x + vReflectDir.y*vReflectDir.y + (vReflectDir.z+1.0)*(vReflectDir.z+1.0));
   vTextureCoord = aTextureCoord;
   vReflectTextureCoord.s = vReflectDir.x/m + 0.5;
   vReflectTextureCoord.t = vReflectDir.y/m + 0.5;
   vNormal = aNormal;
#ifndef TEXTURED
   vColor = aColor;
#endif
%LIGHT_CODE%
M_FOG_VERTEX_DENSITY
}