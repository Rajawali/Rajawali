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

import rajawali.lights.ALight;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;


public class LightsFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LIGHTS_FRAGMENT";
	
	private List<ALight> mLights;
	
	private RVec3[] muLightColor, muLightPosition, muLightDirection;
	private RFloat[] mvAttenuation;
	private RVec4 mvEye;
	private RFloat[] muLightPower, muSpotCutoffAngle, muSpotFalloff;
	
	public LightsFragmentShaderFragment(List<ALight> lights) {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mLights = lights;
		initialize();
	}

	@Override
	public void initialize()
	{
		super.initialize();
		
		int lightCount = mLights.size();
		@SuppressWarnings("unused")
		int dirLightCount = 0, spotLightCount = 0, pointLightCount = 0;

		for (int i = 0; i < lightCount; i++)
		{
			if (mLights.get(i).getLightType() == ALight.DIRECTIONAL_LIGHT)
				dirLightCount++;
			else if (mLights.get(i).getLightType() == ALight.SPOT_LIGHT)
				spotLightCount++;
			else if (mLights.get(i).getLightType() == ALight.POINT_LIGHT)
				pointLightCount++;
		}
		
		muLightPosition = new RVec3[lightCount];
		muLightColor = new RVec3[lightCount];
		muLightPower = new RFloat[lightCount];
		muLightDirection = new RVec3[dirLightCount + spotLightCount];
		muSpotCutoffAngle = new RFloat[spotLightCount];
		muSpotFalloff = new RFloat[spotLightCount];
		mvAttenuation = new RFloat[lightCount];

		//mgLightDirection = (RVec3) addGlobal(LightsShaderVar.G_LIGHT_DIRECTION, DataType.VEC3);
		mvEye = (RVec4) addVarying(LightsShaderVar.V_EYE);
		
		dirLightCount = 0;
		spotLightCount = 0;
		pointLightCount = 0;

		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();

			muLightPosition[i] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_POSITION, i);
			muLightPower[i] = (RFloat) addUniform(LightsShaderVar.U_LIGHT_POWER, i);
			muLightColor[i] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_COLOR, i);
			
			if(t == ALight.DIRECTIONAL_LIGHT || t == ALight.SPOT_LIGHT)
			{
				muLightDirection[dirLightCount] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_DIRECTION, dirLightCount);
				dirLightCount++;
			}
			if(t == ALight.SPOT_LIGHT)
			{
				muSpotCutoffAngle[spotLightCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_CUTOFF_ANGLE, spotLightCount);
				muSpotFalloff[spotLightCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_FALLOFF, spotLightCount);
				spotLightCount++;
			}
			mvAttenuation[i] = (RFloat) addVarying(LightsShaderVar.V_LIGHT_ATTENUATION, i);
		}
		
		addVarying(LightsShaderVar.V_AMBIENT_COLOR);
	}
	
	@Override
	public void main() {
		int lightDirCount = 0, lightAttCount = 0;
		int spotCount = 0;
		
		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();
			RVec3 lightDir = new RVec3("lightDir" + i);

			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				lightDir.assign(normalize(muLightPosition[i].subtract(mvEye.xyz())));
				
				if(t == ALight.SPOT_LIGHT) {
					//
					// -- vec3 spotDir = normalize(-uLightDirection);
					//
					RVec3 spotDir = new RVec3("spotDir" + spotCount);
					spotDir.assign(normalize(muLightDirection[lightAttCount].multiply(-1.0f)));
					
					//
					// -- float spot_factor = dot(lightDir, spotDir);
					//
					RFloat spotFactor = new RFloat("spotFactor" + spotCount);
					spotFactor.assign(dot(lightDir, spotDir));
				
					//
					// -- if(uSpotCutoffAngle < 180.0 ) {
					//
					startif(muSpotCutoffAngle[spotCount], "<", 180.0f);
					{
						//
						// -- if(spotFactor >= cos(radians(uSpotCutoffAngle))) {
						//
						startif(spotFactor, ">=", cos(radians(muSpotCutoffAngle[spotCount])));
						{
							//
							// -- spotFactor = (1.0 - (1.0 - spotFactor * 1.0 / (1.0 - cos(radians(uSpotCutoffAngle))));
							//
							RFloat exponent = new RFloat("exponent");
							exponent.assign(subtract(1.0f, cos(radians(muSpotCutoffAngle[spotCount]))));
							exponent.assign(divide(1.0f, exponent));
							
							RFloat facInv = new RFloat("facInv");
							facInv.assign(subtract(1, spotFactor));
							
							exponent.assign(facInv.multiply(exponent));
							exponent.assign(subtract(1, exponent));
							
							//
							// -- spotFactor = pow(spotFactor, uSpotFalloff * 1.0 / spotFactor");
							//
							spotFactor.assign(pow(exponent, multiply(muSpotFalloff[spotCount], divide(1.0f, exponent))));
						}
						ifelse();
						{
							//
							// -- spotFactor = 0.0;
							//
							spotFactor.assign(0);
						}
						endif();
						//
						// -- lightDir = vec3(L.x, L.y, L.z) * spotFactor;
						//
						lightDir.assign(multiply(castVec3(lightDir.x(), lightDir.y(), lightDir.z()), spotFactor));
					}
					endif();

					spotCount++;
				}				
			} else if(t == ALight.DIRECTIONAL_LIGHT) {
				//
				// -- lightDir = normalize(-uLightDirection);
				//
				lightDir.assign(normalize(muLightDirection[lightDirCount].multiply(-1.0f)));
				lightDirCount++;
			}
		}
	}
	
	public String getShaderId()
	{
		return SHADER_ID;
	}
	
	@Override
	public void setLocations(int programHandle) {

	}

	@Override
	public void applyParams() {
	}
}
