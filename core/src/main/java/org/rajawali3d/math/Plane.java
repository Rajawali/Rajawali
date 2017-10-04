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

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import org.rajawali3d.math.vector.Vector3;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Encapsulation of the concept of a mathematical plane.
 */
public class Plane {

    private static final String TAG = "Plane";

    @IntDef({ FRONT_OF_PLANE, ON_PLANE, BACK_OF_PLANE })
	@Retention(RetentionPolicy.SOURCE)
    public @interface PlaneSide {}
    public static final int FRONT_OF_PLANE = 1;
    public static final int ON_PLANE       = 0;
    public static final int BACK_OF_PLANE  = -1;

    @NonNull
	protected final Vector3 normal;

	protected double distanceToOrigin;

	public Plane() {
		normal = new Vector3();
	}

	/**
	 * Create a plane from 3 co-planar points
	 *
	 * @param point1 {@link Vector3} Point 1 of 3.
	 * @param point2 {@link Vector3} Point 2 of 3.
	 * @param point3 {@link Vector3} Point 3 of 3.
	 */
	public Plane(@NonNull Vector3 point1, @NonNull Vector3 point2, @NonNull Vector3 point3) {
        this();
		set(point1, point2, point3);
	}

    /**
     * Updates this plane from 3 co-planar points
     *
     * @param point1 {@link Vector3} Point 1 of 3.
     * @param point2 {@link Vector3} Point 2 of 3.
     * @param point3 {@link Vector3} Point 3 of 3.
     */
	public void set(@NonNull Vector3 point1, @NonNull Vector3 point2, @NonNull Vector3 point3) {
		Vector3 u = new Vector3();
		Vector3 v = new Vector3();
		u.subtractAndSet(point2, point1);
		v.subtractAndSet(point3, point1);
		normal.crossAndSet(u, v);
		normal.normalize();
        distanceToOrigin = -point1.dot(normal);
	}

    /**
     * Sets the components which define this plane.
     *
     * @param normalX {@code double} The x component of the normal vector.
     * @param normalY {@code double} The y component of the normal vector.
     * @param normalZ {@code double} The z component of the normal vector.
     * @param d {@code double} The distance of the plane from the origin.
     */
	public void setComponents(double normalX, double normalY, double normalZ, double d) {
		normal.setAll(normalX, normalY, normalZ);
        this.distanceToOrigin = d;
		normalize();
	}

    /**
     * Computes the signed normal distance from this {@link Plane} to the provided point.
     *
     * @param point {@link Vector3} The point to compute the distance to.
     * @return {@code double} The computed normal distance.
     */
	public double getDistanceTo(@NonNull Vector3 point) {
		return (normal.dot(point) + distanceToOrigin);
	}

    /**
     * Retrieves the normal vector which defines this {@link Plane}.
     *
     * @return {@link Vector3} The normal vector.
     */
    @NonNull
	public Vector3 getNormal() {
		return normal;
	}

    /**
     * Retrieves the distance of this {@link Plane} to the origin.
     *
     * @return {@code double} The distance.
     */
	public double getDistanceToOrigin() {
		return distanceToOrigin;
	}

    /**
     * Calculates which side of this {@link Plane} the given point is on.
     *
     * @param point {@link Vector3} The point to check.
     * @return {@code int} The determined side.
     */
    @PlaneSide
	public int getPointSide(@NonNull Vector3 point) {
		double distance = Vector3.dot(normal, point) + distanceToOrigin;
		if (distance == 0) {
            return ON_PLANE;
        } else if (distance < 0) {
            return BACK_OF_PLANE;
        } else {
            return FRONT_OF_PLANE;
        }
	}

    /**
     * Determines if this plane faces in the same direction as the provided vector.
     *
     * @param direction {@link Vector3} defining the "Forward" direction to test against.
     * @return {@code true} If this {@link Plane} faces the same direction, that is, the dot product between
     * {@code direction} and the normal
     * vector is positive.
     */
	public boolean isFrontFacing(@NonNull Vector3 direction) {
		double dot = Vector3.dot(normal, direction);
		return dot >= 0;
	}

    /**
     * Normalizes this plane, calculating a valid value for the distance to origin from the normal vector.
     */
	public void normalize() {
		double inverseNormalLength = 1.0 / normal.normalize();
        distanceToOrigin *= inverseNormalLength;
	}

    @Override
    public String toString() {
        return "Plane{" +
               "normal=" + normal +
               ", distanceToOrigin=" + distanceToOrigin +
               '}';
    }
}
