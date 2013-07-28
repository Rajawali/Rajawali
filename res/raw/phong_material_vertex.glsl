precision mediump float;
precision mediump int;
uniform mat4 uMVPMatrix;
uniform mat3 uNMatrix;
uniform mat4 uMMatrix;
uniform mat4 uVMatrix;
#ifdef USE_SINGLE_COLOR
uniform vec4 uSingleColor;
#endif

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec2 aTextureCoord;
#ifdef USE_VERTEX_COLOR
attribute vec4 aColor;
#endif

varying vec2 vTextureCoord;
varying vec3 vNormal;
varying vec3 vEyeVec;
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

   float dist = 0.0;
   vec4 position = aPosition;
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

   vEyeVec = -vec3(uMMatrix  * position);

#ifdef SKELETAL_ANIM
   vNormal = normalize(uNMatrix * mat3(TransformedMatrix) * normal);
#else
   vNormal = normalize(uNMatrix * normal);
#endif

%LIGHT_CODE%

#ifdef USE_VERTEX_COLOR
	vColor = aColor;
#endif

#ifdef USE_SINGLE_COLOR
	vColor = uSingleColor;
#endif
   
M_FOG_VERTEX_DENSITY
}