package rajawali.materials;

import java.util.Stack;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.lights.PointLight;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;

public abstract class AAdvancedMaterial extends AMaterial {
	protected static final int MAX_LIGHTS = RajawaliRenderer.getMaxLights(); 
	
	public static final String M_FOG_VERTEX_VARS =
			"#ifdef FOG_ENABLED\n" +
			"	uniform float uFogNear;\n" +
			"	uniform float uFogFar;\n" +
			"	uniform bool uFogEnabled;\n" +
			"	varying float vFogDensity;\n" +
			"#endif\n";
	public static final String M_FOG_VERTEX_DENSITY = 
			"#ifdef FOG_ENABLED\n" +
			"	vFogDensity = 0.0;\n" +
			"	if (uFogEnabled == true){\n" +
			"		vFogDensity = (gl_Position.z - uFogNear) / (uFogFar - uFogNear);\n" +
			"		vFogDensity = clamp(vFogDensity, 0.0, 1.0);\n" +
			"	}\n" +
			"#endif\n";
	public static final String M_FOG_FRAGMENT_VARS =
			"#ifdef FOG_ENABLED\n" +
			"	uniform vec3 uFogColor;\n" +
			"	varying float vFogDensity;\n" +
			"#endif\n";
	public static final String M_FOG_FRAGMENT_COLOR =
			"#ifdef FOG_ENABLED\n" +
			"	gl_FragColor.rgb = mix(gl_FragColor.rgb, uFogColor, vFogDensity);\n" +
			"#endif\n";

	/**
	 * @deprecated Replaced by {@link #M_FOG_VERTEX_DENSITY}
	 */
	@Deprecated
	public static final String M_FOG_VERTEX_DEPTH = M_FOG_VERTEX_DENSITY;

	/**
	 * @deprecated No longer needed (density calculation moved to
	 *             {@link #M_FOG_VERTEX_DENSITY} in the vertex shader)
	 */
	@Deprecated
	public static final String M_FOG_FRAGMENT_CALC = "";

	protected int muNormalMatrixHandle;
	protected int muAmbientColorHandle;
	protected int muAmbientIntensityHandle;
	protected int muFogColorHandle;
	protected int muFogNearHandle;
	protected int muFogFarHandle;
	protected int muFogEnabledHandle;
	protected int[] muLightColorHandles;
	protected int[] muLightPowerHandles;
	protected int[] muLightPositionHandles;
	protected int[] muLightDirectionHandles; 
	protected int[] muLightAttenuationHandles;
		
	protected float[] mNormalMatrix;
	protected float[] mTmp, mTmp2;
	protected float[] mAmbientColor, mAmbientIntensity;
	protected float[] mFogColor;
	protected float mFogNear, mFogFar;
	protected boolean mFogEnabled;
	
	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();

	public AAdvancedMaterial() {
		super();
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader) {
		this(vertexShader, fragmentShader, AMaterial.NONE);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		this(vertexShader, fragmentShader,
				isAnimated ? AMaterial.VERTEX_ANIMATION : AMaterial.NONE);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader, int parameters) {
		super(vertexShader, fragmentShader, parameters);
		mNormalMatrix = new float[9];
		mTmp = new float[9];
		mTmp2 = new float[9];
		mAmbientColor = new float[] {.2f, .2f, .2f, 1};
		mAmbientIntensity = new float[] { .3f, .3f, .3f, 1 };		

		if(RajawaliRenderer.isFogEnabled())
			mFogColor = new float[] { .8f, .8f, .8f };
	}
	
	@Override
	public void setLights(Stack<ALight> lights) {
		if(lights.size() != mLights.size() && lights.size() != 0) {
			super.setLights(lights);
			setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		} else if(lights.size() != 0) {
			boolean same = true;
			for(int i=0; i<lights.size(); ++i)
				if(lights.get(i) != mLights.get(i))
					same = false;
			if(!same)
			{
				super.setLights(lights);
				setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
			}
		} else {
			super.setLights(lights);
		}
	}
	
	@Override
	public void setLightParams() {
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			GLES20.glUniform3fv(muLightColorHandles[i], 1, light.getColor(), 0);
			GLES20.glUniform1f(muLightPowerHandles[i], light.getPower());
			GLES20.glUniform3fv(muLightPositionHandles[i], 1, light.getPositionArray(), 0);
			if(light.getLightType() == ALight.DIRECTIONAL_LIGHT)
				GLES20.glUniform3fv(muLightDirectionHandles[i], 1, ((DirectionalLight)light).getDirection(), 0);
			else
				GLES20.glUniform4fv(muLightAttenuationHandles[i], 1, ((PointLight)light).getAttenuation(), 0);
		}
	}
	
	public void setAmbientColor(float[] color) {
		mAmbientColor = color;
	}
	
	public void setAmbientColor(Number3D color) {
		setAmbientColor(color.x, color.y, color.z, 1);
	}
	
	public void setAmbientColor(float r, float g, float b, float a) {
		setAmbientColor(new float[] { r, g, b, a });
	}
	
	public void setAmbientColor(int color) {
		setAmbientColor(new float[] { Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f });
	}
	
	public void setAmbientIntensity(float[] intensity) {
		mAmbientIntensity = intensity;
	}
	
	public void setAmbientIntensity(float intensity) {
		mAmbientIntensity[0] = intensity;
		mAmbientIntensity[1] = intensity;
		mAmbientIntensity[2] = intensity;
		mAmbientIntensity[3] = 1;
	}
	
	public void setAmbientIntensity(float r, float g, float b, float a) {
		setAmbientIntensity(new float[] { r, g, b, a });
	}
	
	public void setFogColor(int color) {
		mFogColor[0] = Color.red(color) / 255f;
		mFogColor[1] = Color.green(color) / 255f;
		mFogColor[2] = Color.blue(color) / 255f;
	}
	
	public void setFogNear(float near) {
		mFogNear = near;
	}
	
	public void setFogFar(float far) {
		mFogFar = far;
	}
	
	public void setFogEnabled(boolean enabled) {
		mFogEnabled = enabled;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muAmbientColorHandle, 1, mAmbientColor, 0);
		GLES20.glUniform4fv(muAmbientIntensityHandle, 1, mAmbientIntensity, 0);
		if(mFogEnabled) {
			GLES20.glUniform3fv(muFogColorHandle, 1, mFogColor, 0);
			GLES20.glUniform1f(muFogNearHandle, mFogNear);
			GLES20.glUniform1f(muFogFarHandle, mFogFar);
			GLES20.glUniform1i(muFogEnabledHandle, mFogEnabled == true ? GLES20.GL_TRUE : GLES20.GL_FALSE);
		}
	}
	
	@Override
	public void setCamera(Camera camera) {
		super.setCamera(camera);
		if(camera.isFogEnabled()) {
			setFogColor(camera.getFogColor());
			setFogNear(camera.getFogNear());
			setFogFar(camera.getFogFar());
			setFogEnabled(true);
		} else {
			setFogEnabled(false);
		}
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		StringBuffer lightVars = new StringBuffer();
		int numLights = mLights.size();
		
		for(int i=0; i<numLights; ++i) {
			lightVars.append("uniform vec3 uLightColor").append(i).append(";\n");
			lightVars.append("uniform float uLightPower").append(i).append(";\n");
			lightVars.append("uniform int uLightType").append(i).append(";\n");
			lightVars.append("uniform vec3 uLightPosition").append(i).append(";\n");
			lightVars.append("uniform vec3 uLightDirection").append(i).append(";\n");
			lightVars.append("uniform vec4 uLightAttenuation").append(i).append(";\n");
			lightVars.append("varying float vAttenuation").append(i).append(";\n");
		}
		vertexShader = vertexShader.replace("%LIGHT_VARS%", lightVars.toString());
		fragmentShader = fragmentShader.replace("%LIGHT_VARS%", lightVars.toString());
		
		super.setShaders(vertexShader, fragmentShader);
		muNormalMatrixHandle = getUniformLocation("uNMatrix");
		muAmbientColorHandle = getUniformLocation("uAmbientColor");
		muAmbientIntensityHandle = getUniformLocation("uAmbientIntensity");
		
		muLightAttenuationHandles = new int[numLights];
		muLightColorHandles = new int[numLights];
		muLightDirectionHandles = new int[numLights];
		muLightPositionHandles = new int[numLights];
		muLightPowerHandles = new int[numLights];
		
		for(int i=0; i<mLights.size(); ++i) {
			muLightColorHandles[i] 			= getUniformLocation("uLightColor" + i);
			muLightPowerHandles[i] 			= getUniformLocation("uLightPower" + i);
			muLightPositionHandles[i] 		= getUniformLocation("uLightPosition" + i);
			muLightDirectionHandles[i] 		= getUniformLocation("uLightDirection" + i);
			muLightAttenuationHandles[i] 	= getUniformLocation("uLightAttenuation" + i);
		}
		
		if(RajawaliRenderer.isFogEnabled()) {
			muFogColorHandle = getUniformLocation("uFogColor");
			muFogNearHandle = getUniformLocation("uFogNear");
			muFogFarHandle = getUniformLocation("uFogFar");
			muFogEnabledHandle = getUniformLocation("uFogEnabled");
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
	
	public void destroy() {
		super.destroy();
		muLightAttenuationHandles = null;
		muLightColorHandles = null;
		muLightPowerHandles = null;
		muLightPositionHandles = null;
		muLightDirectionHandles = null;
		muLightAttenuationHandles = null;
		mNormalMatrix = null;
		mTmp = null;
		mTmp2 = null;
		mAmbientColor = null;
		mAmbientIntensity = null;
		mFogColor = null;
		mTmpNormalMatrix = null;
		mTmpMvMatrix = null;
	}
}
