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
package rajawali.math;

import rajawali.math.vector.Vector3;

public class Plane {
	private static Vector3 mTmp1;
	private static Vector3 mTmp2;
	public final Vector3 mNormal;
	public double d = 0;

	public enum PlaneSide {
		Back, OnPlane,  Front
	}

	public Plane() {
		mTmp1 = new Vector3();
		mTmp2 = new Vector3();
		mNormal = new Vector3();
	}
	
	public Plane(Vector3 normal, double d) {
		this();
		mNormal.setAll(normal);
		mNormal.normalize();
		this.d = d;
	} 

	public Plane(Vector3 point1, Vector3 point2, Vector3 point3) {
		this();
		set(point1, point2, point3);
	}

	public void set(Vector3 point1, Vector3 point2, Vector3 point3) {
		mTmp1.setAll(point1);
		mTmp2.setAll(point2);
		mTmp1.x -= mTmp2.x; mTmp1.y -= mTmp2.y; mTmp1.z -= mTmp2.z;
		mTmp2.x -= point3.x; mTmp2.y -= point3.y; mTmp2.z -= point3.z;

		mNormal.setAll((mTmp1.y * mTmp2.z) - (mTmp1.z * mTmp2.y), (mTmp1.z * mTmp2.x) - (mTmp1.x * mTmp2.z), (mTmp1.x * mTmp2.y) - (mTmp1.y * mTmp2.x));

		mNormal.normalize();

		d = -Vector3.dot(point1, mNormal); 
	}

	public void setAll(double nx, double ny, double nz, double d) {
		mNormal.setAll(nx, ny, nz);
		this.d = d;
	}

	public double distance(Vector3 point) {
		return Vector3.dot(mNormal, point) + d;
	}

	public PlaneSide getPointSide(Vector3 point) {
		double dist =Vector3.dot(mNormal, point) + d;
		if (dist == 0) {return PlaneSide.OnPlane;}
		else if (dist < 0){ return PlaneSide.Back;}
		else {return PlaneSide.Front;}
	}

	public boolean isFrontFacing(Vector3 direction) {
		double dot = Vector3.dot(mNormal, direction); 
		return dot <= 0;
	}

	public Vector3 getNormal() {
		return mNormal;
	}

	public double getD() {
		return d;
	}

	public void setAllFrom(Plane plane) {
		this.mNormal.setAll(plane.mNormal);
		this.d = plane.d;
	}
}
