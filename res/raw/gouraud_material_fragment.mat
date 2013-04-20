precision mediump float;

varying vec2 vTextureCoord;
varying float vSpecularIntensity;
varying float vDiffuseIntensity;
varying vec3 vLightColor;
varying vec4 vColor;

uniform sampler2D uDiffuseTexture;
uniform sampler2D uSpecularTexture;
uniform sampler2D uAlphaTexture;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity; 
uniform vec4 uSpecularColor;
uniform vec4 uSpecularIntensity;

      %FOG_FRAGMENT_VARS%
%LIGHT_VARS%

void main() {
#ifdef TEXTURED
   vec4 diffuse = vec4(vLightColor, 1.0) * vDiffuseIntensity * texture2D(uDiffuseTexture, vTextureCoord);
#else
   vec4 diffuse = vDiffuseIntensity * vColor;
#endif

#ifdef SPECULAR_MAP
   vec4 specular = uSpecularColor * vSpecularIntensity * uSpecularIntensity * texture2D(uSpecularTexture, vTextureCoord);
#else
   vec4 specular = uSpecularColor * vSpecularIntensity * uSpecularIntensity; 
#endif

   vec4 ambient = uAmbientIntensity * uAmbientColor;      
   gl_FragColor = diffuse + specular + ambient;

#ifdef ALPHA_MAP
   float alpha = texture2D(uAlphaTexture, vTextureCoord).r;
   gl_FragColor.a = alpha;
#else
   gl_FragColor.a = diffuse.a;
#endif
      M_FOG_FRAGMENT_COLOR
}