precision mediump float;

const float pi = 3.14159265359;

varying vec2 vTextureCoord;

uniform sampler2D uTexture;
uniform vec2 uTexelSize;

uniform int uOrientation;
uniform int uBlurAmount;
uniform float uBlurScale;
uniform float uBlurStrength;
uniform float uScreenHeight;
uniform float uScreenWidth;

// Gaussian function : http://en.wikipedia.org/wiki/Gaussian_blur#Mechanics
float gaussian(float x, float variance) {
  return (1.0 / sqrt(2.0 * pi * variance)) * exp(-((x * x) / (2.0 * variance)));
}

void main() {
  // Calculate texel size.
  vec2 textureSize = vec2(uScreenWidth, uScreenHeight);
  vec2 texelSize = vec2(0.0, 0.0);
  // Prevent division by zero.
  if (uScreenWidth != 0.0) 
    texelSize.x = 1.0 / uScreenWidth;
  else
    texelSize.x = 0.0;
  if (uScreenHeight != 0.0) 
    texelSize.y = 1.0 / uScreenHeight;
  else
    texelSize.y = 0.0;
  float halfBlur = float(uBlurAmount) * 0.5;
  vec4 color = vec4(0.0);
  vec4 texColor = vec4(0.0);

  float std = halfBlur * 0.35;
  std *= std;
  float strength = 1.0 - uBlurStrength;

			// Limit number of blur steps to no more than 10.
  for (int i = 0; i < 10; ++i) {
    if (i >= uBlurAmount)
      break;
    float offset = float(i) - halfBlur;
    if (uOrientation == 0) {
	  // Horizontal gaussian blur
      texColor = texture2D(uTexture, vTextureCoord + vec2(offset * texelSize.x * uBlurScale, 0.0)) * gaussian(offset * strength, std);
    } else {
	  // Vertical gaussian blur
      texColor = texture2D(uTexture, vTextureCoord + vec2(0.0, offset * texelSize.y * uBlurScale)) * gaussian(offset * strength, std);
    }
    color += texColor;
  }

  gl_FragColor = clamp(color, 0.0, 1.0);	// Ensure the color values stay within 0.0 - 1.0 range
  gl_FragColor.w = 1.0; // Always opaque
}