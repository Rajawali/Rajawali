package rajawali.materials;

import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;


public class DiffuseMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat4 uVMatrix;\n" +
		"uniform vec3 uLightPos;\n" +
		"uniform bool uUseObjectTransform;\n" +
		"attribute vec4 aPosition;\n" +
		"attribute vec3 aNormal;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N, L;\n" +
		"varying vec4 vColor;\n" +
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	N = uNMatrix * aNormal;\n" +
		"	vec4 V = uMMatrix * aPosition;\n" +
		"   vec4 lightPos = uUseObjectTransform ? uVMatrix * vec4(uLightPos, 1.0) : vec4(uLightPos, 1.0);\n" +
		"	L = lightPos.xyz - V.xyz;\n" +
		"	vColor = aColor;\n" +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N, L;\n" +
		"varying vec4 vColor;\n" +
 
		"uniform sampler2D uTexture0;\n" +
		"uniform bool uUseTexture;\n" +

		"void main() {\n" +
		"	float intensity = max(0.0, dot(normalize(N), normalize(L)));\n" +
		"	if(uUseTexture==true) gl_FragColor = texture2D(uTexture0, vTextureCoord);\n" +
		"	else gl_FragColor = vColor;\n" +
		"	gl_FragColor.rgb *= intensity;\n" +
		"}";
	
	protected int muLightPosHandle;
	protected int muNormalMatrixHandle;
	protected int muUseObjectTransformHandle;
	
	protected float[] mNormalMatrix;
	protected float[] mLightPos;
	protected float[] mTmp;
	
	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();

	
	public DiffuseMaterial() {
		super(mVShader, mFShader);
		mNormalMatrix = new float[9];
		mTmp = new float[9];
		mLightPos = new float[3];
	}

	@Override
	public void setLight(ALight light) {
		super.setLight(light);

		DirectionalLight dirLight = (DirectionalLight)light;
		mLightPos[0] = dirLight.getPosition().x;
		mLightPos[1] = dirLight.getPosition().y;
		mLightPos[2] = dirLight.getPosition().z;
		GLES20.glUniform3fv(muLightPosHandle, 1, mLightPos, 0);
		GLES20.glUniform1i(muUseObjectTransformHandle, light.shouldUseObjectTransform() ? 1 : 0);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muLightPosHandle = GLES20.glGetUniformLocation(mProgram, "uLightPos");
		if(muLightPosHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uLightPos");
		}
		muNormalMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uNMatrix");
		if(muNormalMatrixHandle == -1) {
			throw new RuntimeException("Could not get uniform location for uNMatrix");
		}
		muUseObjectTransformHandle = GLES20.glGetUniformLocation(mProgram, "uUseObjectTransform");
		if(muUseObjectTransformHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uUseObjectTransform");
		}
	}
	
	@Override
	public void setModelMatrix(float[] modelMatrix) {
		super.setModelMatrix(modelMatrix);
		
		mTmpMvMatrix.setValues(
				modelMatrix
		);
		
		mTmpMvMatrix.invert(mTmpNormalMatrix);
		mTmpNormalMatrix.getValues(mNormalMatrix);

	    GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false, mNormalMatrix, 0);
	}
}