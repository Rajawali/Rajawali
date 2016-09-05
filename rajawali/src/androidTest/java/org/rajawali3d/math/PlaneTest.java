package org.rajawali3d.math;

import static org.junit.Assert.*;

import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class PlaneTest {

    @Test
    public void testConstructorNoArgs() throws Exception {
        final Plane plane = new Plane();
        assertNotNull(plane);
        assertEquals(Vector3.ZERO, plane.normal);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(plane.distanceToOrigin));
    }

    @Test
    public void testConstructorPoints() throws Exception {
        Plane plane = new Plane(new Vector3(1, 0, 0), new Vector3(1, 0, 1), new Vector3(0, 0, 1));
        assertNotNull(plane);
        assertEquals(Vector3.NEG_Y, plane.normal);
        assertEquals(0d, plane.distanceToOrigin, 1e-14);

        plane = new Plane(new Vector3(0, 1, 1), new Vector3(0, 1, 0), new Vector3(0, 0, 1));
        assertNotNull(plane);
        assertEquals(Vector3.NEG_X, plane.normal);
        assertEquals(0d, plane.distanceToOrigin, 1e-14);

        plane = new Plane(new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(0, 1, 0));
        assertNotNull(plane);
        assertEquals(Vector3.Z, plane.normal);
        assertEquals(0d, plane.distanceToOrigin, 1e-14);

        plane = new Plane(new Vector3(1, 0, 2), new Vector3(1, 1, 2), new Vector3(0, 1, 2));
        assertNotNull(plane);
        assertEquals(Vector3.Z, plane.normal);
        assertEquals(-2d, plane.distanceToOrigin, 1e-14);
    }

    @Test
    public void testSet() throws Exception {
        final Plane plane = new Plane();
        plane.set(new Vector3(1, 0, 2), new Vector3(1, 1, 2), new Vector3(0, 1, 2));
        assertEquals(Vector3.Z, plane.normal);
        assertEquals(-2d, plane.distanceToOrigin, 1e-14);
    }

    @Test
    public void testSetComponents() throws Exception {
        final Plane plane = new Plane();
        plane.setComponents(0d, 0d, 1d, -2d);
        assertEquals(Vector3.Z, plane.normal);
        assertEquals(-2d, plane.distanceToOrigin, 1e-14);
    }

    @Test
    public void testGetDistanceTo() throws Exception {
        final Plane plane = new Plane(new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(0, 1, 0));
        double out = plane.getDistanceTo(new Vector3(0, 0, 4));
        assertEquals(4d, out, 1e-14);

        out = plane.getDistanceTo(new Vector3(1, 1, 4));
        assertEquals(4d, out, 1e-14);
    }

    @Test
    public void testGetNormal() throws Exception {
        final Plane plane = new Plane();
        plane.set(new Vector3(1, 0, 2), new Vector3(1, 1, 2), new Vector3(0, 1, 2));
        assertEquals(Vector3.Z, plane.getNormal());
    }

    @Test
    public void testGetDistanceToOrigin() throws Exception {
        final Plane plane = new Plane();
        plane.set(new Vector3(1, 0, 2), new Vector3(1, 1, 2), new Vector3(0, 1, 2));
        assertEquals(-2d, plane.getDistanceToOrigin(), 1e-14);
    }

    @Test
    public void testGetPointSide() throws Exception {
        final Plane plane = new Plane(new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(0, 1, 0));
        int out = plane.getPointSide(new Vector3(0, 0, 4));
        assertEquals(Plane.FRONT_OF_PLANE, out);

        out = plane.getPointSide(new Vector3(0, 0, 0));
        assertEquals(Plane.ON_PLANE, out);

        out = plane.getPointSide(new Vector3(0, 0, -4));
        assertEquals(Plane.BACK_OF_PLANE, out);
    }

    @Test
    public void testIsFrontFacing() throws Exception {
        final Plane plane = new Plane(new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(0, 1, 0));
        assertTrue(plane.isFrontFacing(Vector3.Z));
        assertFalse(plane.isFrontFacing(Vector3.NEG_Z));
        assertTrue(plane.isFrontFacing(new Vector3(1, 0, 1)));
        assertFalse(plane.isFrontFacing(new Vector3(1, 0, -1)));
    }

    @Test
    public void testNormalize() throws Exception {
        final Plane plane = new Plane();
        plane.normal.setAll(0, 0, 2);
        plane.distanceToOrigin = -4d;
        plane.normalize();
        assertEquals(Vector3.Z, plane.normal);
        assertEquals(-2d, plane.distanceToOrigin, 1e-14);
    }
}