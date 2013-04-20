precision mediump float;

uniform mat4 uMVPMatrix;
uniform mat3 uNMatrix;
uniform mat4 uMMatrix;
uniform mat4 uVMatrix;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity;

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec2 aTextureCoord;
attribute vec4 aColor;

varying vec2 vTextureCoord;
varying float vSpecularIntensity;
varying float vDiffuseIntensity;
varying vec3 vLightColor;
varying vec4 vColor;

%FOG_VERTEX_VARS%
%LIGHT_VARS%

#ifdef VERTEX_ANIM
attribute vec4 aNextFramePosition;
attribute vec3 aNextFrameNormal;
uniform float uInterpolation;
#endif

void main() {
   vec4 position = aPosition;
   vec3 normal = aNormal;
   #ifdef VERTEX_ANIM
   position = aPosition + uInterpolation * (aNextFramePosition - aPosition);
   normal = aNormal + uInterpolation * (aNextFrameNormal - aNormal);
   #endif

   gl_Position = uMVPMatrix * position;
   vTextureCoord = aTextureCoord;

   vec3 E = -vec3(uMMatrix * position);
   vec3 N = normalize(uNMatrix * normal);
   vec3 L = vec3(0.0);
   float dist = 0.0;
   float attenuation = 1.0;
   float NdotL = 0.0;

%LIGHT_CODE%
   vSpecularIntensity = clamp(vSpecularIntensity, 0.0, 1.0);
#ifndef TEXTURED
   vColor = vec4(vLightColor, 1.0) * aColor;
#endif
M_FOG_VERTEX_DENSITY
}