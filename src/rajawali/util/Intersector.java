/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package rajawali.util;

import rajawali.math.Number3D;
import rajawali.math.Plane;

/** Class offering various static methods for intersection testing between different geometric objects.
 * 
 * Originally written by Badlogic Games. Ported for Rajawali by Andrew Jo.
 *  
 * @author badlogicgames@gmail.com
 * @author jan.stria
 * @author andrewjo@gmail.com
 */
public final class Intersector {
	private final static Number3D v0 = new Number3D();
	private final static Number3D v1 = new Number3D();
	private final static Number3D v2 = new Number3D();
	
	/**
	 * Intersects a ray defined by a start and end point and a {@link Plane}.
	 * @param rayStart Startpoint of the ray
	 * @param rayEnd Endpoint of the ray
	 * @param plane The plane
	 * @param hitPoint The intersection point (optional)
	 * @return True if there is an intersection, false otherwise.
	 */
	public static boolean intersectRayPlane(Number3D rayStart, Number3D rayEnd, Plane plane, Number3D hitPoint) {
		Number3D rayDir = Number3D.subtract(rayEnd, rayStart);
		float denorm = rayDir.dot(plane.getNormal());
		if (denorm != 0) {
			float t = -(rayStart.dot(plane.getNormal()) + plane.getD()) / denorm;
			if (t < 0) return false;
			
			if (hitPoint != null) hitPoint.setAllFrom(Number3D.add(rayStart, Number3D.multiply(rayDir, t)));
			return true;
		} else if (plane.getPointSide(rayStart) == Plane.PlaneSide.OnPlane) {
			if (hitPoint != null) hitPoint.setAllFrom(rayStart);
			return true;
		} else {
			return false;
		}
	}
	
	private static final Plane p = new Plane(new Number3D(), 0);
	private static final Number3D i = new Number3D();
	
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
	public static boolean intersectRayTriangle(Number3D rayStart, Number3D rayEnd, Number3D t1, Number3D t2, Number3D t3, Number3D hitPoint) {
		Number3D rayDir = Number3D.subtract(rayEnd, rayStart);
		rayDir.normalize();
		p.set(t1, t2, t3);
		if (!intersectRayPlane(rayStart, rayEnd, p, i)) return false;
		
		v0.setAllFrom(Number3D.subtract(t3, t1));
		v1.setAllFrom(Number3D.subtract(t2, t1));
		v2.setAllFrom(Number3D.subtract(i, t1));
		
		float dot00 = v0.dot(v0);
		float dot01 = v0.dot(v1);
		float dot02 = v0.dot(v2);
		float dot11 = v1.dot(v1);
		float dot12 = v1.dot(v2);
		
		float denom = dot00 * dot11 - dot01 * dot01;
		if (denom == 0) return false;
		
		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;
		
		if (u >= 0 && v >= 0 && u + v <= 1) {
			if (hitPoint != null) hitPoint.setAllFrom(i);
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
	public static boolean intersectRaySphere(Number3D rayStart, Number3D rayEnd, Number3D sphereCenter, float sphereRadius, Number3D hitPoint) {
		rayStart = new Number3D(rayStart);
		rayEnd = new Number3D(rayEnd);
		Number3D dir = Number3D.subtract(rayEnd, rayStart);
		dir.normalize();
		
		sphereCenter = new Number3D(sphereCenter);
		float radius2 = sphereRadius * sphereRadius;
		
		/*
		 * Refer to http://paulbourke.net/geometry/circlesphere/ for mathematics
		 * behind ray-sphere intersection.
		 */
		float a = Number3D.dot(dir, dir);
		float b = 2.0f * Number3D.dot(dir, Number3D.subtract(rayStart, sphereCenter));
		float c = Number3D.dot(sphereCenter, sphereCenter) + Number3D.dot(rayStart, rayStart) - 2.0f * Number3D.dot(sphereCenter, rayStart) - radius2;
		
		// Test for intersection.
		float result = b * b - 4.0f * a * c;
		
		if (result < 0) return false;
		
		// Starting with this section, the code was referenced from libGDX.
		float distSqrt = (float)Math.sqrt(result);
		float q;
		
		if (b < 0)
			q = (-b - distSqrt) / 2.0f;
		else
			q = (-b + distSqrt) / 2.0f;
		
		
		float t0 = q / 1;
		float t1 = c / q;
		
		// If t0 is larger than t1, swap them around.
		if (t0 > t1) {
			float temp = t0;
			t0 = t1;
			t1 = temp;
		}
		
		// If t1 is less than zero, the object is in the ray's negative direction
		// and consequently ray misses the sphere.
		if (t1 < 0) return false;
		
		// If t0 is less than zero, intersection point is at t1.
		if (t0 < 0) {
			hitPoint = rayStart.add(Number3D.multiply(dir, t1));
			return true;
		} else {
			hitPoint = rayStart.add(Number3D.multiply(dir, t0));
			return true;
		}
	}
}
