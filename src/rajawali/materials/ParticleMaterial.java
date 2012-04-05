package rajawali.materials;

import java.nio.FloatBuffer;

import rajawali.math.Number3D;
import android.opengl.GLES20;


public class ParticleMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform float uPointSize;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform vec3 uCamPos;\n" +
		"uniform vec3 uDistanceAtt;\n" +
		"uniform vec3 uFriction;\n" +
		"uniform float uTime;\n" +
		"uniform bool uMultiParticlesEnabled;\n" +
		
		"attribute vec4 aPosition;\n" +		
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec3 aVelocity;\n" +
		
		"varying vec2 vTextureCoord;\n" +

		"void main() {\n" +
		"	vec4 position = vec4(aPosition);\n" +
		"	if(uMultiParticlesEnabled){" +
		"		position.x += aVelocity.x * uFriction.x * uTime;\n" +
		"		position.y += aVelocity.y * uFriction.y * uTime;\n" +
		"		position.z += aVelocity.z * uFriction.z * uTime; }" +
		"	gl_Position = uMVPMatrix * position;\n" +
		"	float pdist = length(uCamPos - position.xyz);\n" +
		"	gl_PointSize = uPointSize / sqrt(uDistanceAtt.x + uDistanceAtt.y * pdist + uDistanceAtt.z * pdist * pdist);\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D uTexture0;\n" +

		"void main() {\n" +
		"	gl_FragColor = texture2D(uTexture0, gl_PointCoord);\n" +
		"}\n";
	
	protected float mPointSize = 10.0f;
	protected int muPointSizeHandle;
	protected int muCamPosHandle;
	protected int muDistanceAttHandle;
	protected int maVelocityHandle;
	protected int muFrictionHandle;
	protected int muTimeHandle;
	protected int muMultiParticlesEnabledHandle;
	
	protected float[] mDistanceAtt;
	protected boolean mMultiParticlesEnabled;
	protected float[] mFriction;
	protected float[] mCamPos;
	
	public ParticleMaterial() {
		super(mVShader, mFShader, false);
		mDistanceAtt = new float[] {1, 1, 1};
		mFriction = new float[3];
		mCamPos = new float[3];
	}
	
	public void setPointSize(float pointSize) {
		mPointSize = pointSize;
       	GLES20.glUniform1f(muPointSizeHandle, mPointSize);
	}
	
	public void setMultiParticlesEnabled(boolean enabled) {
		mMultiParticlesEnabled = enabled;
		GLES20.glUniform1i(muMultiParticlesEnabledHandle, mMultiParticlesEnabled == true ? GLES20.GL_TRUE : GLES20.GL_FALSE);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
	}
	
	public void setVelocity(FloatBuffer velocity) {
    	velocity.position(0);
    	GLES20.glVertexAttribPointer(maVelocityHandle, 3, GLES20.GL_FLOAT, false, 0, velocity);
    	GLES20.glEnableVertexAttribArray(maVelocityHandle);
    }
	
	public void setFriction(Number3D friction) {
		mFriction[0] = friction.x; mFriction[1] = friction.y; mFriction[2] = friction.z;
		if(mMultiParticlesEnabled == true) {
			GLES20.glUniform3fv(muFrictionHandle, 1, mFriction, 0);
		}
	}
	
	public void setTime(float time) {
		GLES20.glUniform1f(muTimeHandle, time);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muPointSizeHandle = GLES20.glGetUniformLocation(mProgram, "uPointSize");
		muCamPosHandle = GLES20.glGetUniformLocation(mProgram, "uCamPos");
		muDistanceAttHandle = GLES20.glGetUniformLocation(mProgram, "uDistanceAtt");
		
		maVelocityHandle = GLES20.glGetAttribLocation(mProgram, "aVelocity");
		muFrictionHandle = GLES20.glGetUniformLocation(mProgram, "uFriction");
		muTimeHandle = GLES20.glGetUniformLocation(mProgram, "uTime");
		muMultiParticlesEnabledHandle = GLES20.glGetUniformLocation(mProgram, "uMultiParticlesEnabled");
	}
	
	public void setCameraPosition(Number3D cameraPos) {
		mCamPos[0] = cameraPos.x; mCamPos[1] = cameraPos.y; mCamPos[2] = cameraPos.z;
		GLES20.glUniform3fv(muCamPosHandle, 1, mCamPos, 0);
		GLES20.glUniform3fv(muDistanceAttHandle, 1, mDistanceAtt, 0);
	}
}
