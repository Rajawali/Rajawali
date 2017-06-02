precision highp float;

const float FXAA_SUBPIX_SHIFT = 1.0 / 8.0;

uniform mat4 uMVPMatrix;
uniform float rt_w;
uniform float rt_h;

attribute vec4 aPosition;
attribute vec2 aTextureCoord;

varying vec4 posPos;
varying vec2 vTextureCoord;

void main(void) {
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoord = aTextureCoord;
    vec2 rcpFrame = vec2(1.0 / rt_w, 1.0 / rt_h);
    posPos.xy = aTextureCoord.xy;
    posPos.zw = aTextureCoord.xy - (rcpFrame * (0.5 + FXAA_SUBPIX_SHIFT));
}