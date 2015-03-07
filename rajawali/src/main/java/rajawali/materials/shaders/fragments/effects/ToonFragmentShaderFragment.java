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
package rajawali.materials.shaders.fragments.effects;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.methods.DiffuseMethod.DiffuseShaderVar;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;
import android.opengl.GLES20;


public class ToonFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "TOON_FRAGMENT";
	
	public static enum ToonShaderVar implements IGlobalShaderVar {
		U_TOON_COLOR0("uToonColor0", DataType.VEC4),
		U_TOON_COLOR1("uToonColor1", DataType.VEC4),
		U_TOON_COLOR2("uToonColor2", DataType.VEC4),
		U_TOON_COLOR3("uToonColor3", DataType.VEC4);
		
		private String mVarString;
		private DataType mDataType;

		ToonShaderVar(String varString, DataType dataType) {
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
	
	private RFloat[] mgNdotL;
	
	private RVec4 muToonColor0;
	private RVec4 muToonColor1;
	private RVec4 muToonColor2;
	private RVec4 muToonColor3;
	
	private float[] mToonColor0;
	private float[] mToonColor1;
	private float[] mToonColor2;
	private float[] mToonColor3;
	
	private int muToonColor0Handle;
	private int muToonColor1Handle;
	private int muToonColor2Handle;
	private int muToonColor3Handle;
	
	private List<ALight> mLights;
	
	public ToonFragmentShaderFragment(List<ALight> lights)
	{
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mLights = lights;
		initialize();
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
		RFloat intensity = new RFloat("intensity");
		RVec3 normal = (RVec3)getGlobal(DefaultShaderVar.G_NORMAL);
		RFloat power = new RFloat("power");
		power.assign(0.0f);
		intensity.assign(0.0f);
		
		for(int i=0; i<mLights.size(); i++)
		{
			RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION, i);
			RFloat lightPower = (RFloat)getGlobal(LightsShaderVar.U_LIGHT_POWER, i);
			RVec3 lightDir = new RVec3("lightDir" + i);
			RFloat nDotL = mgNdotL[i];
			nDotL.assign(max(dot(normal, lightDir), 0.1f));
			//
			// -- power = uLightPower * NdotL * vAttenuation;
			//
			power.assign(lightPower.multiply(nDotL).multiply(attenuation));
			intensity.assignAdd(power);
		}
		
		startif(new Condition(intensity, Operator.GREATER_THAN, .95f));
		{
			color.assign(muToonColor0);
		}
		ifelseif(new Condition(intensity, Operator.GREATER_THAN, .5f));
		{
			color.assign(muToonColor1);
		}
		ifelseif(new Condition(intensity, Operator.GREATER_THAN, .25f));
		{
			color.assign(muToonColor2);
		}
		ifelse();
		{
			color.assign(muToonColor3);
		}
		endif();
	}
	
	@Override
	public void initialize()
	{
		super.initialize();		
		muToonColor0 = (RVec4) addUniform(ToonShaderVar.U_TOON_COLOR0);
		muToonColor1 = (RVec4) addUniform(ToonShaderVar.U_TOON_COLOR1);
		muToonColor2 = (RVec4) addUniform(ToonShaderVar.U_TOON_COLOR2);
		muToonColor3 = (RVec4) addUniform(ToonShaderVar.U_TOON_COLOR3);
		
		mgNdotL = new RFloat[mLights.size()];
		
		for (int i = 0; i < mLights.size(); i++)
		{
			mgNdotL[i] = (RFloat) addGlobal(DiffuseShaderVar.L_NDOTL, i);
		}
	}
	
	@Override
	public void setLocations(int programHandle) {
		super.setLocations(programHandle);
		muToonColor0Handle = getUniformLocation(programHandle, ToonShaderVar.U_TOON_COLOR0);
		muToonColor1Handle = getUniformLocation(programHandle, ToonShaderVar.U_TOON_COLOR1);
		muToonColor2Handle = getUniformLocation(programHandle, ToonShaderVar.U_TOON_COLOR2);
		muToonColor3Handle = getUniformLocation(programHandle, ToonShaderVar.U_TOON_COLOR3);
	}

	@Override
	public void applyParams() {
		super.applyParams();
		GLES20.glUniform4fv(muToonColor0Handle, 1, mToonColor0, 0);
		GLES20.glUniform4fv(muToonColor1Handle, 1, mToonColor1, 0);
		GLES20.glUniform4fv(muToonColor2Handle, 1, mToonColor2, 0);
		GLES20.glUniform4fv(muToonColor3Handle, 1, mToonColor3, 0);
	}
	
	public void setToonColors(float[] toonColor0, float[] toonColor1, float[] toonColor2, float[] toonColor3) {
		mToonColor0 = toonColor0; 
		mToonColor1 = toonColor1; 
		mToonColor2 = toonColor2; 
		mToonColor3 = toonColor3; 
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}
}
