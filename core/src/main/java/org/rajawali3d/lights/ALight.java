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
package org.rajawali3d.lights;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.math.vector.Vector3;

public abstract class ALight extends ATransformable3D {
	public static final int DIRECTIONAL_LIGHT = 0;
	public static final int POINT_LIGHT = 1;
	public static final int SPOT_LIGHT = 2;

	protected final float[] mColor = new float[] { 1.0f, 1.0f, 1.0f };
	protected final double[] mPositionArray = new double[3];
	protected final double[] mDirectionArray = new double[3];
	protected float mPower = .5f;
	private int mLightType;

	protected boolean mUseObjectTransform;

	public ALight(int lightType) {
		super();
		mLightType = lightType;
	}

	public void setColor(final float r, final float g, final float b) {
		mColor[0] = r;
		mColor[1] = g;
		mColor[2] = b;
	}

	public void setColor(int color) {
		mColor[0] = ((color >> 16) & 0xFF) / 255f;
		mColor[1] = ((color >> 8) & 0xFF) / 255f;
		mColor[2] = (color & 0xFF) / 255f;
	}

	public void setColor(Vector3 color) {
		setColor((float) color.x, (float) color.y, (float) color.z);
	}

	public float[] getColor() {
		return mColor;
	}

	public void setPower(float power) {
		mPower = power;
	}

	public float getPower() {
		return mPower;
	}

	public boolean shouldUseObjectTransform() {
		return mUseObjectTransform;
	}

	public void shouldUseObjectTransform(boolean useObjectTransform) {
		mUseObjectTransform = useObjectTransform;
	}

	public int getLightType() {
		return mLightType;
	}

	public void setLightType(int lightType) {
		mLightType = lightType;
	}

	public double[] getPositionArray() {
		mPositionArray[0] = mPosition.x;
		mPositionArray[1] = mPosition.y;
		mPositionArray[2] = mPosition.z;
		return mPositionArray;
	}
}
