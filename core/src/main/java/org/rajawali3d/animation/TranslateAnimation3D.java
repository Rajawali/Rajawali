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

import org.rajawali3d.math.vector.Vector3;

public class TranslateAnimation3D extends Animation3D {

	protected final Vector3 mMultipliedPosition;
	protected final Vector3 mAddedPosition;
	protected final Vector3 mFromPosition;

	protected Vector3 mToPosition;
	protected Vector3 mDiffPosition;

	public TranslateAnimation3D(Vector3 toPosition) {
		super();
		mFromPosition = new Vector3();
		mMultipliedPosition = new Vector3();
		mAddedPosition = new Vector3();
		mToPosition = new Vector3(toPosition);
	}

	public TranslateAnimation3D(Vector3 fromPosition, Vector3 toPosition) {
		this(toPosition);

		mFromPosition.setAll(fromPosition);
	}

	@Override
	protected void eventStart() {
		if (isFirstStart())
			mFromPosition.setAll(mTransformable3D.getPosition());

		super.eventStart();
	}

	@Override
	protected void applyTransformation() {
		if (mDiffPosition == null)
			mDiffPosition = Vector3.subtractAndCreate(mToPosition, mFromPosition);

		mMultipliedPosition.scaleAndSet(mDiffPosition, mInterpolatedTime);
		mAddedPosition.addAndSet(mFromPosition, mMultipliedPosition);
		mTransformable3D.setPosition(mAddedPosition);
	}

}
