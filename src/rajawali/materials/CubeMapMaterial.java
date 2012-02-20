package rajawali.materials;

import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;


public class CubeMapMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform vec3 uLightPos;\n" +
		"uniform vec3 uCameraPosition;\n" +
		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec3 aNormal;\n" +
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 N, L;\n" +
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vec4 transfPos = uMMatrix * aPosition;\n" +
		"	vec3 eyeDir = normalize(transfPos.xyz - uCameraPosition.xyz);\n" +
		"	N = uNMatrix * aNormal;\n" +
		"	vReflectDir = reflect(eyeDir, N);\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	L = uLightPos.xyz - aPosition.xyz;\n" +
		"	vNormal = aNormal;\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"uniform samplerCube uTexture0;\n" +
		"varying vec3 N, L;\n" +
		"varying vec3 vNormal;\n" +

		"void main() {\n" +
		"	float intensity = max(0.0, dot(normalize(N), normalize(L)));\n" +
		"	gl_FragColor = textureCube(uTexture0, vReflectDir);\n" +
		"	gl_FragColor.rgb *= intensity;\n" +
		//"	gl_FragColor = vec4(normalize(vNormal), 1.0);\n" +
		"}\n";
	
	protected int muLightPosHandle;
	protected int muNormalMatrixHandle;

	protected float[] mLightPos;
	protected float[] mNormalMatrix;
	
	public CubeMapMaterial() {
		super(mVShader, mFShader);
		usesCubeMap = true;
		mNormalMatrix = new float[9];
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
