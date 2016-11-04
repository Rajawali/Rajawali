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
package c.org.rajawali3d.camera;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.intersection.Intersector;
import c.org.rajawali3d.intersection.Intersector.Intersection;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines a camera viewing frustum in world space coordinates. This is used primarily for view frustum culling but
 * can also be used to easily show camera frustum debug geometry.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Frustum {

    @IntDef({ LEFT, RIGHT, BOTTOM, TOP, NEAR, FAR })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FrustumPlanes {
    }

    /**
     * Left frustum plane
     */
    @SuppressWarnings("WeakerAccess") public static final int LEFT = 0;

    /**
     * Right frustum plane
     */
    @SuppressWarnings("WeakerAccess") public static final int RIGHT = 1;

    /**
     * Bottom frustum plane
     */
    @SuppressWarnings("WeakerAccess") public static final int BOTTOM = 2;

    /**
     * Top frustum plane
     */
    @SuppressWarnings("WeakerAccess") public static final int TOP = 3;

    /**
     * Near frustum plane
     */
    @SuppressWarnings("WeakerAccess") public static final int NEAR = 4;

    /**
     * Far frustum plane
     */
    @SuppressWarnings("WeakerAccess") public static final int FAR = 5;

    @IntDef({ NTL, NTR, NBL, NBR, FTL, FTR, FBL, FBR })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FrustumCorners {
    }

    /**
     * Near-Top-Left frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int NTL = 0;

    /**
     * Near-Top-Right frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int NTR = 1;

    /**
     * Near-Bottom-Left frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int NBL = 2;

    /**
     * Near-Bottom-Right frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int NBR = 3;

    /**
     * Far-Top-Left frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int FTL = 4;

    /**
     * Far-Top-Right frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int FTR = 5;

    /**
     * Far-Bottom-Left frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int FBL = 6;

    /**
     * Far-Bottom-Right frustum corner
     */
    @SuppressWarnings("WeakerAccess") public static final int FBR = 7;

    private static final String TAG = "Frustum";

    @NonNull
    private final Plane[] planes;

    @NonNull
    private final Vector3 scratchMin;

    @NonNull
    private final Vector3 scratchMax;

    @NonNull
    private final Vector3 scratchVector1;

    @NonNull
    private final Vector3 scratchVector2;

    /**
     * Constructs a new {@link Frustum}. The resulting frustum is in an uninitialized state.
     */
    @SuppressWarnings("WeakerAccess")
    public Frustum() {
        planes = new Plane[6];
        scratchMin = new Vector3();
        scratchMax = new Vector3();
        scratchVector1 = new Vector3();
        scratchVector2 = new Vector3();
        for (int i = 0; i < 6; ++i) {
            planes[i] = new Plane();
        }
    }

    /**
     * Fetches the internal array of {@link Plane}s which make up this {@link Frustum}.
     *
     * @return The frustum {@link Plane} array. Great care should be taken interacting with this array as any changes
     * will effect this {@link Frustum}.
     */
    @NonNull
    @SuppressWarnings("WeakerAccess")
    public Plane[] getPlanes() {
        return planes;
    }

    /**
     * Updates the planes of this frustum based on the provided {@link Vector3} array representing the 8 corners.
     *
     * @param corners {@link Vector3} array of length >= 8 representing the 8 corners of the frustum. See
     *                {@link FrustumCorners} for ordering.
     */
    public void update(@NonNull @Size(min = 8) Vector3[] corners) {
        // Define the planes based on 3 points on the plane
        planes[LEFT].set(corners[NBL], corners[NTL], corners[FBL]);
        planes[RIGHT].set(corners[NTR], corners[NBR], corners[FBR]);
        planes[TOP].set(corners[NTL], corners[NTR], corners[FTL]);
        planes[BOTTOM].set(corners[NBR], corners[NBL], corners[FBR]);
        planes[NEAR].set(corners[NTR], corners[NTL], corners[NBR]);
        planes[FAR].set(corners[FTL], corners[FTR], corners[FBL]);

        // Normalize all the planes
        planes[LEFT].normalize();
        planes[RIGHT].normalize();
        planes[BOTTOM].normalize();
        planes[TOP].normalize();
        planes[NEAR].normalize();
        planes[FAR].normalize();
    }

    /**
     * Performs an intersection test between this {@link Frustum} and the provided {@link AABB}.
     *
     * @param bounds The {@link AABB} to test.
     *
     * @return {@link Intersection} result.
     */
    @Intersection
    @SuppressWarnings("WeakerAccess")
    public int intersectBounds(@NonNull AABB bounds) {
        scratchMin.setAll(bounds.getMinBound());
        scratchMax.setAll(bounds.getMaxBound());
        @Intersection int result = Intersector.INSIDE;
        // Use scratchVector1 as negative bound, scratchVector2 as positive
        for (int i = 0; i < 6; i++) {
            Plane p = planes[i];
            scratchVector1.x = p.getNormal().x > 0 ? scratchMin.x : scratchMax.x;
            scratchVector2.x = p.getNormal().x > 0 ? scratchMax.x : scratchMin.x;
            scratchVector1.y = p.getNormal().y > 0 ? scratchMin.y : scratchMax.y;
            scratchVector2.y = p.getNormal().y > 0 ? scratchMax.y : scratchMin.y;
            scratchVector1.z = p.getNormal().z > 0 ? scratchMin.z : scratchMax.z;
            scratchVector2.z = p.getNormal().z > 0 ? scratchMax.z : scratchMin.z;

            // Negative point distance
            double distance1 = p.getDistanceTo(scratchVector1);
            // Positive point distance
            double distance2 = p.getDistanceTo(scratchVector2);

            // Is the positive vertex outside?
            if (distance2 < 0) {
                return Intersector.OUTSIDE;
            } else if (distance1 < 0) {
                result = Intersector.INTERSECT;
            }
        }
        return result;
    }

    /**
     * Performs an intersection test between this {@link Frustum} and the provided sphere as defined by its center
     * point and radius.
     *
     * @param center The {@link Vector3} center point of the sphere.
     * @param radius {@code double} The radius of the sphere.
     *
     * @return {@link Intersection} result.
     */
    @Intersection
    @SuppressWarnings("WeakerAccess")
    public int intersectSphere(@NonNull Vector3 center, double radius) {
        double distance;
        @Intersection int result = Intersector.INSIDE;
        for (int i = 0; i < 6; ++i) {
            distance = planes[i].getDistanceTo(center);
            if (distance < -radius) {
                return Intersector.OUTSIDE;
            } else if (distance < radius) {
                result = Intersector.INTERSECT;
            }
        }
        return result;
    }

    /**
     * Performs an intersection test between this {@link Frustum} and the provided {@link Vector3}.
     *
     * @param point The {@link Vector3} to test.
     * @return {@link Intersection} result.
     */
    @Intersection
    @SuppressWarnings("WeakerAccess")
    public int intersectPoint(Vector3 point) {
        double distance;
        for (int i = 0; i < 6; ++i) {
            distance = planes[i].getDistanceTo(point);
            if (distance < 0) {
                return Intersector.OUTSIDE;
            }
        }
        return Intersector.INSIDE;
    }
}
