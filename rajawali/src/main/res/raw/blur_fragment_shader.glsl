// -- Taken from https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson5

precision mediump float;

varying vec2 vTextureCoord;

uniform sampler2D uTexture;
uniform vec2 uDirection;
uniform float uRadius;
uniform float uResolution;

void main() {
    vec4 color = vec4(0.0);
    float blur = uRadius / uResolution; 

    color += texture2D(uTexture, vec2(vTextureCoord.x - 4.0*blur*uDirection.x, vTextureCoord.y - 4.0*blur*uDirection.y)) * 0.0162162162;
    color += texture2D(uTexture, vec2(vTextureCoord.x - 3.0*blur*uDirection.x, vTextureCoord.y - 3.0*blur*uDirection.y)) * 0.0540540541;
    color += texture2D(uTexture, vec2(vTextureCoord.x - 2.0*blur*uDirection.x, vTextureCoord.y - 2.0*blur*uDirection.y)) * 0.1216216216;
    color += texture2D(uTexture, vec2(vTextureCoord.x - 1.0*blur*uDirection.x, vTextureCoord.y - 1.0*blur*uDirection.y)) * 0.1945945946;

    color += texture2D(uTexture, vec2(vTextureCoord.x, vTextureCoord.y)) * 0.2270270270;

    color += texture2D(uTexture, vec2(vTextureCoord.x + 1.0*blur*uDirection.x, vTextureCoord.y + 1.0*blur*uDirection.y)) * 0.1945945946;
    color += texture2D(uTexture, vec2(vTextureCoord.x + 2.0*blur*uDirection.x, vTextureCoord.y + 2.0*blur*uDirection.y)) * 0.1216216216;
    color += texture2D(uTexture, vec2(vTextureCoord.x + 3.0*blur*uDirection.x, vTextureCoord.y + 3.0*blur*uDirection.y)) * 0.0540540541;
    color += texture2D(uTexture, vec2(vTextureCoord.x + 4.0*blur*uDirection.x, vTextureCoord.y + 4.0*blur*uDirection.y)) * 0.0162162162;

    gl_FragColor = vec4(color.rgb, 1.0);
}