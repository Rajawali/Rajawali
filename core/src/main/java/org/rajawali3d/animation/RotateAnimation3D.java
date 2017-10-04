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

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

public class RotateAnimation3D extends Animation3D {

	protected double mRotateX;
	protected double mRotateY;
	protected double mRotateZ;
	protected Quaternion mQuat;
	protected Quaternion mQuatFrom;

	public RotateAnimation3D(double xRotate, double yRotate, double zRotate) {
		super();

		mQuat = Quaternion.getIdentity();
		mQuatFrom = new Quaternion();

		mRotateX = xRotate;
		mRotateY = yRotate;
		mRotateZ = zRotate;

		mQuat.multiply(new Quaternion().fromAngleAxis(Vector3.getAxisVector(Axis.Y), yRotate));
		mQuat.multiply(new Quaternion().fromAngleAxis(Vector3.getAxisVector(Axis.Z), zRotate));
		mQuat.multiply(new Quaternion().fromAngleAxis(Vector3.getAxisVector(Axis.X), xRotate));
	}

	public RotateAnimation3D(Vector3 rotate) {
		this(rotate.x, rotate.y, rotate.z);
	}

	@Override
	public void eventStart() {
		if (isFirstStart())
			mTransformable3D.getOrientation(mQuatFrom);
		
		super.eventStart();
	}

	@Override
	protected void applyTransformation() {
		Quaternion orientation = new Quaternion().slerp(mQuatFrom, mQuat, mInterpolatedTime, false);
		mTransformable3D.setOrientation(orientation);
	}

}
