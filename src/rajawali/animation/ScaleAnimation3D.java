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
package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.math.vector.Vector3;

public class ScaleAnimation3D extends Animation3D {

	protected Vector3 mToScale;
	protected Vector3 mFromScale;
	protected Vector3 mDiffScale;
	protected Vector3 mMultipliedScale = new Vector3();
	protected Vector3 mAddedScale = new Vector3();

	
	public ScaleAnimation3D(double toScale) {
		super();
		mToScale = new Vector3(toScale);
	}
	public ScaleAnimation3D(double fromScale, double toScale) {
		super();
		mToScale = new Vector3(toScale);
		mFromScale = new Vector3(fromScale);
	}
	
	public ScaleAnimation3D(Vector3 toScale) {
		super();
		mToScale = toScale;
	}

	public ScaleAnimation3D(Vector3 fromScale, Vector3 toScale) {
		super();
		mToScale = toScale;
		mFromScale = fromScale;
	}

	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (mFromScale == null)
			mFromScale = new Vector3(transformable3D.getScale());
	}

	@Override
	protected void applyTransformation() {
		if (mDiffScale == null)
			mDiffScale = Vector3.subtractAndCreate(mToScale, mFromScale);

		mMultipliedScale.scaleAndSet(mDiffScale, mInterpolatedTime);
		mAddedScale.addAndSet(mFromScale, mMultipliedScale);
		mTransformable3D.setScale(mAddedScale);
	}

}
