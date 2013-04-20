precision mediump float;

varying vec2 vTextureCoord;
varying vec3 vReflectDir;
uniform samplerCube uCubeMapTexture;
varying vec3 N;
varying vec4 V;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity;

%FOG_FRAGMENT_VARS%
%LIGHT_VARS%

void main() {
   float intensity = 0.0;
%LIGHT_CODE%
   gl_FragColor = textureCube(uCubeMapTexture, vReflectDir);
   gl_FragColor += uAmbientColor * uAmbientIntensity;
   gl_FragColor.rgb *= intensity;
M_FOG_FRAGMENT_COLOR
}