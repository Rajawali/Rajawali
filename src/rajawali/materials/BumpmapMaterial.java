package rajawali.materials;

import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;

public class BumpmapMaterial extends AMaterial {
	protected static final String mVShader = 
			"uniform mat4 uMVPMatrix;\n" +
			"uniform mat3 uNMatrix;\n" +
			"uniform mat4 uMMatrix;\n" +
			"uniform mat4 uVMatrix;\n" +
			"uniform vec3 uLightPos;\n" +
			
			"attribute vec4 aPosition;\n" +
			"attribute vec3 aNormal;\n" +
			"attribute vec2 aTextureCoord;\n" +
			
			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N, L;\n" +
			
			"void main() {\n" +
			"	gl_Position = uMVPMatrix * aPosition;\n" +
			"	vTextureCoord = aTextureCoord;\n" +
			"	N = uNMatrix * aNormal;\n" +
			"	vec4 V = uMMatrix * aPosition;\n" +
			"   vec4 lightPos = vec4(uLightPos, 1.0);\n" +
			"	L = normalize(vec3(lightPos - V));\n" +
			"}";
			
		protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N, L;\n" +
			
			"uniform sampler2D uTexture0;\n" +
			"uniform sampler2D normalTexture;\n" +

			"void main() {\n" +
			"	vec3 normal = normalize(texture2D(normalTexture, vTextureCoord).rgb * 2.0 - 1.0);" +
			"	normal.z = -normal.z;" +
			"	normal = normalize(normal + normalize(N));" +
		    "	float diffuse = max(dot(normal, L), 0.0);" +
			" 	vec3 color = diffuse * texture2D(uTexture0, vTextureCoord).rgb;" +
		    "	gl_FragColor = vec4(color, 1.0);\n" + 
			"}";
	
	protected int muLightPosHandle;
	protected int muNormalMatrixHandle;

	protected float[] mNormalMatrix;
	protected float[] mLightPos;
	protected float[] mTmp, mTmp2;
	
	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();

	public BumpmapMaterial() {
		super(mVShader, mFShader);
		mNormalMatrix = new float[9];
		mTmp = new float[9];
		mTmp2 = new float[9];
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
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
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
	}
	
	@Override
	public void setModelMatrix(float[] modelMatrix) {
		super.setModelMatrix(modelMatrix);
		
		mTmp2[0] = modelMatrix[0]; mTmp2[1] = modelMatrix[1]; mTmp2[2] = modelMatrix[2]; 
		mTmp2[3] = modelMatrix[4]; mTmp2[4] = modelMatrix[5]; mTmp2[5] = modelMatrix[6];
		mTmp2[6] = modelMatrix[8]; mTmp2[7] = modelMatrix[9]; mTmp2[8] = modelMatrix[10];
		
		mTmpMvMatrix.setValues(mTmp2);
		
		mTmpNormalMatrix.reset();
		mTmpMvMatrix.invert(mTmpNormalMatrix);

		mTmpNormalMatrix.getValues(mTmp);
		mTmp2[0] = mTmp[0]; mTmp2[1] = mTmp[3]; mTmp2[2] = mTmp[6]; 
		mTmp2[3] = mTmp[1]; mTmp2[4] = mTmp[4]; mTmp2[5] = mTmp[7];
		mTmp2[6] = mTmp[2]; mTmp2[7] = mTmp[5]; mTmp2[8] = mTmp[8];
		mTmpNormalMatrix.setValues(mTmp2);
		mTmpNormalMatrix.getValues(mNormalMatrix);

	    GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false, mNormalMatrix, 0);
	}
}
