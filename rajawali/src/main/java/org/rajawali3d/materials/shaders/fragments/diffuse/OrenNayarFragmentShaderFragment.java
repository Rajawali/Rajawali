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
package org.rajawali3d.materials.shaders.fragments.diffuse;

import java.util.List;

import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.methods.DiffuseMethod.DiffuseShaderVar;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;

public class OrenNayarFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "OREN_NAYAR_FRAGMENT";
	
	private List<ALight> mLights;
	private RFloat[] mgNdotL;
	private float mRoughness = 1/3f;
	
	public OrenNayarFragmentShaderFragment(List<ALight> lights, float roughness) {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mLights = lights;
		mRoughness = roughness;
		if(roughness>1) mRoughness = 1;
		if(roughness<0) mRoughness = 0;
		initialize();
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		mgNdotL = new RFloat[mLights.size()];
		
		for (int i = 0; i < mLights.size(); i++)
		{
			mgNdotL[i] = (RFloat) addGlobal(DiffuseShaderVar.L_NDOTL, i);
		}
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RFloat gShadowValue = (RFloat) getGlobal(DefaultShaderVar.G_SHADOW_VALUE);
		RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
		RVec3 ambientColor = (RVec3) getGlobal(LightsShaderVar.G_AMBIENT_COLOR);
		RVec3 eyeDir = (RVec3) getGlobal(DefaultShaderVar.V_EYE_DIR);
		RVec3 normal = (RVec3) getGlobal(DefaultShaderVar.G_NORMAL);

		RVec3 viewDir = new RVec3("viewDir");
		viewDir.assign(normalize(eyeDir.multiply(-1)));
    		RFloat NdotV = new RFloat("NdotV");
		NdotV.assign(clamp(dot(normal, viewDir),0,1));
		RFloat sinNV = new RFloat("sinNV");
		sinNV.assign("sqrt(1.0 - NdotV * NdotV)");

		RVec3 diffuse = new RVec3("diffuse");
		diffuse.assign(0);
		RFloat power = new RFloat("power");
		power.assign(0.0f);
		
		RFloat roughness2 = new RFloat("roughness2");
		roughness2.assign(mRoughness * mRoughness);
		RFloat A = new RFloat("A");
		A.assign("1.0 - 0.5 * roughness2 / (roughness2 + 0.33)");
		RFloat B = new RFloat("B");
		B.assign("0.45 * roughness2 / (roughness2 + 0.09)");

		for (int i = 0; i < mLights.size(); i++)
		{
			RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION, i);
			RFloat lightPower = (RFloat)getGlobal(LightsShaderVar.U_LIGHT_POWER, i);
			RVec3 lightColor = (RVec3)getGlobal(LightsShaderVar.U_LIGHT_COLOR, i);
			
			RVec3 lightDir = new RVec3("lightDir" + i);
			RFloat NdotL = mgNdotL[i];
			NdotL.assign(clamp(dot(normal, lightDir), 0, 1));


			RFloat cosPhi = new RFloat("cosPhi" + i); // cos(phi_v, phi_l)
			cosPhi.assign(dot(
				new ShaderVar(normalize(viewDir.subtract(NdotV).multiply(normal)),DataType.VEC3), 
				new ShaderVar(normalize(lightDir.subtract(NdotL).multiply(normal)),DataType.VEC3)
			)); 

			RFloat sinNL = new RFloat("sinNL" + i);
			sinNL.assign("sqrt(1.0 - NdotL" + i + " * NdotL" + i + ")");

			RFloat s = new RFloat("s" + i); // sin(max(theta_v, theta_l))
			s.assign("NdotV < NdotL" + i + " ? sinNV : sinNL" + i);
			RFloat t = new RFloat("t" + i); // tan(min(theta_v, theta_l))
			t.assign("NdotV > NdotL" + i + " ? sinNV / NdotV : sinNL" + i + " / NdotL" + i); 

			//
			// -- power = uLightPower * NdotL * (A + B * cosPhi * s * t) * vAttenuation;
			//
			power.assign(lightPower.multiply(NdotL)
					.multiply(enclose(A.add(B.multiply(cosPhi).multiply(s).multiply(t))))
					.multiply(attenuation)
			);

			//
			// -- diffuse.rgb += uLightColor * power;
			//
			diffuse.assignAdd(lightColor.multiply(power));
		}
		color.rgb().assign(diffuse.multiply(color.rgb()).add(ambientColor));
		color.rgb().assignMultiply(new RFloat("1.0").subtract(gShadowValue));
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
}
