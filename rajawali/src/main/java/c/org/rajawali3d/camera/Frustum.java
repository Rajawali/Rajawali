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
import c.org.rajawali3d.bounds.AABB;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.Intersector;
import org.rajawali3d.util.Intersector.Bounded;

/**
 * Defines a camera viewing frustum in world space coordinates. This is used primarily for view frustum culling but
 * can also be used to easily show camera frustum debug geometry.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Frustum {

    @IntDef({ LEFT, RIGHT, BOTTOM, TOP, NEAR, FAR })
    protected  @interface FrustumPlanes {}
    public static final int LEFT = 0;
    public static final int RIGHT       = 1;
    public static final int BOTTOM  = 2;
    public static final int TOP = 3;
    public static final int NEAR = 4;
    public static final int FAR = 5;

    @NonNull
    protected final Plane[] planes;

    @NonNull
    private final Vector3 scratchMin;

    @NonNull
    private final Vector3 scratchMax;

    @NonNull
    private final Vector3 scratchVector1;

    @NonNull
    private final Vector3 scratchVector2;

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

    public void update(@NonNull Matrix4 inverseProjectionView) {
        double[] m = inverseProjectionView.getDoubleValues();

        planes[LEFT].setComponents(m[Matrix4.M30] - m[Matrix4.M00], m[Matrix4.M31] - m[Matrix4.M01],
                                m[Matrix4.M32] - m[Matrix4.M02], m[Matrix4.M33] - m[Matrix4.M03]);
        planes[RIGHT].setComponents(m[Matrix4.M30] + m[Matrix4.M00], m[Matrix4.M31] + m[Matrix4.M01],
                                m[Matrix4.M32] + m[Matrix4.M02], m[Matrix4.M33] + m[Matrix4.M03]);
        planes[BOTTOM].setComponents(m[Matrix4.M30] + m[Matrix4.M10], m[Matrix4.M31] + m[Matrix4.M11],
                                m[Matrix4.M32] + m[Matrix4.M12], m[Matrix4.M33] + m[Matrix4.M13]);
        planes[TOP].setComponents(m[Matrix4.M30] - m[Matrix4.M10], m[Matrix4.M31] - m[Matrix4.M11],
                                m[Matrix4.M32] - m[Matrix4.M12], m[Matrix4.M33] - m[Matrix4.M13]);
        planes[NEAR].setComponents(m[Matrix4.M30] - m[Matrix4.M20], m[Matrix4.M31] - m[Matrix4.M21],
                                m[Matrix4.M32] - m[Matrix4.M22], m[Matrix4.M33] - m[Matrix4.M23]);
        planes[FAR].setComponents(m[Matrix4.M30] + m[Matrix4.M20], m[Matrix4.M31] + m[Matrix4.M21],
                                m[Matrix4.M32] + m[Matrix4.M22], m[Matrix4.M33] + m[Matrix4.M23]);

        planes[LEFT].normalize();
        planes[RIGHT].normalize();
        planes[BOTTOM].normalize();
        planes[TOP].normalize();
        planes[NEAR].normalize();
        planes[FAR].normalize();
    }

    @Bounded
    public int intersectBounds(@NonNull AABB bounds) {
        scratchMin.setAll(bounds.getMinBound());
        scratchMax.setAll(bounds.getMaxBound());
        @Bounded int result = Intersector.INSIDE;
        // Use scratchVector1 as negative bound, scratchVector2 as positive
        for (int i = 0; i < 6; i++) {
            Plane p = planes[i];
            scratchVector1.x = p.getNormal().x > 0 ? scratchMin.x : scratchMax.x;
            scratchVector2.x = p.getNormal().x > 0 ? scratchMax.x : scratchMin.x;
            scratchVector1.y = p.getNormal().y > 0 ? scratchMin.y : scratchMax.y;
            scratchVector2.y = p.getNormal().y > 0 ? scratchMax.y : scratchMin.y;
            scratchVector1.z = p.getNormal().z > 0 ? scratchMin.z : scratchMax.z;
            scratchVector2.z = p.getNormal().z > 0 ? scratchMax.z : scratchMin.z;

            double distance1 = p.getDistanceTo(scratchVector1);
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

    @Bounded
    public int intersectSphere(@NonNull Vector3 center, double radius) {
        double distance;
        @Bounded int result = Intersector.INSIDE;
        for (int i = 0; i < 6; ++i) {
            distance = planes[i].getDistanceTo(center);
            if (distance < -radius) {
                return Intersector.OUTSIDE;
            } else if (distance == radius) {
                result = Intersector.INTERSECT;
            }
        }
        return result;
    }

    @Bounded
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
