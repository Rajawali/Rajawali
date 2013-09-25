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
import rajawali.math.vector.Vector3.Axis;
import rajawali.math.Quaternion;

public class RotateAnimation3D extends Animation3D {

	protected double mDegreesToRotate;
	protected double mRotateFrom;
	protected double mRotationAngle;
	protected double mRotateX;
	protected double mRotateY;
	protected double mRotateZ;
	protected Vector3 mRotationAxis;
	protected Quaternion mQuat;
	protected Quaternion mQuatFrom;
	protected Quaternion mTmpOrientation = new Quaternion();
	protected boolean mCopyCurrentOrientation;
	protected boolean mAngleAxisRotation;

	public RotateAnimation3D(Axis axis, double degreesToRotate) {
		this(axis, 0, degreesToRotate);
		mCopyCurrentOrientation = true;
	}

	public RotateAnimation3D(Axis axis, double rotateFrom, double degreesToRotate) {
		this(Vector3.getAxisVector(axis), rotateFrom, degreesToRotate);
	}

	public RotateAnimation3D(Vector3 axis, double degreesToRotate) {
		this(axis, 0, degreesToRotate);
		mCopyCurrentOrientation = true;
	}

	public RotateAnimation3D(Vector3 axis, double rotateFrom, double degreesToRotate) {
		super();
		mAngleAxisRotation = true;
		mQuat = new Quaternion();
		mQuatFrom = new Quaternion();
		mQuatFrom.fromAngleAxis(axis, rotateFrom);
		mRotationAxis = axis;
		mRotateFrom = rotateFrom;
		mDegreesToRotate = degreesToRotate;
	}

	public RotateAnimation3D(double xRotate, double yRotate, double zRotate) {
		super();
		mAngleAxisRotation = false;
		mCopyCurrentOrientation = true;

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
		if (mCopyCurrentOrientation)
			 mTransformable3D.getOrientation(mQuatFrom);
		super.eventStart();
	}

	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (mCopyCurrentOrientation)
			transformable3D.getOrientation(mQuatFrom);
	}

	@Override
	protected void applyTransformation() {
		if (mAngleAxisRotation) {
			// Rotation around an axis by amount of degrees.
			mRotationAngle = mRotateFrom + (mInterpolatedTime * mDegreesToRotate);
			mQuat.fromAngleAxis(mRotationAxis, mRotationAngle);
			mQuat.multiply(mQuatFrom);
			mTransformable3D.setOrientation(mQuat);
		} else {
			mTransformable3D.setOrientation(Quaternion.slerpAndCreate(mQuatFrom, mQuat, mInterpolatedTime));
		}
	}
}
