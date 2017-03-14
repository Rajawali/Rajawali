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

import android.graphics.Color;

import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.shaders.AShaderBase.DataType;
import org.rajawali3d.materials.shaders.AShaderBase.IGlobalShaderVar;
import org.rajawali3d.materials.shaders.fragments.diffuse.LambertFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.diffuse.LambertVertexShaderFragment;
import org.rajawali3d.materials.shaders.fragments.effects.ToonFragmentShaderFragment;


/**
 * Contains a collection of diffuse shading methods. These methods are used by materials
 * that have lighting enabled. Diffuse shading determines the color of a material when a
 * light shines on it. To use a diffuse method you need to create an instance of one of
 * the classes and then assign it to a material using the {@link Material#setDiffuseMethod(IDiffuseMethod)}
 * method:
 * <pre><code>
 * material.setDiffuseMethod(new DiffuseMethod.Lambert());
 * </code></pre>
 * 
 * @author dennis.ippel
 * @see http://en.wikipedia.org/wiki/Diffuse_reflection
 *
 */
public abstract class DiffuseMethod {
	/**
	 * Defines shader variables that are specific to diffuse shading.
	 * 
	 * @author dennis.ippel
	 *
	 */
	public static enum DiffuseShaderVar implements IGlobalShaderVar {
		/**
		 * The dot product between the surface normal and the light direction. 
		 */
		L_NDOTL("NdotL", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		DiffuseShaderVar(String varString, DataType dataType) {
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
	 * Defines Lambertian reflectance. This technique causes polygons to reflect light equally
	 * in all directions. The reflection is calculated by taking the dot product of the 
	 * surface's normal vector and a normalized light direction vector. This number is then
	 * multiplied by the color of the surface and the intensity of the light hitting the 
	 * surface. 
	 * 
	 * To use the Lambertian diffuse method you need to create an instance of one of
	 * the methods and then assign it to a material using the {@link Material#setDiffuseMethod(IDiffuseMethod)}
	 * method:
	 * <pre><code>
	 * material.setDiffuseMethod(new DiffuseMethod.Lambert());
	 * </code></pre>
	 * 
	 * @see http://en.wikipedia.org/wiki/Lambertian_reflectance
	 * 
	 * @author dennis.ippel
	 *
	 */
	public static final class Lambert implements IDiffuseMethod
	{
		private float mIntensity;
		private List<ALight> mLights;
		
		public Lambert()
		{
			this(0.8f);
		}
		
		public Lambert(float intensity)
		{
			mIntensity = intensity;
		}
		
		public float getIntensity()
		{
			return mIntensity;
		}
		
		public void setIntensity(float intensity)
		{
			mIntensity = intensity;
		}
		
		public IShaderFragment getVertexShaderFragment()
		{
			return new LambertVertexShaderFragment();
		}
		
		public IShaderFragment getFragmentShaderFragment()
		{
			return new LambertFragmentShaderFragment(mLights);
		}
		
		public void setLights(List<ALight> lights)
		{
			mLights = lights;
		}
	}
	
	/**
	 * Toon shading or cel shading is a type of non-photorealistic rendering designed to
	 * make a model appear to be hand-drawn. It is often used to mimic the style of a 
	 * comic book or cartoon. Smooth lighting values are calculated for each pixel and then
	 * mapped to a small number of discrete shades to create a characteristic flat look.
	 * 
	 * To use the Toon diffuse method you need to create an instance of one of
	 * the methods and then assign it to a material using the {@link Material#setDiffuseMethod(IDiffuseMethod)}
	 * method:
	 * <pre><code>
	 * material.setDiffuseMethod(new DiffuseMethod.Toon(int toonColor1, int toonColor2, int toonColor3, int toonColor4));
	 * </code></pre>
	 * 
	 * @see http://en.wikipedia.org/wiki/Cel_shading
	 * 
	 * @author dennis.ippel
	 *
	 */
	public static final class Toon implements IDiffuseMethod
	{
		private float[] mToonColor0;
		private float[] mToonColor1;
		private float[] mToonColor2;
		private float[] mToonColor3;
		private List<ALight> mLights;
		private ToonFragmentShaderFragment mFragmentShader;

		public Toon()
		{
			mToonColor0 = new float[] { 1, .5f, .5f, 1 };
			mToonColor1 = new float[] { .6f, .3f, .3f, 1 };
			mToonColor2 = new float[] { .4f, .2f, .2f, 1 };
			mToonColor3 = new float[] { .2f, .1f, .1f, 1 };
		}
		
		public Toon(int toonColor0, int toonColor1, int toonColor2, int toonColor3)
		{
			this();
			setToonColors(toonColor0, toonColor1, toonColor2, toonColor3);
		}
		
		public IShaderFragment getVertexShaderFragment() {
			return null;
		}

		public IShaderFragment getFragmentShaderFragment() {
			if(mFragmentShader == null)
			{
				mFragmentShader = new ToonFragmentShaderFragment(mLights);
				mFragmentShader.setToonColors(mToonColor0, mToonColor1, mToonColor2, mToonColor3);
			}
			return mFragmentShader;
		}

		public void setLights(List<ALight> lights) {
			mLights = lights;
		}
		
		public void setToonColors(int color0, int color1, int color2, int color3) {
			mToonColor0[0] = Color.red(color0); 
			mToonColor0[1] = Color.green(color0);
			mToonColor0[2] = Color.blue(color0); 
			mToonColor0[3] = Color.alpha(color0); 

			mToonColor1[0] = Color.red(color1); 
			mToonColor1[1] = Color.green(color1);
			mToonColor1[2] = Color.blue(color1); 
			mToonColor1[3] = Color.alpha(color1); 

			mToonColor2[0] = Color.red(color2); 
			mToonColor2[1] = Color.green(color2);
			mToonColor2[2] = Color.blue(color2); 
			mToonColor2[3] = Color.alpha(color2); 
			
			mToonColor3[0] = Color.red(color3); 
			mToonColor3[1] = Color.green(color3);
			mToonColor3[2] = Color.blue(color3); 
			mToonColor3[3] = Color.alpha(color3); 
			
			if(mFragmentShader != null)
				mFragmentShader.setToonColors(mToonColor0, mToonColor1, mToonColor2, mToonColor3);
		}
	}
}
