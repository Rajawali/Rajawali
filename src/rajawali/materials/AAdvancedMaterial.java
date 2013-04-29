package rajawali.materials;

import java.util.List;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.lights.PointLight;
import rajawali.lights.SpotLight;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;

public abstract class AAdvancedMaterial extends AMaterial {
	protected static final int MAX_LIGHTS = RajawaliRenderer.getMaxLights(); 
	
	public static final String M_FOG_VERTEX_VARS =
			"\n#ifdef FOG_ENABLED\n" +
			"	uniform float uFogNear;\n" +
			"	uniform float uFogFar;\n" +
			"	uniform bool uFogEnabled;\n" +
			"	varying float vFogDensity;\n" +
			"#endif\n";
	public static final String M_FOG_VERTEX_DENSITY = 
			"\n#ifdef FOG_ENABLED\n" +
			"	vFogDensity = 0.0;\n" +
			"	if (uFogEnabled == true){\n" +
			"		vFogDensity = (gl_Position.z - uFogNear) / (uFogFar - uFogNear);\n" +
			"		vFogDensity = clamp(vFogDensity, 0.0, 1.0);\n" +
			"	}\n" +
			"#endif\n";
	public static final String M_FOG_FRAGMENT_VARS =
			"\n#ifdef FOG_ENABLED\n" +
			"	uniform vec3 uFogColor;\n" +
			"	varying float vFogDensity;\n" +
			"#endif\n";
	public static final String M_FOG_FRAGMENT_COLOR =
			"\n#ifdef FOG_ENABLED\n" +
			"	gl_FragColor.rgb = mix(gl_FragColor.rgb, uFogColor, vFogDensity);\n" +
			"#endif\n";
	public static final String M_SKELETAL_ANIM_VERTEX_VARS = 
			"\n#ifdef SKELETAL_ANIM\n" + 
			"uniform mat4 uBoneMatrix[%NUM_JOINTS%];\n" +
			"attribute vec4 vBoneIndex1;\n" +
			"attribute vec4 vBoneWeight1;\n" +
			"	#ifdef VERTEX_WEIGHT_8\n" +
			"		attribute vec4 vBoneIndex2;\n" +
			"		attribute vec4 vBoneWeight2;\n" +
			"	#endif\n" + 
			"#endif\n";
	public static final String M_SKELETAL_ANIM_VERTEX_MATRIX =
			"\n#ifdef SKELETAL_ANIM\n" +
			"mat4 TransformedMatrix = (vBoneWeight1.x * uBoneMatrix[int(vBoneIndex1.x)]) + \n" +
			"	(vBoneWeight1.y * uBoneMatrix[int(vBoneIndex1.y)]) + \n" +
			"	(vBoneWeight1.z * uBoneMatrix[int(vBoneIndex1.z)]) + \n" +
			"	(vBoneWeight1.w * uBoneMatrix[int(vBoneIndex1.w)]);\n" +
			
			"	#ifdef VERTEX_WEIGHT_8\n" +
			"		TransformedMatrix = TransformedMatrix + (vBoneWeight2.x * uBoneMatrix[int(vBoneIndex2.x)]) + \n" +
			"		(vBoneWeight2.y * uBoneMatrix[int(vBoneIndex2.y)]) + \n" +
			"		(vBoneWeight2.z * uBoneMatrix[int(vBoneIndex2.z)]) + \n" +
			 "		(vBoneWeight2.w * uBoneMatrix[int(vBoneIndex2.w)]);\n"	+
			"	#endif\n" +
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
	protected int[] muSpotCutoffAngleHandles;
	protected int[] muSpotFalloffHandles;
		
	protected float[] mNormalMatrix;
	protected float[] mTmp, mTmp2;
	protected float[] mAmbientColor, mAmbientIntensity;
	protected float[] mFogColor;
	protected float mFogNear, mFogFar;
	protected boolean mFogEnabled;

	// -- skeletal animation
	private int mvBoneIndex1Handle;
	private int mvBoneWeight1Handle;
	private int mvBoneIndex2Handle;
	private int mvBoneWeight2Handle;
	private int muBoneMatrixHandle;
	
	private int mNumJoints;
	private int mMaxWeights;
	
	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();

	public AAdvancedMaterial() {
		super();
	}
	
	public AAdvancedMaterial(int vertex_resID, int fragment_resID) {
		this(RawMaterialLoader.fetch(vertex_resID), RawMaterialLoader.fetch(fragment_resID), AMaterial.NONE);
	}
	
	public AAdvancedMaterial(int vertex_resID, int fragment_resID, boolean isAnimated) {
		this(RawMaterialLoader.fetch(vertex_resID), RawMaterialLoader.fetch(fragment_resID), isAnimated);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader) {
		this(vertexShader, fragmentShader, AMaterial.NONE);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		this(vertexShader, fragmentShader,
				isAnimated ? AMaterial.VERTEX_ANIMATION : AMaterial.NONE);
	}
	
	public AAdvancedMaterial(int vertex_resID, int fragment_resID, int parameters) {
		this(RawMaterialLoader.fetch(vertex_resID), RawMaterialLoader.fetch(fragment_resID), parameters);
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
	public void setLights(List<ALight> lights) {
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
			else if(light.getLightType() == ALight.SPOT_LIGHT)
			{
				GLES20.glUniform3fv(muLightDirectionHandles[i], 1, ((SpotLight)light).getDirection(), 0);
				GLES20.glUniform4fv(muLightAttenuationHandles[i], 1, ((SpotLight)light).getAttenuation(), 0);
				GLES20.glUniform1f(muSpotCutoffAngleHandles[i], ((SpotLight)light).getCutoffAngle());
				GLES20.glUniform1f(muSpotFalloffHandles[i], ((SpotLight)light).getFalloff());
			}
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
	
	public void setNumJoints(int numJoints)
	{
		mNumJoints = numJoints;
	}
	
	public void setMaxWeights(int maxWeights)
	{
		mMaxWeights = maxWeights;
	}	
	
	public void setBone1Indexes(final int boneIndex1BufferHandle) {
		if(checkValidHandle(boneIndex1BufferHandle, "bone indexes 1 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneIndex1BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneIndex1Handle);
			GLES20.glVertexAttribPointer(mvBoneIndex1Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBone2Indexes(final int boneIndex2BufferHandle) {
		if(checkValidHandle(boneIndex2BufferHandle, "bone indexes 2 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneIndex2BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneIndex2Handle);
			GLES20.glVertexAttribPointer(mvBoneIndex2Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBone1Weights(final int boneWeights1BufferHandle) {
		if(checkValidHandle(boneWeights1BufferHandle, "bone weights 1 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneWeights1BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneWeight1Handle);
			GLES20.glVertexAttribPointer(mvBoneWeight1Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBone2Weights(final int boneWeights2BufferHandle) {
		if(checkValidHandle(boneWeights2BufferHandle, "bone weights 2 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneWeights2BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneWeight2Handle);
			GLES20.glVertexAttribPointer(mvBoneWeight2Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBoneMatrix(float[] boneMatrix) {
		if(checkValidHandle(muBoneMatrixHandle, null))
			GLES20.glUniformMatrix4fv(muBoneMatrixHandle, mNumJoints, false, boneMatrix, 0);
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
		vertexShader = replaceShaderVars(vertexShader);
		fragmentShader = replaceShaderVars(fragmentShader);
		
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
			lightVars.append("uniform float uSpotExponent").append(i).append(";\n");
			lightVars.append("uniform float uSpotCutoffAngle").append(i).append(";\n");
			lightVars.append("uniform float uSpotFalloff").append(i).append(";\n");
		}
		vertexShader = vertexShader.replace("%LIGHT_VARS%", lightVars.toString());
		if(mSkeletalAnimationEnabled)
		{
			if(mMaxWeights > 4)
			vertexShader = "\n#define VERTEX_WEIGHT_8\n" + vertexShader;	
			vertexShader = vertexShader.replace("%NUM_JOINTS%", Integer.toString(mNumJoints));
		}
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
		muSpotCutoffAngleHandles = new int[numLights];
		muSpotFalloffHandles = new int[numLights];
		
		for(int i=0; i<mLights.size(); ++i) {
			muLightColorHandles[i] 			= getUniformLocation("uLightColor" + i);
			muLightPowerHandles[i] 			= getUniformLocation("uLightPower" + i);
			muLightPositionHandles[i] 		= getUniformLocation("uLightPosition" + i);
			muLightDirectionHandles[i] 		= getUniformLocation("uLightDirection" + i);
			muLightAttenuationHandles[i] 	= getUniformLocation("uLightAttenuation" + i);
			muSpotCutoffAngleHandles[i] 	= getUniformLocation("uSpotCutoffAngle" + i);
			muSpotFalloffHandles[i] 		= getUniformLocation("uSpotFalloff" + i);
		}
		
		if(RajawaliRenderer.isFogEnabled()) {
			muFogColorHandle = getUniformLocation("uFogColor");
			muFogNearHandle = getUniformLocation("uFogNear");
			muFogFarHandle = getUniformLocation("uFogFar");
			muFogEnabledHandle = getUniformLocation("uFogEnabled");
		}
		
		if(mSkeletalAnimationEnabled)
		{
			mvBoneIndex1Handle = getAttribLocation("vBoneIndex1");
			mvBoneWeight1Handle = getAttribLocation("vBoneWeight1");
			
			if(mMaxWeights>4){//TODO check if maxWeights > 8 -> throw exception
				mvBoneIndex2Handle = getAttribLocation("vBoneIndex2");
				mvBoneWeight2Handle = getAttribLocation("vBoneWeight2");
			}
			
			muBoneMatrixHandle = getUniformLocation("uBoneMatrix");
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
		muLightColorHandles = null;
		muLightPowerHandles = null;
		muLightPositionHandles = null;
		muLightDirectionHandles = null;
		muLightAttenuationHandles = null;
		muSpotCutoffAngleHandles = null;
		muSpotFalloffHandles = null;
		mNormalMatrix = null;
		mTmp = null;
		mTmp2 = null;
		mAmbientColor = null;
		mAmbientIntensity = null;
		mFogColor = null;
		mTmpNormalMatrix = null;
		mTmpMvMatrix = null;
	}
	
	private final String replaceShaderVars(String shader) {
		if (shader.contains("%FOG_VERTEX_VARS%"))
			shader = shader.replace("%FOG_VERTEX_VARS%", M_FOG_VERTEX_VARS);
		
		if (shader.contains("M_FOG_VERTEX_DENSITY"))
			shader = shader.replace("M_FOG_VERTEX_DENSITY", M_FOG_VERTEX_DENSITY);
		
		if (shader.contains("%FOG_FRAGMENT_VARS%"))
			shader = shader.replace("%FOG_FRAGMENT_VARS%", M_FOG_FRAGMENT_VARS);
		
		if (shader.contains("M_FOG_FRAGMENT_COLOR"))
			shader = shader.replace("M_FOG_FRAGMENT_COLOR", M_FOG_FRAGMENT_COLOR);
		
		if (shader.contains("%SKELETAL_ANIM_VERTEX_VARS%"))
			shader = shader.replace("%SKELETAL_ANIM_VERTEX_VARS%", M_SKELETAL_ANIM_VERTEX_VARS);
		
		if (shader.contains("M_SKELETAL_ANIM_VERTEX_MATRIX"))
			shader = shader.replace("M_SKELETAL_ANIM_VERTEX_MATRIX", M_SKELETAL_ANIM_VERTEX_MATRIX);
		
		return shader;
	}
}
