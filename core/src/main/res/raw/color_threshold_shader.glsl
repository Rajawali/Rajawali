precision highp float;

uniform float uOpacity;
uniform vec3 uLowerThreshold;
uniform vec3 uUpperThreshold;
uniform sampler2D uTexture;

varying vec2 vTextureCoord;

void main() {
	vec3 fragColor = texture2D(uTexture, vTextureCoord).rgb;
	fragColor = fragColor.r > uLowerThreshold.r && fragColor.g > uLowerThreshold.g 
		&& fragColor.b > uLowerThreshold.b ? fragColor : vec3(0.0);
	
	fragColor = fragColor.r < uUpperThreshold.r && fragColor.g < uUpperThreshold.g 
		&& fragColor.b < uUpperThreshold.b ? fragColor : vec3(0.0);
	
	gl_FragColor = uOpacity * vec4(fragColor, 1.0);
}