precision highp float;

#define FXAA_SPAN_MAX 8.0
#define FXAA_REDUCE_MUL (1.0 / 8.0)
#define FXAA_REDUCE_MIN (1.0 / 256.0)

uniform sampler2D uTexture;

uniform float rt_w;
uniform float rt_h;

varying vec4 posPos;
varying vec2 vTextureCoord;

#define FxaaTexOff(t, p, o, r) texture2D(t, p + (o * r))

/**
 *
 * @param posPos {@link vec4} Output of FxaaVertexShader interpolated across screen.
 * @param tex {@link sampler2D} The input texture.
 * @param rcpFrame {@link vec2} Constant {1.0/frameWidth, 1.0/frameHeight}.
 */
vec3 FxaaPixelShader(vec4 posPos, sampler2D tex, vec2 rcpFrame) {
/*---------------------------------------------------------*/
    vec3 rgbNW = texture2D(tex, posPos.zw).xyz;
    vec3 rgbNE = FxaaTexOff(tex, posPos.zw, vec2(1,0), rcpFrame.xy).xyz;
    vec3 rgbSW = FxaaTexOff(tex, posPos.zw, vec2(0,1), rcpFrame.xy).xyz;
    vec3 rgbSE = FxaaTexOff(tex, posPos.zw, vec2(1,1), rcpFrame.xy).xyz;
    vec3 rgbM  = texture2D(tex, posPos.xy).xyz;
/*---------------------------------------------------------*/
    vec3 luma = vec3(0.299, 0.587, 0.114);
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM  = dot(rgbM,  luma);
/*---------------------------------------------------------*/
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
/*---------------------------------------------------------*/
    vec2 dir;
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));
/*---------------------------------------------------------*/
    float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);
    float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(vec2( FXAA_SPAN_MAX,  FXAA_SPAN_MAX),
          max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
          dir * rcpDirMin)) * rcpFrame.xy;
/*--------------------------------------------------------*/
    vec3 rgbA = (1.0 / 2.0) * (
        texture2D(tex, posPos.xy + dir * (1.0 / 3.0 - 0.5)).xyz +
        texture2D(tex, posPos.xy + dir * (2.0 / 3.0 - 0.5)).xyz);
    vec3 rgbB = rgbA * (1.0 / 2.0) + (1.0 / 4.0) * (
        texture2D(tex, posPos.xy + dir * (0.0 / 3.0 - 0.5)).xyz +
        texture2D(tex, posPos.xy + dir * (3.0 / 3.0 - 0.5)).xyz);
    float lumaB = dot(rgbB, luma);
    if((lumaB < lumaMin) || (lumaB > lumaMax)) {
        return rgbA;
    }
    return rgbB;
}

vec4 PostFX(sampler2D tex, vec2 uv, float time) {
  vec4 c = vec4(0.0);
  vec2 rcpFrame = vec2(1.0 / rt_w, 1.0 / rt_h);
  c.rgb = FxaaPixelShader(posPos, tex, rcpFrame);
  //c.rgb = 1.0 - texture2D(tex, posPos.xy).rgb;
  c.a = 1.0;
  return c;
}

void main() {
  vec2 uv = vTextureCoord.st;
  gl_FragColor = PostFX(uTexture, uv, 0.0);
}