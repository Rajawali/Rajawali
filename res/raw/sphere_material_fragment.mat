precision mediump float;

uniform sampler2D uDiffuseTexture;
uniform sampler2D uSphereMapTexture;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity;
uniform float uSphereMapStrength;

varying vec2 vReflectTextureCoord;
varying vec2 vTextureCoord;
varying vec3 vReflectDir;
varying vec3 N;
varying vec4 V;
varying vec3 vNormal;
varying vec4 vColor;

%FOG_FRAGMENT_VARS%
%LIGHT_VARS%

void main() {
   float intensity = 0.0;
%LIGHT_CODE%
   vec4 reflColor = texture2D(uSphereMapTexture, vReflectTextureCoord);
#ifdef TEXTURED
   vec4 diffColor = texture2D(uDiffuseTexture, vTextureCoord);
#else
   vec4 diffColor = vColor;
#endif
   gl_FragColor = diffColor + reflColor * uSphereMapStrength;
   gl_FragColor += uAmbientColor * uAmbientIntensity;
   gl_FragColor.rgb *= intensity;
M_FOG_FRAGMENT_COLOR
}