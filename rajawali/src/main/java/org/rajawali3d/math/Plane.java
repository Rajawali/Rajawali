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
package org.rajawali3d.math;

import org.rajawali3d.math.vector.Vector3;

public class Plane {
	public static enum PlaneSide {
		BACK, ONPLANE, FRONT
	}
	/**
	 * Plane normal
	 */
	private Vector3 mNormal;
	private double mD;	
	
	public Plane() {
		mNormal = new Vector3();
	}
	
	/**
	 * Create a plane from coplanar points
	 * 
	 * @param point1
	 * @param point2
	 * @param point3
	 */
	public Plane(Vector3 point1, Vector3 point2, Vector3 point3) {
		set(point1, point2, point3);
	}
	
	public void set(Vector3 point1, Vector3 point2, Vector3 point3) {
		Vector3 v1 = new Vector3();
		Vector3 v2 = new Vector3();
		v1.subtractAndSet(point1, point2);
		v2.subtractAndSet(point3, point2);
		mNormal = v1.cross(v2);
		mNormal.normalize();
		
		mD = -point1.dot(mNormal);
	}
	
	public void setComponents(double x, double y, double z, double w) {
		mNormal.setAll(x, y, z);
		mD = w;
	}
	
	public double getDistanceTo(Vector3 point) {
		return mD + mNormal.dot(point);
	}
	
	public Vector3 getNormal() {
		return mNormal;
	}
	
	public double getD() {
		return mD;
	}
	
	public PlaneSide getPointSide(Vector3 point) {
		double distance = Vector3.dot(mNormal, point) + mD;
		if(distance == 0) return PlaneSide.ONPLANE;
		else if(distance < 0) return PlaneSide.BACK;
		else return PlaneSide.FRONT;
	}
	
	public boolean isFrontFacing(Vector3 direction) {
		double dot = Vector3.dot(mNormal, direction);
		return dot <= 0;
	}
	
	public void normalize() {
		double inverseNormalLength = 1.0 / mNormal.length();
		mNormal.multiply(inverseNormalLength);
		mD *= inverseNormalLength;
	}
}
