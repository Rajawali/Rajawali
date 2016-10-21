package c.org.rajawali3d.camera;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.support.test.filters.SmallTest;
import c.org.rajawali3d.bounds.AABB;
import org.junit.Test;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;
import c.org.rajawali3d.intersection.Intersector;
import c.org.rajawali3d.intersection.Intersector.Intersection;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class FrustumTest {

    @Test
    public void testUpdate() throws Exception {
        final Vector3[] corners = new Vector3[]{
                new Vector3(-1, 1, 1),
                new Vector3(1, 1, 1),
                new Vector3(-1, -1, 1),
                new Vector3(1, -1, 1),
                new Vector3(-1, 1, 120),
                new Vector3(1, 1, 120),
                new Vector3(-1, -1, 120),
                new Vector3(1, -1, 120),
        };
        final Frustum frustum = new Frustum();
        frustum.update(corners);
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
        assertEquals(-1d, planes[Frustum.NEAR].getDistanceToOrigin(), 1e-14);
        assertEquals(new Vector3(0d, 0d, -1d), planes[Frustum.FAR].getNormal());
        assertEquals(120d, planes[Frustum.FAR].getDistanceToOrigin(), 1e-14);
    }

    @Test
    public void testIntersectBoundsOrthographic() throws Exception {
        final Vector3[] corners = new Vector3[]{
                new Vector3(-1, 1, 1),
                new Vector3(1, 1, 1),
                new Vector3(-1, -1, 1),
                new Vector3(1, -1, 1),
                new Vector3(-1, 1, 120),
                new Vector3(1, 1, 120),
                new Vector3(-1, -1, 120),
                new Vector3(1, -1, 120),
                };
        final Frustum frustum = new Frustum();
        frustum.update(corners);

        final AABB inBox = mock(AABB.class);
        doReturn(new Vector3(0.5, 0.5, 3)).when(inBox).getMaxBound();
        doReturn(new Vector3(-0.5, -0.5, 2)).when(inBox).getMinBound();

        final AABB outBox = mock(AABB.class);
        doReturn(new Vector3(0.5, 0.5, 0)).when(outBox).getMaxBound();
        doReturn(new Vector3(-0.5, -0.5, -1)).when(outBox).getMinBound();

        final AABB intersectBox = mock(AABB.class);
        doReturn(new Vector3(0.5, 0.5, 5)).when(intersectBox).getMaxBound();
        doReturn(new Vector3(-0.5, -0.5, 0)).when(intersectBox).getMinBound();

        @Intersection int out = frustum.intersectBounds(inBox);
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectBounds(outBox);
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectBounds(intersectBox);
        assertEquals(Intersector.INTERSECT, out);
    }

    @Test
    public void testIntersectBoundsPerspective() throws Exception {
        final Vector3[] corners = new Vector3[]{
                new Vector3(-0.8284271247461901, 0.41421356237309503, 1.0),
                new Vector3(0.8284271247461901, 0.41421356237309503, 1.0),
                new Vector3(-0.8284271247461901, -0.41421356237309503, 1.0),
                new Vector3(0.8284271247461901, -0.41421356237309503, 1.0),
                new Vector3(-99.41125496954281, 49.705627484771405, 120.0),
                new Vector3(99.41125496954281, 49.705627484771405, 120.0),
                new Vector3(-99.41125496954281, -49.705627484771405, 120.0),
                new Vector3(99.41125496954281, -49.705627484771405, 120.0),
        };
        final Frustum frustum = new Frustum();
        frustum.update(corners);
        final AABB in = mock(AABB.class);
        doReturn(new Vector3(1, 1, 111)).when(in).getMaxBound();
        doReturn(new Vector3(-1, -1, 110)).when(in).getMinBound();
        @Intersection int out = frustum.intersectBounds(in);
        assertEquals(Intersector.INSIDE, out);
    }

    @Test
    public void testIntersectSphere() throws Exception {
        final Vector3[] corners = new Vector3[]{
                new Vector3(-1, 1, 1),
                new Vector3(1, 1, 1),
                new Vector3(-1, -1, 1),
                new Vector3(1, -1, 1),
                new Vector3(-1, 1, 120),
                new Vector3(1, 1, 120),
                new Vector3(-1, -1, 120),
                new Vector3(1, -1, 120),
                };
        final Frustum frustum = new Frustum();
        frustum.update(corners);
        @Intersection int out = frustum.intersectSphere(new Vector3(0, 0, 1.5), 0.25);
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectSphere(new Vector3(0, 0, 1), 2.0);
        assertEquals(Intersector.INTERSECT, out);
        out = frustum.intersectSphere(new Vector3(2, 2, 0.5), 0.1);
        assertEquals(Intersector.OUTSIDE, out);
    }

    @Test
    public void testIntersectPoint() throws Exception {
        final Vector3[] corners = new Vector3[]{
                new Vector3(-1, 1, 1),
                new Vector3(1, 1, 1),
                new Vector3(-1, -1, 1),
                new Vector3(1, -1, 1),
                new Vector3(-1, 1, 120),
                new Vector3(1, 1, 120),
                new Vector3(-1, -1, 120),
                new Vector3(1, -1, 120),
                };
        final Frustum frustum = new Frustum();
        frustum.update(corners);
        @Intersection int out = frustum.intersectPoint(new Vector3(0, 0, 1.5));
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 0, 1));
        assertEquals(Intersector.INSIDE, out);
        out = frustum.intersectPoint(new Vector3(2, 0, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(-2, 0, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 2, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, -2, 0));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 0, 122));
        assertEquals(Intersector.OUTSIDE, out);
        out = frustum.intersectPoint(new Vector3(0, 0, -2));
        assertEquals(Intersector.OUTSIDE, out);
    }
}