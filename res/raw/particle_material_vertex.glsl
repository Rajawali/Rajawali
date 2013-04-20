precision mediump float;
uniform mat4 uMVPMatrix;
uniform float uPointSize;
uniform mat4 uMMatrix;
uniform vec3 uCamPos;
uniform vec3 uDistanceAtt;
uniform vec3 uFriction;
uniform float uTime;
uniform bool uMultiParticlesEnabled;
		
#ifdef ANIMATED
uniform float uCurrentFrame;
uniform float uTileSize;
uniform float uNumTileRows;
attribute float aAnimOffset;
#endif
		
attribute vec4 aPosition;		
attribute vec2 aTextureCoord;
attribute vec3 aVelocity;
		
varying vec2 vTextureCoord;

void main() {
	vec4 position = vec4(aPosition);
	if(uMultiParticlesEnabled){
		position.x += aVelocity.x * uFriction.x * uTime;
		position.y += aVelocity.y * uFriction.y * uTime;
		position.z += aVelocity.z * uFriction.z * uTime; 
	}
	
	gl_Position = uMVPMatrix * position;
	vec3 cp = vec3(uCamPos);
	float pdist = length(cp - position.xyz);
	gl_PointSize = uPointSize / sqrt(uDistanceAtt.x + uDistanceAtt.y * pdist + uDistanceAtt.z * pdist * pdist);
#ifdef ANIMATED
	vTextureCoord.s = mod(uCurrentFrame + aAnimOffset, uNumTileRows) * uTileSize;
	vTextureCoord.t = uTileSize * floor((uCurrentFrame + aAnimOffset ) / uNumTileRows);
#else
	vTextureCoord = aTextureCoord;
#endif
}