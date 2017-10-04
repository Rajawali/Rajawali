//  -- Taken from https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson3

precision mediump float;

varying vec2 vTextureCoord;

uniform float uRadius;
uniform float uSoftness;
uniform float uOpacity;

uniform sampler2D uTexture;

uniform vec2 uResolution;

void main() {
    vec4 texColor = texture2D(uTexture, vec2(vTextureCoord.x, vTextureCoord.y));

    vec2 position = (gl_FragCoord.xy / uResolution.xy) - vec2(0.5);

    //determine the vector length of the center position
    float len = length(position);

    // derive inner and outer radii from uRadius and uSoftness
    float inner = (uRadius > abs(uSoftness/2.)) ? uRadius - abs(uSoftness/2.) : 0.;
    float outer = uRadius + abs(uSoftness/2.);
    if(inner == outer) {
      inner -= 0.00001;
      outer += 0.00001;
    }

    //use smoothstep to create a smooth vignette
    float vignette = 1. - smoothstep(inner, outer, len);

    //apply the vignette with 60% opacity
    texColor.rgb = mix(texColor.rgb, texColor.rgb * vignette, uOpacity);

    gl_FragColor = vec4(texColor.rgb, 1.0);
}
