#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES uDiffuseTexture;
varying vec4 vColor;

void main() {
#ifdef TEXTURED
	gl_FragColor = texture2D(uDiffuseTexture, vTextureCoord);
#else
	gl_FragColor = vColor;
#endif
}