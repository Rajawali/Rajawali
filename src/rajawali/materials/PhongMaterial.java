package rajawali.materials;

import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.wallpaper.Wallpaper;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;


public class PhongMaterial extends AMaterial {
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
		"varying vec3 N, L, E, H;\n" +
		"varying vec4 vColor;\n" +
		
		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		
		"	vec4 eyePosition = uMMatrix  * aPosition;\n" + 
		"	vec4 eyeLightPos = uUseObjectTransform ? uVMatrix * vec4(uLightPos, 1.0) : vec4(uLightPos, 1.0);\n" +
		"	N = normalize(uNMatrix * aNormal);\n" +
		"	L = normalize(eyeLightPos.xyz - eyePosition.xyz);\n" + 
		"	E = -normalize(eyePosition.xyz);\n" +
		"	H = normalize(L + E);\n" +
		"	vColor = aColor;\n" +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N, L, E, H;\n" +
		"varying vec4 vColor;\n" +
		
		"uniform vec4 uSpecularColor;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform sampler2D uTexture0;\n" +
		"uniform float uShininess;\n" +
		"uniform bool uUseTexture;\n" +

		"void main() {\n" +
		"	vec3 Normal = normalize(N);\n" +
		"	vec3 Light  = normalize(L);\n" +
		"	vec3 Eye    = normalize(E);\n" +
		"	vec3 Half   = normalize(H);\n" +
		
		"	float Kd = max(dot(Normal, Light), 0.0);\n" + 
		"	float Ks = pow(max(dot(Half, Normal), 0.0), uShininess);\n" + 
	    "	float Ka = 0.0;\n" +
	    "	vec4 diffuse  = uUseTexture ? Kd * texture2D(uTexture0, vTextureCoord) : Kd * vColor;\n" + 
	    "	vec4 specular = Ks * uSpecularColor;\n" + 
	    "	vec4 ambient  = Ka * uAmbientColor;\n" + 
	    "	gl_FragColor = ambient + diffuse + specular;\n" + 
		"}";
	
	protected int muLightPosHandle;
	protected int muNormalMatrixHandle;
	protected int muUseObjectTransformHandle;
	protected int muSpecularColorHandle;
	protected int muAmbientColorHandle;
	protected int muShininessHandle;
	
	protected float[] mNormalMatrix;
	protected float[] mLightPos;
	protected float[] mTmp, mTmp2;
	protected float[] mSpecularColor;
	protected float[] mAmbientColor;
	protected float mShininess;
	
	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();
	
	public PhongMaterial() {
		super(mVShader, mFShader);
		mNormalMatrix = new float[9];
		mSpecularColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		mAmbientColor = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
		mShininess = 96.0f;//
		mLightPos = new float[3];
	}
	
	public PhongMaterial(float[] specularColor, float[] ambientColor, float shininess) {
		this();
		mSpecularColor = specularColor;
		mAmbientColor = ambientColor;
		mShininess = shininess;
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
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muSpecularColorHandle, 1, mSpecularColor, 0);
		GLES20.glUniform4fv(muAmbientColorHandle, 1, mAmbientColor, 0);
		GLES20.glUniform1f(muShininessHandle, mShininess);
	}
	
	public void setSpecularColor(float[] color) {
		mSpecularColor = color;
	}
	
	public void setSpecularColor(float r, float g, float b, float a) {
		setSpecularColor(new float[] { r, g, b, a });
	}
	
	public void setSpecularColor(int color) {
		setSpecularColor(new float[] { Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color) });
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
	
	public void setShininess(float shininess) {
		mShininess = shininess;
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

		muSpecularColorHandle = GLES20.glGetUniformLocation(mProgram, "uSpecularColor");
		if(muSpecularColorHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uSpecularColor");
		}

		muAmbientColorHandle = GLES20.glGetUniformLocation(mProgram, "uAmbientColor");
		if(muAmbientColorHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uAmbientColor");
		}

		muShininessHandle = GLES20.glGetUniformLocation(mProgram, "uShininess");
		if(muShininessHandle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uShininess");
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