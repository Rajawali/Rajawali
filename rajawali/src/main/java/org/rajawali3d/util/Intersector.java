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
package org.rajawali3d.util;

import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector2;
import org.rajawali3d.math.vector.Vector3;

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
	
        enum Variants {
            MISSED,
            ENTRANCE_ONLY,
            EXIT_ONLY,
            BOTH
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
		double len = dir.normalize();
		
		sphereCenter = new Vector3(sphereCenter);
		double radius2 = sphereRadius * sphereRadius;
		
		/*
		 * Refer to http://paulbourke.net/geometry/circlesphere/ for mathematics
		 * behind ray-sphere intersection.
		 */
            Vector2 mu = new Vector2();
            switch(RaySphereIntersection(rayStart, rayEnd, sphereCenter, sphereRadius, mu)) {
                case BOTH:
                    hitPoint.subtractAndSet(rayEnd,rayStart);
                    hitPoint.multiply(Math.min(mu.getX(),mu.getY()));
                    hitPoint.add(rayStart);
                    return true;
                case ENTRANCE_ONLY:
                    hitPoint.subtractAndSet(rayEnd,rayStart);
                    hitPoint.multiply(1-mu.getX());
                    hitPoint.add(rayStart);
                    return true;
                case EXIT_ONLY:
                    hitPoint.subtractAndSet(rayEnd,rayStart);
                    hitPoint.multiply(1-mu.getY());
                    hitPoint.add(rayStart);
                    return true;
            };
            return false;
	}

        private static Variants RaySphereIntersection(Vector3 p1,Vector3 p2,Vector3 sc,double r,Vector2 mu) {
            Vector3 dp = Vector3.subtractAndCreate(p2, p1);
            double a = Vector3.length2(dp);
            double b = 2 * Vector3.dot(dp, Vector3.subtractAndCreate(p1,sc));
            double c = Vector3.length2(sc) + Vector3.length2(p1) - 2 * Vector3.dot(sc,p1) - r * r;

            double bb4ac = b * b - 4 * a * c;
            if (Math.abs(a) < Math.nextUp(0) || bb4ac < 0) {
                return Variants.MISSED;
            }

            double mu1 = (-b + Math.sqrt(bb4ac)) / (2 * a);
            double mu2 = (-b - Math.sqrt(bb4ac)) / (2 * a);
            mu.setAll(1-mu1,1-mu2);

            Variants val = Variants.BOTH;
            if(mu1<0 || mu1>1) val = Variants.EXIT_ONLY;
            if(mu2<0 || mu1>2) val = Variants.ENTRANCE_ONLY;
            if((mu1<0 || mu1>1) && (mu2<0 || mu2>1)) val = Variants.MISSED;
            return val;
        }
}
