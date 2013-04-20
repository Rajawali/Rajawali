precision mediump float;
uniform mat4 uMVPMatrix;
uniform mat3 uNMatrix;
uniform mat4 uMMatrix;
uniform mat4 uVMatrix;

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec2 aTextureCoord;
attribute vec4 aColor;

varying vec2 vTextureCoord;
varying vec3 N;
varying vec4 V;
varying vec4 vColor;

%FOG_VERTEX_VARS%
%LIGHT_VARS%
%SKELETAL_ANIM_VERTEX_VARS% 

#ifdef VERTEX_ANIM
attribute vec4 aNextFramePosition;
attribute vec3 aNextFrameNormal;
uniform float uInterpolation;
#endif

void main() {

M_SKELETAL_ANIM_VERTEX_MATRIX

   vec4 position = aPosition;
   float dist = 0.0;
   vec3 normal = aNormal;
   #ifdef VERTEX_ANIM
   position = aPosition + uInterpolation * (aNextFramePosition - aPosition);
   normal = aNormal + uInterpolation * (aNextFrameNormal - aNormal);
   #endif

#ifdef SKELETAL_ANIM
   gl_Position = uMVPMatrix * TransformedMatrix * position;
#else
   gl_Position = uMVPMatrix * position;
#endif
   vTextureCoord = aTextureCoord;
#ifdef SKELETAL_ANIM
   N = normalize(uNMatrix * mat3(TransformedMatrix) * normal);
#else
   N = normalize(uNMatrix * normal);
#endif
   V = uMMatrix * position;
#ifndef TEXTURED
   vColor = aColor;
#endif

%LIGHT_CODE%

M_FOG_VERTEX_DENSITY
}