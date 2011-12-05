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
		"	if(uUseTexture) gl_FragColor = texture2D(uTexture0, vTextureCoord);\n" +
		"	else gl_FragColor = vColor;\n" +
		"	gl_FragColor.rgb *= intensity;\n" +
		"}";
	
	protected int muLightPosHandle;
	protected int muNormalMatrixHandle;
	protected int muUseObjectTransformHandle;
	
	protected float[] mNormalMatrix;
	protected float[] mLightPos;
	
	public DiffuseMaterial() {
		super(mVShader, mFShader);
		mNormalMatrix = new float[9];
	}

	@Override
	public void setLight(ALight light) {
		super.setLight(light);

		DirectionalLight dirLight = (DirectionalLight)light;
		mLightPos = dirLight.getPosition();
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
		android.graphics.Matrix normalMatrix = new android.graphics.Matrix();
		android.graphics.Matrix mvMatrix = new android.graphics.Matrix();
		
		mvMatrix.setValues(new float[]{
				modelMatrix[0], modelMatrix[1], modelMatrix[2], 
				modelMatrix[4], modelMatrix[5], modelMatrix[6],
				modelMatrix[8], modelMatrix[9], modelMatrix[10]
		});
		
		normalMatrix.reset();
		mvMatrix.invert(normalMatrix);
		float[] values = new float[9];
		normalMatrix.getValues(values);
		
		normalMatrix.setValues(new float[] {
				values[0], values[3], values[6],
				values[1], values[4], values[7],
				values[2], values[5], values[8]
		});
		normalMatrix.getValues(mNormalMatrix);

	    GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false, mNormalMatrix, 0);
	}
}