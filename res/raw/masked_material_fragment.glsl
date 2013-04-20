precision mediump float;

varying vec2 vTextureCoord;
uniform sampler2D uDiffuseTexture;
      
%FOG_FRAGMENT_VARS%  

void main() {
   vec4 tex = texture2D(uDiffuseTexture, vTextureCoord);
   if(tex.a < 0.5)
      discard;
   gl_FragColor = tex;
M_FOG_FRAGMENT_COLOR
}