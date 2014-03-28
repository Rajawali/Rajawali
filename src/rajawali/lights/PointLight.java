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
package rajawali.lights;

public class PointLight extends ALight {
	protected float[] mAttenuation;
	
	public PointLight() {
		super(POINT_LIGHT);
		mAttenuation = new float[4];
		setAttenuation(50, 1, .09f, .032f);
	}
	
	public void setAttenuation(float range, float constant, float linear, float quadratic) {
		mAttenuation[0] = range;
		mAttenuation[1] = constant;
		mAttenuation[2] = linear;
		mAttenuation[3] = quadratic;
	}
	
	public float[] getAttenuation() {
		return mAttenuation;
	}
}
