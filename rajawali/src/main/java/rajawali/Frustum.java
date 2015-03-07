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
package rajawali;

import rajawali.bounds.BoundingBox;
import rajawali.math.Matrix4;
import rajawali.math.Plane;
import rajawali.math.vector.Vector3;

public class Frustum {
	private final Plane[] mPlanes;
	private Vector3 mPoint1;
	private Vector3 mPoint2;

	public Frustum() {
		mPlanes = new Plane[6];
		mPoint1 = new Vector3();
		mPoint2 = new Vector3();
		for (int i = 0; i < 6; i++)
			mPlanes[i] = new Plane();
	}

	public void update(Matrix4 inverseProjectionView) {
		float[] m = inverseProjectionView.getFloatValues();
		
		mPlanes[0].setComponents(m[Matrix4.M30] - m[Matrix4.M00], m[Matrix4.M31] - m[Matrix4.M01], m[Matrix4.M32] - m[Matrix4.M02], m[Matrix4.M33] - m[Matrix4.M03]);
		mPlanes[1].setComponents(m[Matrix4.M30] + m[Matrix4.M00], m[Matrix4.M31] + m[Matrix4.M01], m[Matrix4.M32] + m[Matrix4.M02], m[Matrix4.M33] + m[Matrix4.M03]);
		mPlanes[2].setComponents(m[Matrix4.M30] + m[Matrix4.M10], m[Matrix4.M31] + m[Matrix4.M11], m[Matrix4.M32] + m[Matrix4.M12], m[Matrix4.M33] + m[Matrix4.M13]);
		mPlanes[3].setComponents(m[Matrix4.M30] - m[Matrix4.M10], m[Matrix4.M31] - m[Matrix4.M11], m[Matrix4.M32] - m[Matrix4.M12], m[Matrix4.M33] - m[Matrix4.M13]);
		mPlanes[4].setComponents(m[Matrix4.M30] - m[Matrix4.M20], m[Matrix4.M31] - m[Matrix4.M21], m[Matrix4.M32] - m[Matrix4.M22], m[Matrix4.M33] - m[Matrix4.M23]);
		mPlanes[5].setComponents(m[Matrix4.M30] + m[Matrix4.M20], m[Matrix4.M31] + m[Matrix4.M21], m[Matrix4.M32] + m[Matrix4.M22], m[Matrix4.M33] + m[Matrix4.M23]);
		
		mPlanes[0].normalize();
		mPlanes[1].normalize();
		mPlanes[2].normalize();
		mPlanes[3].normalize();
		mPlanes[4].normalize();
		mPlanes[5].normalize();
	}

	public boolean sphereInFrustum(Vector3 center, double radius) {
		for(int i=0; i<6; i++) {
			double distance = mPlanes[i].getDistanceTo(center);
			if(distance < -radius)
				return false;
		}
		
		return true;
	}

	public boolean boundsInFrustum(BoundingBox bounds) {
		for(int i=0; i<6; i++) {
			Plane p = mPlanes[i];
			mPoint1.x = p.getNormal().x > 0 ? bounds.getMin().x : bounds.getMax().x;
			mPoint2.x = p.getNormal().x > 0 ? bounds.getMax().x : bounds.getMin().x;
			mPoint1.y = p.getNormal().y > 0 ? bounds.getMin().y : bounds.getMax().y;
			mPoint2.y = p.getNormal().y > 0 ? bounds.getMax().y : bounds.getMin().y;
			mPoint1.z = p.getNormal().z > 0 ? bounds.getMin().z : bounds.getMax().z;
			mPoint2.z = p.getNormal().z > 0 ? bounds.getMax().z : bounds.getMin().z;

			double distance1 = p.getDistanceTo(mPoint1);
			double distance2 = p.getDistanceTo(mPoint2);

			if ( distance1 < 0 && distance2 < 0 )
				return false;
		}
		
		return true;
	}

	public boolean pointInFrustum(Vector3 point) {
		for(int i=0; i<6; i++) {
			double distance = mPlanes[i].getDistanceTo(point);
			if(distance < 0)
				return false;
		}
		return true;
	}
}
