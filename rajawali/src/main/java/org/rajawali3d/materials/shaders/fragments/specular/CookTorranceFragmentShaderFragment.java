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
package org.rajawali3d.materials.shaders.fragments.specular;

import java.util.List;

import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.methods.DiffuseMethod.DiffuseShaderVar;
import org.rajawali3d.materials.methods.SpecularMethod.SpecularShaderVar;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;
import org.rajawali3d.materials.shaders.fragments.texture.ATextureFragmentShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import android.graphics.Color;
import android.opengl.GLES20;

public class CookTorranceFragmentShaderFragment extends ATextureFragmentShaderFragment implements IShaderFragment {
	public final static String SHADER_ID = "COOK_TORRANCE_FRAGMENT";
	
	private RVec3 muSpecularColor;
	private RFloat muRoughness;
	private RFloat muExtinctionCoefficient;
	
	private float[] mSpecularColor;
	private float mRoughness;
	private float mExtinctionCoefficient;
	
	private int muSpecularColorHandle;
	private int muRoughnessHandle;
	private int muExtinctionCoefficientHandle;
	
	private List<ALight> mLights;
	
	public CookTorranceFragmentShaderFragment(List<ALight> lights, int specularColor, float roughness) {
		this(lights, specularColor, roughness, 1/8f, null);
	}
	
	public CookTorranceFragmentShaderFragment(List<ALight> lights, int specularColor, float roughness, float extinctionCoefficient, List<ATexture> textures) {
		super(textures);
		mSpecularColor = new float[] { 1, 1, 1 };
		mSpecularColor[0] = (float)Color.red(specularColor) / 255.f;
		mSpecularColor[1] = (float)Color.green(specularColor) / 255.f;
		mSpecularColor[2] = (float)Color.blue(specularColor) / 255.f;
		mRoughness = roughness;
		mExtinctionCoefficient = extinctionCoefficient;
		mLights = lights;
		mTextures = textures;
		initialize();
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}

	@Override
	public void main() {

	RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
	RVec3 eyeDir = (RVec3) getGlobal(DefaultShaderVar.V_EYE_DIR);
	RVec3 normal = (RVec3) getGlobal(DefaultShaderVar.G_NORMAL);

	RFloat k = (RFloat) getGlobal(SpecularShaderVar.U_EXTINCTION_COEFFICIENT);
	RFloat F0 = new RFloat("F0");
	F0.assign("0.8");
	RFloat nDotV = new RFloat("nDotV");
	nDotV.assign("max(0., dot(gNormal, normalize(-vEyeDir)))");

	RFloat Rs = new RFloat("Rs");
	RVec3 specular = new RVec3("specular");

	for(int i=0; i<mLights.size(); ++i) {
		Rs.assign("0.0");
		specular.assign("vec3(0.,0.,0.)");

		RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION, i);
		RFloat lightPower = (RFloat)getGlobal(LightsShaderVar.U_LIGHT_POWER, i);
		RVec3 lightColor = (RVec3)getGlobal(LightsShaderVar.U_LIGHT_COLOR, i);
		RVec3 lightDir = new RVec3("lightDir" + i);

		RFloat NdotL = (RFloat)getGlobal(DiffuseShaderVar.L_NDOTL, i);

		startif(new Condition(NdotL, Operator.GREATER_THAN, "0.0")); {

		RVec3 H = new RVec3("H" + i);
		H.assign("normalize(lightDir" + i + " + normalize(-vEyeDir))");
		RFloat NdotH = new RFloat("NdotH");
		NdotH.assign("max(0., dot(gNormal, H" + i + "))");
		RFloat VdotH = new RFloat("VdotH");
		VdotH.assign("max(0., dot(normalize(-vEyeDir), H" + i + "))");

		// Fresnel reflectance
		RFloat F = new RFloat("F");
		F.assign("pow(1.0 - VdotH, 5.0)");
		F.assignMultiply("1.0 - F0");
		F.assignAdd(F0);

		// Microfacet distribution by Beckmann
		RFloat m_squared = new RFloat("m_squared");
		m_squared.assign(muRoughness.multiply(muRoughness));
		RFloat r1 = new RFloat("r1");
		r1.assign("1.0 / (4.0 * m_squared * pow(NdotH, 4.0))");
		RFloat r2 = new RFloat("r2");
		r2.assign("(NdotH * NdotH - 1.0) / (m_squared * NdotH * NdotH)");
		RFloat D = new RFloat("D");
		D.assign("r1 * exp(r2)");

		// Geometric shadowing
		RFloat two_NdotH = new RFloat("two_NdotH");
		two_NdotH.assign("2.0 * NdotH");
		RFloat g1 = new RFloat("g1");
		g1.assign("(two_NdotH * nDotV) / VdotH");
		RFloat g2 = new RFloat("g2");
		g2.assign("(two_NdotH * NdotL" + i + ") / VdotH");
		RFloat G = new RFloat("G");
		G.assign("min(1.0, min(g1, g2))");

		Rs.assign("(F * D * G) / (" + Math.PI + " * NdotL" + i + " * nDotV)");

		specular.assign("uSpecularColor * NdotL" + i);
		specular.assignMultiply("(uK + Rs * (1. - uK))");
		specular.assignMultiply(lightColor.multiply(lightPower).multiply(attenuation));

		} endif();

		color.rgb().assignAdd(specular);
	}

	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		muSpecularColor = (RVec3) addUniform(SpecularShaderVar.U_SPECULAR_COLOR);
		muRoughness = (RFloat) addUniform(SpecularShaderVar.U_ROUGHNESS);
		muExtinctionCoefficient = (RFloat) addUniform(SpecularShaderVar.U_EXTINCTION_COEFFICIENT);
	}
	
	@Override
	public void setLocations(int programHandle) {
		super.setLocations(programHandle);
		muSpecularColorHandle = getUniformLocation(programHandle, SpecularShaderVar.U_SPECULAR_COLOR);
		muRoughnessHandle = getUniformLocation(programHandle, SpecularShaderVar.U_ROUGHNESS);
		muExtinctionCoefficientHandle = getUniformLocation(programHandle, SpecularShaderVar.U_EXTINCTION_COEFFICIENT);
	}
	
	@Override
	public void applyParams() {
		super.applyParams();
		GLES20.glUniform3fv(muSpecularColorHandle, 1, mSpecularColor, 0);
		GLES20.glUniform1f(muRoughnessHandle, mRoughness);
		GLES20.glUniform1f(muExtinctionCoefficientHandle, mExtinctionCoefficient);
	}
	
	public void setSpecularColor(int color)
	{
		mSpecularColor[0] = (float)Color.red(color) / 255.f;
		mSpecularColor[1] = (float)Color.green(color) / 255.f;
		mSpecularColor[2] = (float)Color.blue(color) / 255.f;
	}
	
	public void setExtinctionCoefficient(float extinctionCoefficient)
	{
		mExtinctionCoefficient = extinctionCoefficient;
	}
	
	public void setRoughness(float roughness)
	{
		mRoughness = roughness;
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
	
	public void bindTextures(int nextIndex) {}
	public void unbindTextures() {}
}
