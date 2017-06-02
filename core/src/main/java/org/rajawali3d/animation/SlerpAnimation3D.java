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

import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

/**
 * Performs spherical linear interpolation (SLERP) animation between two {@link Vector3}s.
 * 
 * Example usage:
 * 
 * <pre><code>
 * SlerpAnimation3D anim = new SlerpAnimation3D(pointOnSphere1, pointOnSphere1);
 * anim.setDuration(1000);
 * anim.setInterpolator(new AccelerateDecelerateInterpolator());
 * anim.setTransformable3D(myObject);
 * registerAnimation(anim);
 * anim.play();
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class SlerpAnimation3D extends Animation3D {
	
	protected final Quaternion mFrom;
	protected final Quaternion mTo;
	protected final Vector3 mForwardVec = Vector3.getAxisVector(Axis.Z);
	protected final Vector3 mTmpVec;
	protected final Vector3 mTmpQuatVector;
	protected final Quaternion mTmpQuat;
	protected final double[] mRotationMatrix;
	protected final double mDistance;
	
	public SlerpAnimation3D(Vector3 from, Vector3 to) {
		super();
		mFrom = quaternionFromVector(from.clone());
		mTo = quaternionFromVector(to.clone());
		mTmpVec = new Vector3();
		mTmpQuatVector = new Vector3();
		mTmpQuat = new Quaternion();
		mDistance = from.length();
		mRotationMatrix = new double[16];
	}
	
	@Override
	protected void applyTransformation() {
		mTmpQuat.slerp(mFrom, mTo, mInterpolatedTime);
		mTmpVec.setAll(mForwardVec);
		mTmpQuat.toRotationMatrix(mRotationMatrix);
		mTmpVec.multiply(mRotationMatrix);
		mTmpVec.multiply(mDistance);
		mTransformable3D.setPosition(mTmpVec);
	}
	
	protected Quaternion quaternionFromVector(Vector3 vec) {
		vec.normalize();
		final double angle = MathUtil.radiansToDegrees(Math.acos(Vector3.dot(mForwardVec, vec)));
		final Quaternion q = new Quaternion();
		q.fromAngleAxis(mTmpQuatVector.crossAndSet(mForwardVec, vec), angle);
		return q;
	}
}
