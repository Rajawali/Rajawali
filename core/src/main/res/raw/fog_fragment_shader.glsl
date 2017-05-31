precision mediump float;

uniform sampler2D uTexture;
uniform sampler2D uDepthTexture;
uniform vec3 uFogColor;
uniform float uFogNear;
uniform float uFogFar;

varying vec2 vTextureCoord;

void main() {
	vec4 depthPixel = texture2D(uDepthTexture, vTextureCoord);
	float fogDensity = clamp((depthPixel.r - uFogNear) / (uFogFar - uFogNear), 0.0, 1.0);

	vec4 srcColor = texture2D(uTexture, vTextureCoord);
	gl_FragColor.rgb = mix(srcColor.rgb, uFogColor, fogDensity);
	gl_FragColor.a = srcColor.a;
	//gl_FragColor.r = depthPixel.r;
	gl_FragColor.g = depthPixel.g;
	gl_FragColor.b = depthPixel.b;
}