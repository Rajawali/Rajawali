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
package org.rajawali3d.animation;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import android.graphics.Color;

public class ColorAnimation3D extends Animation3D {

	protected final float[] mAddedColor = new float[3];
	protected final float[] mFromColor = new float[3];
	protected final float[] mMultipliedColor = new float[3];
	protected final float[] mToColor = new float[3];
	protected final float[] mDiffColor;
	protected final int mDiffAlpha;
	protected final int mFromAlpha;	
	protected final int mToAlpha;
	
	protected int mMultipliedAlpha;

	public ColorAnimation3D(int fromColor, int toColor) {
		super();
		Color.colorToHSV(fromColor, mFromColor);
		Color.colorToHSV(toColor, mToColor);

		mFromAlpha = fromColor >>> 24;
		mToAlpha = toColor >>> 24;
		
		mDiffColor = new float[3];
		mDiffColor[0] = mToColor[0] - mFromColor[0];
		mDiffColor[1] = mToColor[1] - mFromColor[1];
		mDiffColor[2] = mToColor[2] - mFromColor[2];

		mDiffAlpha = mToAlpha - mFromAlpha;
	}

	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (!(transformable3D instanceof Object3D)) {
			throw new RuntimeException(
					"ColorAnimation3D requires the passed transformable3D to be an instance of "
							+ Object3D.class.getSimpleName());
		}
	}

	@Override
	protected void applyTransformation() {
		mMultipliedColor[0] = mDiffColor[0] * (float) mInterpolatedTime;
		mMultipliedColor[1] = mDiffColor[1] * (float) mInterpolatedTime;
		mMultipliedColor[2] = mDiffColor[2] * (float) mInterpolatedTime;
		mMultipliedAlpha = (int) (mDiffAlpha * (float) mInterpolatedTime);

		mAddedColor[0] = mFromColor[0] + mMultipliedColor[0];
		mAddedColor[1] = mFromColor[1] + mMultipliedColor[1];
		mAddedColor[2] = mFromColor[2] + mMultipliedColor[2];

		((Object3D) mTransformable3D).setColor(Color.HSVToColor(mMultipliedAlpha + mFromAlpha, mAddedColor));
	}

}
