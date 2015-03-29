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


public class PhongFragmentShaderFragment extends ATextureFragmentShaderFragment implements IShaderFragment {
	public final static String SHADER_ID = "PHONG_FRAGMENT";
	
	private RVec3 muSpecularColor;
	private RFloat muShininess;
	private RFloat muSpecularIntensity;
	
	private float[] mSpecularColor;
	private float mShininess;
	private float mSpecularIntensity;
	
	private int muSpecularColorHandle;
	private int muShininessHandle;
	private int muSpecularIntensityHandle;
	
	private List<ALight> mLights;
	
	public PhongFragmentShaderFragment(List<ALight> lights, int specularColor, float shininess) {
		this(lights, specularColor, shininess, 1, null);
	}
	
	public PhongFragmentShaderFragment(List<ALight> lights, int specularColor, float shininess, float specularIntensity, List<ATexture> textures) {
		super(textures);
		mSpecularColor = new float[] { 1, 1, 1 };
		mSpecularColor[0] = (float)Color.red(specularColor) / 255.f;
		mSpecularColor[1] = (float)Color.green(specularColor) / 255.f;
		mSpecularColor[2] = (float)Color.blue(specularColor) / 255.f;
		mShininess = shininess;
		mSpecularIntensity = specularIntensity;
		mLights = lights;
		mTextures = textures;
		initialize();
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}

	@Override
	public void main() {
		RFloat specular = new RFloat("specular");
		RFloat gSpecularValue = (RFloat) getGlobal(DefaultShaderVar.G_SPECULAR_VALUE);
		specular.assign(0);
		
		for(int i=0; i<mLights.size(); ++i) {
			RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION, i);
			RFloat lightPower = (RFloat)getGlobal(LightsShaderVar.U_LIGHT_POWER, i);
			RFloat nDotL = (RFloat)getGlobal(DiffuseShaderVar.L_NDOTL, i);
			RFloat spec = new RFloat("spec" + i);
			spec.assign(pow(nDotL, muShininess));
			spec.assign(spec.multiply(attenuation).multiply(lightPower));
			specular.assignAdd(spec);
		}
				
		specular.assignMultiply(muSpecularIntensity.multiply(gSpecularValue));
		
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
		RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);		
		
		if(mTextures != null && mTextures.size() > 0)
		{
			RVec4 specMapColor = new RVec4("specMapColor");
			specMapColor.assign(castVec4(0));

			for(int i=0; i<mTextures.size(); i++)
			{
				RVec4 specColor = new RVec4("specColor" + i);
				specColor.assign(texture2D(muTextures[i], textureCoord));
				specColor.assignMultiply(muInfluence[i]);
				specMapColor.assignAdd(specColor);
			}
			color.rgb().assignAdd(specular.multiply(muSpecularColor).multiply(specMapColor.rgb()));
		}		
		else
		{
			color.rgb().assignAdd(specular.multiply(muSpecularColor));
		}
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		muSpecularColor = (RVec3) addUniform(SpecularShaderVar.U_SPECULAR_COLOR);
		muShininess = (RFloat) addUniform(SpecularShaderVar.U_SHININESS);
		muSpecularIntensity = (RFloat) addUniform(SpecularShaderVar.U_SPECULAR_INTENSITY);
	}
	
	@Override
	public void setLocations(int programHandle) {
		super.setLocations(programHandle);
		muSpecularColorHandle = getUniformLocation(programHandle, SpecularShaderVar.U_SPECULAR_COLOR);
		muShininessHandle = getUniformLocation(programHandle, SpecularShaderVar.U_SHININESS);
		muSpecularIntensityHandle = getUniformLocation(programHandle, SpecularShaderVar.U_SPECULAR_INTENSITY);
	}
	
	@Override
	public void applyParams() {
		super.applyParams();
		GLES20.glUniform3fv(muSpecularColorHandle, 1, mSpecularColor, 0);
		GLES20.glUniform1f(muShininessHandle, mShininess);
		GLES20.glUniform1f(muSpecularIntensityHandle, mSpecularIntensity);
	}
	
	public void setSpecularColor(int color)
	{
		mSpecularColor[0] = (float)Color.red(color) / 255.f;
		mSpecularColor[1] = (float)Color.green(color) / 255.f;
		mSpecularColor[2] = (float)Color.blue(color) / 255.f;
	}
	
	public void setSpecularIntensity(float specularIntensity)
	{
		mSpecularIntensity = specularIntensity;
	}
	
	public void setShininess(float shininess)
	{
		mShininess = shininess;
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
	
	public void bindTextures(int nextIndex) {}
	public void unbindTextures() {}
}
