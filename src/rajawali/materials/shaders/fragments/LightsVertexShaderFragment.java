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
package rajawali.materials.shaders.fragments;

import java.util.List;

import android.opengl.GLES20;

import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.lights.PointLight;
import rajawali.lights.SpotLight;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.util.ArrayUtils;

public class LightsVertexShaderFragment extends AShader implements IShaderFragment {

	public final static String SHADER_ID = "LIGHTS_VERTEX";

	public static enum LightsShaderVar implements IGlobalShaderVar {
		U_LIGHT_COLOR("uLightColor", DataType.VEC3),
		U_LIGHT_POWER("uLightPower", DataType.FLOAT),
		U_LIGHT_POSITION("uLightPosition", DataType.VEC3),
		U_LIGHT_DIRECTION("uLightDirection", DataType.VEC3),
		U_LIGHT_ATTENUATION("uLightAttenuation", DataType.VEC4),
		U_SPOT_EXPONENT("uSpotExponent", DataType.FLOAT),
		U_SPOT_CUTOFF_ANGLE("uSpotCutoffAngle", DataType.FLOAT),
		U_SPOT_FALLOFF("uSpotFalloff", DataType.FLOAT),
		U_AMBIENT_COLOR("uAmbientColor", DataType.VEC3),
		U_AMBIENT_INTENSITY("uAmbientIntensity", DataType.VEC3),
		V_LIGHT_ATTENUATION("vLightAttenuation", DataType.FLOAT),
		V_EYE("vEye", DataType.VEC4),
		V_AMBIENT_COLOR("vAmbientColor", DataType.VEC3),
		G_LIGHT_DISTANCE("gLightDistance", DataType.FLOAT),
		G_LIGHT_DIRECTION("gLightDirection", DataType.VEC3);

		private String mVarString;
		private DataType mDataType;

		LightsShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}

		public String getVarString() {
			return mVarString;
		}

		public DataType getDataType() {
			return mDataType;
		}
	}

	private RVec3[] muLightColor, muLightPosition, muLightDirection;
	private RVec3 muAmbientColor, muAmbientIntensity, mvAmbientColor;
	private RVec4[] muLightAttenuation;
	private RFloat[] muLightPower, muSpotExponent, muSpotCutoffAngle, muSpotFalloff;

	private RFloat[] mvAttenuation;
	private RVec4 mvEye;

	private RFloat mgLightDistance;
	
	private int[] muLightColorHandles, muLightPowerHandles, muLightPositionHandles,
			muLightDirectionHandles, muLightAttenuationHandles, muSpotExponentHandles,
			muSpotCutoffAngleHandles, muSpotFalloffHandles;
	protected int muAmbientColorHandle, muAmbientIntensityHandle;

	private int mDirLightCount, mSpotLightCount, mPointLightCount;

	private List<ALight> mLights;
	protected final float[] mTemp3Floats = new float[3];
	protected final float[] mTemp4Floats = new float[4];
	protected float[] mAmbientColor, mAmbientIntensity;

	public LightsVertexShaderFragment(List<ALight> lights)
	{
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
		mLights = lights;
		mAmbientColor = new float[] {.2f, .2f, .2f};
		mAmbientIntensity = new float[] {.3f, .3f, .3f};	
		initialize();
	}

	@Override
	public void initialize()
	{
		super.initialize();

		int lightCount = mLights.size();

		for (int i = 0; i < lightCount; i++)
		{
			if (mLights.get(i).getLightType() == ALight.DIRECTIONAL_LIGHT)
				mDirLightCount++;
			else if (mLights.get(i).getLightType() == ALight.SPOT_LIGHT)
				mSpotLightCount++;
			else if (mLights.get(i).getLightType() == ALight.POINT_LIGHT)
				mPointLightCount++;
		}

		muLightColor = new RVec3[lightCount];
		muLightColorHandles = new int[muLightColor.length];
		
		muLightPower = new RFloat[lightCount];
		muLightPowerHandles = new int[muLightPower.length];
		
		muLightPosition = new RVec3[lightCount];
		muLightPositionHandles = new int[muLightPosition.length];
		
		muLightDirection = new RVec3[mDirLightCount + mSpotLightCount];
		muLightDirectionHandles = new int[muLightDirection.length];
		
		muLightAttenuation = new RVec4[mSpotLightCount + mPointLightCount];
		muLightAttenuationHandles = new int[muLightAttenuation.length];
		
		mvAttenuation = new RFloat[lightCount];
		
		muSpotExponent = new RFloat[mSpotLightCount];
		muSpotExponentHandles = new int[muSpotExponent.length];
		muSpotCutoffAngle = new RFloat[mSpotLightCount];
		muSpotCutoffAngleHandles = new int[muSpotCutoffAngle.length];
		muSpotFalloff = new RFloat[mSpotLightCount];
		muSpotFalloffHandles = new int[muSpotFalloff.length];
		
		mgLightDistance = (RFloat) addGlobal(LightsShaderVar.G_LIGHT_DISTANCE);
		mvEye = (RVec4) addVarying(LightsShaderVar.V_EYE);

		int lightDirCount = 0, lightAttCount = 0;
		int spotCount = 0;

		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();

			muLightColor[i] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_COLOR, i);
			muLightPower[i] = (RFloat) addUniform(LightsShaderVar.U_LIGHT_POWER, i);
			muLightPosition[i] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_POSITION, i);
			mvAttenuation[i] = (RFloat) addVarying(LightsShaderVar.V_LIGHT_ATTENUATION, i);

			if(t == ALight.DIRECTIONAL_LIGHT || t == ALight.SPOT_LIGHT)
			{
				muLightDirection[lightDirCount] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_DIRECTION, lightDirCount);
				lightDirCount++;
			}
			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				muLightAttenuation[lightAttCount] = (RVec4) addUniform(LightsShaderVar.U_LIGHT_ATTENUATION, lightAttCount);
				lightAttCount++;
			}
			if(t == ALight.SPOT_LIGHT)
			{
				muSpotExponent[spotCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_EXPONENT, spotCount);
				muSpotCutoffAngle[spotCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_CUTOFF_ANGLE, spotCount);
				muSpotFalloff[spotCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_FALLOFF, spotCount);
				spotCount++;
			}
		}
		
		muAmbientColor = (RVec3) addUniform(LightsShaderVar.U_AMBIENT_COLOR);
		muAmbientIntensity = (RVec3) addUniform(LightsShaderVar.U_AMBIENT_INTENSITY);
		mvAmbientColor = (RVec3) addVarying(LightsShaderVar.V_AMBIENT_COLOR);
	}

	@Override
	public void main() {
		int lightAttCount = 0;
		RMat4 modelMatrix = (RMat4) getGlobal(DefaultShaderVar.U_MODEL_MATRIX);
		RVec4 position = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);
		
		mvEye.assign(enclose(modelMatrix.multiply(position)));
		mvAmbientColor.rgb().assign(muAmbientColor.rgb().multiply(muAmbientIntensity.rgb()));

		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();
			
			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				//
				// -- gLightDistance = distance(vEye.xyz, uLightPosition);
				//
				mgLightDistance.assign(distance(mvEye.xyz(), muLightPosition[i]));
				//
				// -- vAttenuation  = 1.0 / (uLightAttenuation[1] + uLightAttenuation[2] * gLightDistance + uLightAttenuation[3] * gLightDistance * gLightDistance)
				//
				mvAttenuation[i].assign(
						new RFloat(1.0)
							.divide(
									enclose(
										muLightAttenuation[lightAttCount].index(1)
										.add(muLightAttenuation[lightAttCount].index(2))
										.multiply(mgLightDistance)
										.add(muLightAttenuation[lightAttCount].index(3))
										.multiply(mgLightDistance)
										.multiply(mgLightDistance)
										)
									));
				
				lightAttCount++;
			} else if(t == ALight.DIRECTIONAL_LIGHT) {
				//
				// -- vAttenuation = 1.0
				//
				mvAttenuation[i].assign(1.0f);
			}
		}
	}

	public String getShaderId()
	{
		return SHADER_ID;
	}

	@Override
	public void setLocations(int programHandle) {
		int lightDirCount = 0, lightAttCount = 0;
		int spotCount = 0;
		
		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();
			
			muLightColorHandles[i] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_COLOR, i);
			muLightPowerHandles[i] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_POWER, i);
			muLightPositionHandles[i] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_POSITION, i);
			
			if(t == ALight.DIRECTIONAL_LIGHT || t == ALight.SPOT_LIGHT)
			{
				muLightDirectionHandles[lightDirCount] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_DIRECTION, lightDirCount);
				lightDirCount++;
			}
			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				muLightAttenuationHandles[lightAttCount] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_ATTENUATION, lightAttCount);
				lightAttCount++;
			}
			if(t == ALight.SPOT_LIGHT)
			{
				muSpotExponentHandles[spotCount] = getUniformLocation(programHandle, LightsShaderVar.U_SPOT_EXPONENT, spotCount);
				muSpotCutoffAngleHandles[spotCount] = getUniformLocation(programHandle, LightsShaderVar.U_SPOT_CUTOFF_ANGLE, spotCount);
				muSpotFalloffHandles[spotCount] = getUniformLocation(programHandle, LightsShaderVar.U_SPOT_FALLOFF, spotCount);
				spotCount++;
			}
			
			muAmbientColorHandle = getUniformLocation(programHandle, LightsShaderVar.U_AMBIENT_COLOR);
			muAmbientIntensityHandle = getUniformLocation(programHandle, LightsShaderVar.U_AMBIENT_INTENSITY);
		}
	}

	@Override
	public void applyParams() {
		super.applyParams();
		
		int lightCount = mLights.size();
		int dirCount = 0, spotCount = 0, attCount = 0;
		
		for (int i = 0; i < lightCount; i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();
			
			GLES20.glUniform3fv(muLightColorHandles[i], 1, light.getColor(), 0);
			GLES20.glUniform1f(muLightPowerHandles[i], light.getPower());
			GLES20.glUniform3fv(muLightPositionHandles[i], 1, ArrayUtils.convertDoublesToFloats(light.getPositionArray(), mTemp3Floats), 0);
			
			if(t == ALight.SPOT_LIGHT)
			{
				SpotLight l = (SpotLight)light;
				GLES20.glUniform3fv(muLightDirectionHandles[spotCount], 1, ArrayUtils.convertDoublesToFloats(l.getDirection(), mTemp3Floats), 0);
				GLES20.glUniform4fv(muLightAttenuationHandles[attCount], 1, l.getAttenuation(), 0);
				//GLES20.glUniform1f(muSpotExponentHandles[spotCount], l.get)
				GLES20.glUniform1f(muSpotCutoffAngleHandles[spotCount], l.getCutoffAngle());
				GLES20.glUniform1f(muSpotFalloffHandles[spotCount], l.getFalloff());
				spotCount++;
				dirCount++;
				attCount++;
			} else if(t == ALight.POINT_LIGHT) {
				PointLight l = (PointLight)light;
				GLES20.glUniform4fv(muLightAttenuationHandles[attCount], 1, l.getAttenuation(), 0);
				attCount++;
			} else if(t == ALight.DIRECTIONAL_LIGHT) {
				DirectionalLight l = (DirectionalLight)light;
				GLES20.glUniform3fv(muLightDirectionHandles[dirCount], 1, ArrayUtils.convertDoublesToFloats(l.getDirection(), mTemp3Floats), 0);
				dirCount++;
			}
		}
		
		GLES20.glUniform3fv(muAmbientColorHandle, 1, mAmbientColor, 0);
		GLES20.glUniform3fv(muAmbientIntensityHandle, 1, mAmbientIntensity, 0);
	}
	
	public void setAmbientColor(float[] ambientColor)
	{
		mAmbientColor[0] = ambientColor[0];
		mAmbientColor[1] = ambientColor[1];
		mAmbientColor[2] = ambientColor[2];
	}
	
	public void setAmbientIntensity(float[] ambientIntensity)
	{
		mAmbientIntensity[0] = ambientIntensity[0];
		mAmbientIntensity[1] = ambientIntensity[1];
		mAmbientIntensity[2] = ambientIntensity[2];
	}
}
