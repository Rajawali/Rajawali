precision highp float;

uniform float uOpacity;
uniform sampler2D uTexture;

varying vec2 vTextureCoord;

void main() {
	vec3 fragColor = texture2D(uTexture, vTextureCoord).rgb;
	float gray = dot(fragColor.rgb, vec3(0.299, 0.587, 0.114));
	gl_FragColor = uOpacity * vec4(gray * vec3(1.2, 1.0, 0.8), 1.0);
}