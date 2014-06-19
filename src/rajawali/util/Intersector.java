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
package rajawali.util;

import rajawali.math.Plane;
import rajawali.math.vector.Vector3;

/** Class offering various static methods for intersection testing between different geometric objects.
 * 
 * Originally written by Badlogic Games. Ported for Rajawali by Andrew Jo.
 *  
 * @author badlogicgames@gmail.com
 * @author jan.stria
 * @author andrewjo@gmail.com
 */
public final class Intersector {
	private final static Vector3 v0 = new Vector3();
	private final static Vector3 v1 = new Vector3();
	private final static Vector3 v2 = new Vector3();
	
	/**
	 * Intersects a ray defined by a start and end point and a {@link Plane}.
	 * @param rayStart Startpoint of the ray
	 * @param rayEnd Endpoint of the ray
	 * @param plane The plane
	 * @param hitPoint The intersection point (optional)
	 * @return True if there is an intersection, false otherwise.
	 */
	public static boolean intersectRayPlane(Vector3 rayStart, Vector3 rayEnd, Plane plane, Vector3 hitPoint) {
		Vector3 rayDir = Vector3.subtractAndCreate(rayEnd, rayStart);
		double denorm = rayDir.dot(plane.getNormal());
		if (denorm != 0) {
			double t = -(rayStart.dot(plane.getNormal()) + plane.getD()) / denorm;
			if (t < 0) return false;
			
			if (hitPoint != null) hitPoint.addAndSet(rayStart, Vector3.scaleAndCreate(rayDir, t));
			return true;
		} else if (plane.getPointSide(rayStart) == Plane.PlaneSide.ONPLANE) {
			if (hitPoint != null) hitPoint.setAll(rayStart);
			return true;
		} else {
			return false;
		}
	}
	
	private static final Plane p = new Plane();
	private static final Vector3 i = new Vector3();
	
	/**
	 * Intersects a ray defined by a start and end point and a triangle.
	 * @param rayStart Startpoint of the ray
	 * @param rayEnd Endpoint of the ray
	 * @param t1 The first vertex of the triangle
	 * @param t2 The second vertex of the triangle
	 * @param t3 The third vertex of the triangle
	 * @param hitPoint The intersection point (optional)
	 * @return True if there is an intersection, false otherwise.
	 */
	public static boolean intersectRayTriangle(Vector3 rayStart, Vector3 rayEnd, Vector3 t1, Vector3 t2, Vector3 t3, Vector3 hitPoint) {
		Vector3 rayDir = Vector3.subtractAndCreate(rayEnd, rayStart);
		rayDir.normalize();
		p.set(t1, t2, t3);
		if (!intersectRayPlane(rayStart, rayEnd, p, i)) return false;
		
		v0.subtractAndSet(t3, t1);
		v1.subtractAndSet(t2, t1);
		v2.subtractAndSet(i, t1);
		
		double dot00 = v0.dot(v0);
		double dot01 = v0.dot(v1);
		double dot02 = v0.dot(v2);
		double dot11 = v1.dot(v1);
		double dot12 = v1.dot(v2);
		
		double denom = dot00 * dot11 - dot01 * dot01;
		if (denom == 0) return false;
		
		double u = (dot11 * dot02 - dot01 * dot12) / denom;
		double v = (dot00 * dot12 - dot01 * dot02) / denom;
		
		if (u >= 0 && v >= 0 && u + v <= 1) {
			if (hitPoint != null) hitPoint.setAll(i);
			return true;
		} else
			return false;
	}
	
	/**
	 * Intersects a ray defined by the start and end point and a sphere, returning the intersection point in intersection.
	 * @param rayStart Startpoint of the ray
	 * @param rayEnd Endpoint of the ray
	 * @param sphereCenter The center of the sphere
	 * @param sphereRadius The radius of the sphere
	 * @param hitPoint The intersection point (optional)
	 * @return True if there is an intersection, false otherwise.
	 */
	public static boolean intersectRaySphere(Vector3 rayStart, Vector3 rayEnd, Vector3 sphereCenter, double sphereRadius, Vector3 hitPoint) {
		rayStart = new Vector3(rayStart);
		rayEnd = new Vector3(rayEnd);
		Vector3 dir = Vector3.subtractAndCreate(rayEnd, rayStart);
		dir.normalize();
		
		sphereCenter = new Vector3(sphereCenter);
		double radius2 = sphereRadius * sphereRadius;
		
		/*
		 * Refer to http://paulbourke.net/geometry/circlesphere/ for mathematics
		 * behind ray-sphere intersection.
		 */
		double a = Vector3.dot(dir, dir);
		double b = 2.0f * Vector3.dot(dir, Vector3.subtractAndCreate(rayStart, sphereCenter));
		double c = Vector3.dot(sphereCenter, sphereCenter) + Vector3.dot(rayStart, rayStart) - 2.0f * Vector3.dot(sphereCenter, rayStart) - radius2;
		
		// Test for intersection.
		double result = b * b - 4.0f * a * c;
		
		if (result < 0) return false;
		
		// Starting with this section, the code was referenced from libGDX.
		double distSqrt = Math.sqrt(result);
		double q;
		
		if (b < 0)
			q = (-b - distSqrt) / 2.0f;
		else
			q = (-b + distSqrt) / 2.0f;
		
		
		double t0 = q / 1;
		double t1 = c / q;
		
		// If t0 is larger than t1, swap them around.
		if (t0 > t1) {
			double temp = t0;
			t0 = t1;
			t1 = temp;
		}
		
		// If t1 is less than zero, the object is in the ray's negative direction
		// and consequently ray misses the sphere.
		if (t1 < 0) return false;
		
		// If t0 is less than zero, intersection point is at t1.
		if (t0 < 0) {
			hitPoint = rayStart.add(Vector3.scaleAndCreate(dir, t1));
			return true;
		} else {
			hitPoint = rayStart.add(Vector3.scaleAndCreate(dir, t0));
			return true;
		}
	}
}
