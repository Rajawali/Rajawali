package rajawali.materials;

import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import android.graphics.Color;
import android.opengl.GLES20;

public abstract class AAdvancedMaterial extends AMaterial {
	protected int muLightPosHandle;
	protected int muNormalMatrixHandle;
	protected int muAmbientColorHandle;
	protected int muAmbientIntensityHandle;

	protected float[] mNormalMatrix;
	protected float[] mLightPos;
	protected float[] mTmp, mTmp2;
	protected float[] mAmbientColor, mAmbientIntensity;

	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();

	public AAdvancedMaterial() {
		super();
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader) {
		this(vertexShader, fragmentShader, false);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
		
		mNormalMatrix = new float[9];
		mTmp = new float[9];
		mTmp2 = new float[9];
		mLightPos = new float[3];
		mAmbientColor = new float[] {.2f, .2f, .2f, 1};
		mAmbientIntensity = new float[] { .3f, .3f, .3f, 1 };
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
	
	public void setAmbientcolor(float[] color) {
		mAmbientColor = color;
	}
	
	public void setAmbientcolor(float r, float g, float b, float a) {
		setAmbientcolor(new float[] { r, g, b, a });
	}
	
	public void setAmbientcolor(int color) {
		setAmbientcolor(new float[] { Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color) });
	}
	
	public void setAmbientIntensity(float[] intensity) {
		mAmbientIntensity = intensity;
	}
	
	public void setAmbientIntensity(float r, float g, float b, float a) {
		setAmbientIntensity(new float[] { r, g, b, a });
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muAmbientColorHandle, 1, mAmbientColor, 0);
		GLES20.glUniform4fv(muAmbientIntensityHandle, 1, mAmbientIntensity, 0);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muLightPosHandle = getUniformLocation("uLightPos"); 
		muNormalMatrixHandle = getUniformLocation("uNMatrix");
		muAmbientColorHandle = getUniformLocation("uAmbientColor");
		muAmbientIntensityHandle = getUniformLocation("uAmbientIntensity");
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
