precision mediump float;

varying vec2 vTextureCoord;
uniform sampler2D uDiffuseTexture;
		
#ifdef ANIMATED
uniform float uTileSize;
uniform float uNumTileRows;
#endif

void main() {
#ifdef ANIMATED
	vec2 realTexCoord = vTextureCoord + (gl_PointCoord / uNumTileRows);
	gl_FragColor = texture2D(uDiffuseTexture, realTexCoord);
#else
	gl_FragColor = texture2D(uDiffuseTexture, gl_PointCoord);
#endif
}