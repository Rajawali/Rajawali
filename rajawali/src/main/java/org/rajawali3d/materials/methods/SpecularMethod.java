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
package org.rajawali3d.materials.methods;

import java.util.List;

import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.shaders.AShaderBase.DataType;
import org.rajawali3d.materials.shaders.AShaderBase.IGlobalShaderVar;
import org.rajawali3d.materials.shaders.fragments.specular.PhongFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.specular.CookTorranceFragmentShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import android.graphics.Color;


/**
 * Contains a collection of diffuse shading methods. These methods are used by materials
 * that have lighting enabled. A specular highlight is the bright spot of light that appears 
 * on shiny objects when illuminated. The term specular means that light is perfectly reflected 
 * in a mirror-like way from the light source to the viewer.
 * 
 * To use a specular method you need to create an instance of one of the classes and then 
 * assign it to a material using the {@link Material#setSpecularMethod(ISpecularMethod)}
 * method:
 * <pre><code>
 * material.setSpecularMethod(new SpecularMethod.Phong());
 * </code></pre>
 * 
 * @author dennis.ippel
 * @see http://en.wikipedia.org/wiki/Specular_highlight
 *
 */
public abstract class SpecularMethod {
	/**
	 * Defines shader variables that are specific to specular shading.
	 * 
	 * @author dennis.ippel
	 *
	 */
	public enum SpecularShaderVar implements IGlobalShaderVar {
		U_SPECULAR_COLOR("uSpecularColor", DataType.VEC3),
		U_SPECULAR_INTENSITY("uSpecularIntensity", DataType.FLOAT),
		U_SHININESS("uShininess", DataType.FLOAT),
		U_ROUGHNESS("uRoughness", DataType.FLOAT),
		U_EXTINCTION_COEFFICIENT("uK", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		SpecularShaderVar(String varString, DataType dataType) {
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
	
	/**
	 * Phong shading interpolates surface normals across rasterized polygons and computes pixel
	 * colors based on the interpolated normals and a reflection model. 
	 * 
	 * To use the Phong specular method you need to create an instance of one of
	 * the methods and then assign it to a material using the {@link Material#setSpecularMethod(ISpecularMethod)}
	 * method:
	 * <pre><code>
	 * material.setSpecularMethod(new SpecularMethod.Phong());
	 * </code></pre>
	 * 
	 * @author dennis.ippel
	 * @see http://en.wikipedia.org/wiki/Phong_shading
	 */
	public static final class Phong implements ISpecularMethod
	{
		private float[] mSpecularColor = {1,1,1};
		private float mShininess;
		private float mIntensity = 1;
		private List<ALight> mLights;
		private List<ATexture> mTextures;
		private PhongFragmentShaderFragment mFragmentShader;
		
		public Phong()
		{
			this(Color.WHITE, 96);
		}
		
		public Phong(int specularColor)
		{
			this(specularColor, 96);
		}
		
		public Phong(int specularColor, float shininess)
		{
			this(specularColor, shininess, 1);
		}
		
		public Phong(int specularColor, float shininess, float intensity)
		{
			mSpecularColor [0] = (float)Color.red(specularColor) / 255f;
			mSpecularColor [1] = (float)Color.green(specularColor) / 255f;
			mSpecularColor [2] = (float)Color.blue(specularColor) / 255f;
			mShininess = shininess;
			mIntensity = intensity;
		}

		public Phong(float[] specularColor, float shininess, float intensity)
		{
			mSpecularColor = specularColor;
			mShininess = shininess;
			mIntensity = intensity;
		}
		
		public IShaderFragment getVertexShaderFragment()
		{
			return null;
		}
		
		public IShaderFragment getFragmentShaderFragment()
		{
			if(mFragmentShader == null)
				mFragmentShader = new PhongFragmentShaderFragment(mLights, mSpecularColor, mShininess, mIntensity, mTextures);
			return mFragmentShader;
		}
		
		public void setLights(List<ALight> lights)
		{
			mLights = lights;
		}
		
		public void setSpecularColor(int specularColor)
		{
			mSpecularColor [0] = (float)Color.red(specularColor) / 255f;
			mSpecularColor [1] = (float)Color.green(specularColor) / 255f;
			mSpecularColor [2] = (float)Color.blue(specularColor) / 255f;
			if(mFragmentShader != null)
				mFragmentShader.setSpecularColor(mSpecularColor);
		}
		
		public void setSpecularColor(float[] specularColor)
		{
			if(mFragmentShader != null)
				mFragmentShader.setSpecularColor(specularColor);
		}
		
		public int getSpecularColor()
		{
			int r = Math.round(mSpecularColor[0] * 255);
			int g = Math.round(mSpecularColor[1] * 255);
			int b = Math.round(mSpecularColor[2] * 255);
			return Color.rgb(r,g,b);
		}
		
		/**
		 * A high value (200) for shininess gives a more polished shine and lower (10) gives a more diffuse reflection. 
		 * 
		 * @param shininess
		 */
		public void setShininess(float shininess)
		{
			mShininess = shininess;
			if(mFragmentShader != null)
				mFragmentShader.setShininess(shininess);
		}
		
		public float getShininess()
		{
			return mShininess;
		}
		
		/**
		 * Sets the specular intensity. Use 1 for full intensity and 0 for no intensity.
		 * 
		 * @param intensity
		 */
		public void setIntensity(float intensity)
		{
			mIntensity = intensity;
		}
		
		public float getIntensity()
		{
			return mIntensity;
		}

		public void setTextures(List<ATexture> textures) {
			mTextures = textures;
		}
	}

	public static final class CookTorrance implements ISpecularMethod
	{
		private float[] mSpecularColor = {1,1,1};
		private float mRoughness = 1/8f;
		private float mExtinction = 1/8f;
		private List<ALight> mLights;
		private List<ATexture> mTextures;
		private CookTorranceFragmentShaderFragment mFragmentShader;
		
		public CookTorrance()
		{
			this(Color.WHITE, 1/8f);
		}
		
		public CookTorrance(int specularColor)
		{
			this(specularColor, 1/8f);
		}
		
		public CookTorrance(int specularColor, float roughness)
		{
			this(specularColor, roughness, 1/8f);
		}

		public CookTorrance(int specularColor, float roughness, float extinction)
		{
			mSpecularColor [0] = (float)Color.red(specularColor) / 255f;
			mSpecularColor [1] = (float)Color.green(specularColor) / 255f;
			mSpecularColor [2] = (float)Color.blue(specularColor) / 255f;
			mRoughness = roughness;
			mExtinction = extinction;
		}

		public CookTorrance(float[] specularColor, float roughness, float extinction)
		{
			mSpecularColor = specularColor;
			mRoughness = roughness;
			mExtinction = extinction;
		}
		
		public IShaderFragment getVertexShaderFragment()
		{
			return null;
		}
		
		public IShaderFragment getFragmentShaderFragment()
		{
			if(mFragmentShader == null)
				mFragmentShader = new CookTorranceFragmentShaderFragment(mLights, mSpecularColor, mRoughness, mExtinction, mTextures);
			return mFragmentShader;
		}
		
		public void setLights(List<ALight> lights)
		{
			mLights = lights;
		}
		
		public void setSpecularColor(int specularColor)
		{
			mSpecularColor [0] = (float)Color.red(specularColor) / 255f;
			mSpecularColor [1] = (float)Color.green(specularColor) / 255f;
			mSpecularColor [2] = (float)Color.blue(specularColor) / 255f;
			if(mFragmentShader != null)
				mFragmentShader.setSpecularColor(mSpecularColor);
		}

		public void setSpecularColor(float[] specularColor)
		{
			if(mFragmentShader != null)
				mFragmentShader.setSpecularColor(mSpecularColor);
		}
		
		public int getSpecularColor()
		{
			int r = Math.round(mSpecularColor[0] * 255);
			int g = Math.round(mSpecularColor[1] * 255);
			int b = Math.round(mSpecularColor[2] * 255);
			return Color.rgb(r,g,b);
		}
		
		/**
		 * @param roughness
		 */
		public void setRoughness(float roughness)
		{
			mRoughness = roughness;
			if(mFragmentShader != null)
				mFragmentShader.setRoughness(roughness);
		}
		
		public float getRoughness()
		{
			return mRoughness;
		}
		
		public void setExtinction(float extinction)
		{
			mExtinction = extinction;
		}
		
		public float getExtinction()
		{
			return mExtinction;
		}

		public void setTextures(List<ATexture> textures) {
			mTextures = textures;
		}
	}
}
