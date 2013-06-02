precision mediump float;

varying vec2 vTextureCoord;
varying vec3 N;
varying vec4 V;
varying vec4 vColor;

uniform sampler2D uDiffuseTexture;
uniform sampler2D uNormalTexture;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity;
uniform float uColorBlendFactor;

%FOG_FRAGMENT_VARS%
%LIGHT_VARS%

void main() {
   vec3 Kd = vec3(0.0);
   vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);
   bumpnormal = normalize(bumpnormal + N);
   float intensity = 0.0;
   vec3 L = vec3(0.0);
%LIGHT_CODE%
   vec4 diffuse = texture2D(uDiffuseTexture, vTextureCoord);
   #ifdef USE_COLOR
      diffuse *= (1.0 - uColorBlendFactor); 
      diffuse += vColor * uColorBlendFactor;
   #endif

   vec3 color = Kd * diffuse.rgb;
   gl_FragColor = vec4(color, 1.0) + uAmbientColor * uAmbientIntensity; 
M_FOG_FRAGMENT_COLOR
}