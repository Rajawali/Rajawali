precision mediump float;
precision mediump int;

varying vec2 vTextureCoord;
varying vec3 vNormal;
varying vec3 vEyeVec;
varying vec4 vColor;

%FOG_FRAGMENT_VARS%
%LIGHT_VARS%

uniform vec4 uSpecularColor;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity;
uniform sampler2D uDiffuseTexture;
uniform sampler2D uNormalTexture;
uniform sampler2D uSpecularTexture;
uniform float uShininess;

void main() {
   float Kd = 0.0;
   float Ks = 0.0;
   float NdotL = 0.0;
   vec3 N = normalize(vNormal);
   vec3 E = normalize(vEyeVec);
   vec3 L = vec3(0.0);
   vec3 bumpnormal = normalize(texture2D(uNormalTexture, vTextureCoord).rgb * 2.0 - 1.0);
   bumpnormal.z = -bumpnormal.z;
   bumpnormal = normalize(bumpnormal + N);

%LIGHT_CODE%

#ifdef TEXTURED
   vec4 diffuse  = Kd * texture2D(uDiffuseTexture, vTextureCoord);
#else
   vec4 diffuse  = Kd * vColor;
#endif
   vec4 specular = Ks * uSpecularColor * texture2D(uSpecularTexture, vTextureCoord);
   vec4 ambient  = uAmbientIntensity * uAmbientColor;
   gl_FragColor  = ambient + diffuse + specular;
M_FOG_FRAGMENT_COLOR
}