/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.materials;

import java.util.List;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.lights.PointLight;
import rajawali.lights.SpotLight;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.ArrayUtils;
import android.graphics.Color;
import android.opengl.GLES20;

public abstract class AAdvancedMaterial extends AMaterial {
	protected static final int MAX_LIGHTS = RajawaliRenderer.getMaxLights(); 
	
	protected float[] mTempBoneArray = null; //We use lazy loading here because we dont know its size in advance.

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
	
	//This is used only for binding to a GLSL program and doesn't need to be double precision.
	protected final float[] mNormalFloats = new float[9];
	protected Matrix4 mNormalMatrix = new Matrix4();
	
	protected double[] mTmp, mTmp2;
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
	
	public AAdvancedMaterial() {
		super();
	}
	
	public AAdvancedMaterial(int vertex_resID, int fragment_resID) {
		this(RawMaterialLoader.fetch(vertex_resID), RawMaterialLoader.fetch(fragment_resID));
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
		mTmp = new double[16];
		mTmp2 = new double[16];
		mAmbientColor = new float[] {.2f, .2f, .2f, 1f};
		mAmbientIntensity = new float[] {.3f, .3f, .3f, 1f};		

		if(RajawaliRenderer.isFogEnabled())
			mFogColor = new float[] {.8f, .8f, .8f};
	}
	
	@Override
	public void setLights(List<ALight> lights) {
		if(lights.size() != mLights.size() && lights.size() != 0) {
			super.setLights(lights);
		} else if(lights.size() != 0) {
			boolean same = true;
			for(int i=0; i<lights.size(); ++i)
				if(lights.get(i) != mLights.get(i))
					same = false;
			if(!same)
			{
				super.setLights(lights);
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
			GLES20.glUniform3fv(muLightPositionHandles[i], 1, ArrayUtils.convertDoublesToFloats(light.getPositionArray(), mTemp3Floats), 0);
			if(light.getLightType() == ALight.DIRECTIONAL_LIGHT)
				GLES20.glUniform3fv(muLightDirectionHandles[i], 1, 
						ArrayUtils.convertDoublesToFloats(((DirectionalLight)light).getDirection(), mTemp3Floats), 0);
			else if(light.getLightType() == ALight.SPOT_LIGHT)
			{
				GLES20.glUniform3fv(muLightDirectionHandles[i], 1, 
						ArrayUtils.convertDoublesToFloats(((SpotLight)light).getDirection(), mTemp3Floats), 0);
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
	
	public void setAmbientColor(Vector3 color) {
		setAmbientColor((float) color.x, (float) color.y, (float) color.z, 1f);
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
	
	public void setBoneMatrix(double[] boneMatrix) {
		if(checkValidHandle(muBoneMatrixHandle, null))
			if (mTempBoneArray == null) {
				mTempBoneArray = new float[boneMatrix.length];
			}
			GLES20.glUniformMatrix4fv(muBoneMatrixHandle, mNumJoints, false, 
					ArrayUtils.convertDoublesToFloats(boneMatrix, mTempBoneArray), 0);
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
			
			if(mMaxWeights>4){
				mvBoneIndex2Handle = getAttribLocation("vBoneIndex2");
				mvBoneWeight2Handle = getAttribLocation("vBoneWeight2");
			}
			
			muBoneMatrixHandle = getUniformLocation("uBoneMatrix");
		}
	}
	
	@Override
	public void setModelMatrix(Matrix4 modelMatrix) {
		super.setModelMatrix(modelMatrix);
		mNormalMatrix.setAll(modelMatrix).setToNormalMatrix();
		float[] matrix = mNormalMatrix.getFloatValues();
		
		mNormalFloats[0] = matrix[0]; mNormalFloats[1] = matrix[1]; mNormalFloats[2] = matrix[2];
		mNormalFloats[3] = matrix[4]; mNormalFloats[4] = matrix[5]; mNormalFloats[5] = matrix[6];
		mNormalFloats[6] = matrix[8]; mNormalFloats[7] = matrix[9]; mNormalFloats[8] = matrix[10];
	    
		GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false, mNormalFloats, 0);
	}
	
	public void remove() {
		super.remove();
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
