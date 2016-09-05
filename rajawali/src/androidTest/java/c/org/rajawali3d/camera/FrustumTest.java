package c.org.rajawali3d.camera;

import static org.junit.Assert.*;

import android.support.annotation.NonNull;
import c.org.rajawali3d.bounds.AABB;
import org.junit.Test;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.Intersector;
import org.rajawali3d.util.Intersector.Bounded;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class FrustumTest {

    final AABB inBox = new AABB() {
        @NonNull @Override public Vector3 getMaxBound() {
            return new Vector3(0.5d, 0.5d, 0.5d);
        }

        @NonNull @Override public Vector3 getMinBound() {
            return new Vector3(-0.5d, -0.5d, -0.5d);
        }

        @Override public void recalculateBounds(boolean recursive) {

        }

        @Override public void recalculateBoundsForAdd(@NonNull AABB added) {

        }
    };

    final AABB outBox = new AABB() {
        @NonNull @Override public Vector3 getMaxBound() {
            return new Vector3(3d, 3d, 3d);
        }

        @NonNull @Override public Vector3 getMinBound() {
            return new Vector3(2d, 2d, 2d);
        }

        @Override public void recalculateBounds(boolean recursive) {

        }

        @Override public void recalculateBoundsForAdd(@NonNull AABB added) {

        }
    };

    final AABB intersectBox = new AABB() {
        @NonNull @Override public Vector3 getMaxBound() {
            return new Vector3(3d, 3d, 3d);
        }

        @NonNull @Override public Vector3 getMinBound() {
            return new Vector3(-3d, -3d, -3d);
        }

        @Override public void recalculateBounds(boolean recursive) {

        }

        @Override public void recalculateBoundsForAdd(@NonNull AABB added) {

        }
    };

    @Test
    public void testUpdate() throws Exception {
        final Frustum frustum = new Frustum();
        final Matrix4 projection = new Matrix4();
        projection.setToOrthographic(1, -1, -1, 1, 0, 1);
        projection.inverse();
        frustum.update(projection);
        Plane[] planes = frustum.planes;
        assertEquals(new Vector3(1d, 0d, 0d), planes[Frustum.LEFT].getNormal());
        assertEquals(1d, planes[Frustum.LEFT].getDistanceToOrigin(), 1e-14);
        assertEquals(new Vector3(-1d, 0d, 0d), planes[Frustum.RIGHT].getNormal());
        assertEquals(1d, planes[Frustum.RIGHT].getDistanceToOrigin(), 1e-14);
        assertEquals(new Vector3(0d, 1d, 0d), planes[Frustum.BOTTOM].getNormal());
        assertEquals(1d, planes[Frustum.BOTTOM].getDistanceToOrigin(), 1e-14);
        assertEquals(new Vector3(0d, -1d, 0d), planes[Frustum.TOP].getNormal());
        assertEquals(1d, planes[Frustum.TOP].getDistanceToOrigin(), 1e-14);
        assertEquals(new Vector3(0d, 0d, 1d), planes[Frustum.NEAR].getNormal());
        assertEquals(1.5d, planes[Frustum.NEAR].getDistanceToOrigin(), 1e-14);
        assertEquals(new Vector3(0d, 0d, -1d), planes[Frustum.FAR].getNormal());
        assertEquals(0.5d, planes[Frustum.FAR].getDistanceToOrigin(), 1e-14);
    }

    @Test
    public void testIntersectBounds() throws Exception {
        final Frustum frustum = new Frustum();
        final Matrix4 projection = new Matrix4();
        projection.setToOrthographic(1, -1, -1, 1, 0, 1);
        projection.inverse();
        frustum.update(projection);
        @Bounded int out = frustum.intersectBounds(inBox);
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectBounds(outBox);
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectBounds(intersectBox);
        assertEquals(Intersector.INTERSECT, out);
    }

    @Test
    public void testIntersectSphere() throws Exception {
        final Frustum frustum = new Frustum();
        final Matrix4 projection = new Matrix4();
        projection.setToOrthographic(1, -1, -1, 1, 0, 1);
        projection.inverse();
        frustum.update(projection);
        @Bounded int out = frustum.intersectSphere(new Vector3(0, 0, 0.5), 0.25);
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectSphere(new Vector3(0, 0, 0.5), 2.0);
        assertEquals(Intersector.INTERSECT, out);
        out = frustum.intersectSphere(new Vector3(2, 2, 0.5), 0.1);
        assertEquals(Intersector.OUTSIDE, out);
    }

    @Test
    public void testIntersectPoint() throws Exception {
        final Frustum frustum = new Frustum();
        final Matrix4 projection = new Matrix4();
        projection.setToOrthographic(1, -1, -1, 1, 0, 1);
        projection.inverse();
        frustum.update(projection);
        @Bounded int out = frustum.intersectPoint(new Vector3(0, 0, 0.5));
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 0, 0));
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectPoint(new Vector3(2, 0, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(-2, 0, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 2, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, -2, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 0, 2));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 0, -2));
        assertEquals(Intersector.OUTSIDE, out);
    }
}