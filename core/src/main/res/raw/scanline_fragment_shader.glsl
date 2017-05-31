precision mediump float;

varying vec2 vTextureCoord;

uniform float uRadius;
uniform float uOpacity;

uniform sampler2D uTexture;

uniform vec2 uResolution;

void main() {
    vec4 texColor = texture2D(uTexture, vec2(vTextureCoord.x, vTextureCoord.y));

    float screenV = vTextureCoord.y * uResolution.y / uRadius;

    float scanLine = abs(mod(screenV, 2.0) - 1.);

    //apply the scanline
    texColor.rgb = mix(texColor.rgb, vec3(0,0,0), uOpacity * scanLine);

    gl_FragColor = vec4(texColor.rgb, 1.0);
}
