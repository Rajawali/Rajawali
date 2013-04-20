precision mediump float;

varying vec2 vTextureCoord;
varying vec3 N;
varying vec4 V;
varying vec4 vColor;
    
uniform sampler2D uDiffuseTexture;
uniform vec4 uAmbientColor;
uniform vec4 uAmbientIntensity; 
uniform vec4 uToonColor0, uToonColor1, uToonColor2, uToonColor3;

%FOG_FRAGMENT_VARS%
%LIGHT_VARS%

void main() {
   float intensity = 0.0;
   float dist = 0.0;
    float power = 0.0;
   float NdotL = 0.0;
   vec3 L = vec3(0.0);
   vec3 Kd = vec3(0.0);

%LIGHT_CODE%

#ifndef TEXTURED
   vec4 color = vColor;
#else
   vec4 color = vec4(1.0);
#endif
   if(intensity > .95) color = uToonColor0;
   else if(intensity > .5) color = uToonColor1;
   else if(intensity > .25) color = uToonColor2;
   else color = uToonColor3;
   color.rgb *= Kd;  
   color += uAmbientColor * uAmbientIntensity;
   gl_FragColor = color;
M_FOG_FRAGMENT_COLOR
}