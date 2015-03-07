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


public class SpotLight extends DirectionalLight {
	protected float[] mAttenuation;
	protected float mCutoffAngle;
	protected float mMaxCutoffAngle = 180;
	protected float mFalloff;
	
	public SpotLight() {
		super();
		setLightType(SPOT_LIGHT);
		mAttenuation = new float[4];
		setCutoffAngle(40);
		setFalloff(0.4f);
		setAttenuation(50, 1, .09f, .032f);
	}

	public SpotLight(float xDir, float yDir, float zDir) {
		this();
		setDirection(xDir, yDir, zDir);
	}

	public void setAttenuation(float range, float constant, float linear, float quadratic) {
		mAttenuation[0] = range;
		mAttenuation[1] = constant;
		mAttenuation[2] = linear;
		mAttenuation[3] = quadratic;
	}

	/*
	 * Set the outer cone angle
	 */
	public void setCutoffAngle(float cutoffAng) {
		if(cutoffAng > mMaxCutoffAngle)
			cutoffAng = mMaxCutoffAngle;
		mCutoffAngle = cutoffAng;
	}

	public void setFalloff(float falloff) {
		if(Math.abs(falloff) > 1) falloff = 1;
		mFalloff = Math.abs(falloff);
	}

	public float[] getAttenuation() {
		return mAttenuation;
	}

	public float getCutoffAngle() {
		return mCutoffAngle;
	}
	
	public float getFalloff() {
		return mFalloff;
	}
}
