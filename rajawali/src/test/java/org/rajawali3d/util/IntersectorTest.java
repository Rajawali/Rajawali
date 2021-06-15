package org.rajawali3d.util;

import org.junit.Test;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class IntersectorTest {

    @Test
    public void testIntersectRayPlaneHit() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Plane plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayStart.setAll(1,1,1);
        rayEnd.setAll(-1,-1,-1);
        result = Intersector.intersectRayPlane(rayStart, rayEnd, plane, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1/3d, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1/3d, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 1/3d, hitPoint.x, 1e-14);

        rayStart.setAll(1e3,1e3,1e3);
        rayEnd.setAll(-1e3,-1e3,-1e3);
        result = Intersector.intersectRayPlane(rayStart, rayEnd, plane, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1/3d, hitPoint.x, 1e-12);
        assertEquals("hitPoint.y", 1/3d, hitPoint.x, 1e-12);
        assertEquals("hitPoint.z", 1/3d, hitPoint.x, 1e-12);
    }

    @Test
    public void testIntersectRayPlaneMiss() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Plane plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayStart.setAll(1,1,1);
        rayEnd.setAll(2,2,2);
        result = Intersector.intersectRayPlane(rayStart, rayEnd, plane, hitPoint);
        assertFalse(result);
    }

    @Test
    public void testIntersectRayTriangleHit() {
        Vector3 rayStart = new Vector3(0, 0, 0);
        Vector3 rayEnd = new Vector3(1, 1, 1);
        Vector3 t1 = new Vector3(1, 0, 0);
        Vector3 t2 = new Vector3(0, 1, 0);
        Vector3 t3 = new Vector3(0, 0, 1);
        Vector3 hitPoint = new Vector3();
        Boolean result;

        // (1,1,1) . (1/3,1/3,1/3) = 0,  x+y+z=1
        result = Intersector.intersectRayTriangle(rayStart, rayEnd, t1, t2, t3, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1.0 / 3.0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1.0 / 3.0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 1.0 / 3.0, hitPoint.x, 1e-14);
    }

    @Test
    public void testIntersectRayTriangleMiss() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Vector3 t1 = new Vector3(0,0,-1);
        Vector3 t2 = new Vector3(-1,0,1);
        Vector3 t3 = new Vector3(1,0,1);
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayStart.setAll(1,1,1);
        rayEnd.setAll(2,2,2);
        result = Intersector.intersectRayTriangle(rayStart, rayEnd, t1, t2, t3, hitPoint);
        assertFalse(result);
    }

    @Test
    public void testIntersectRaySphereInside() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Vector3 sphereCentre = new Vector3();
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayEnd.setAll(1, 1, 1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1/Math.sqrt(3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1/Math.sqrt(3), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 1/Math.sqrt(3), hitPoint.z, 1e-14);

        rayEnd.setAll(-1, -1, -1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -1/Math.sqrt(3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", -1/Math.sqrt(3), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", -1/Math.sqrt(3), hitPoint.z, 1e-14);

        rayEnd.setAll(1e2, 1e2, 1e2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1/Math.sqrt(3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1/Math.sqrt(3), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 1/Math.sqrt(3), hitPoint.z, 1e-14);

        rayEnd.setAll(-1e2, -1e2, -1e2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -1/Math.sqrt(3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", -1/Math.sqrt(3), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", -1/Math.sqrt(3), hitPoint.z, 1e-14);
    }

    @Test
    public void testIntersectRaySphereAxis() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Vector3 sphereCentre = new Vector3();
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayStart.setAll(3,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(-3,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -1, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(0,3,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(0,-3,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", -1, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(0,0,3);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 1, hitPoint.z, 1e-14);

        rayStart.setAll(0,0,-3);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", -1, hitPoint.z, 1e-14);

        sphereRadius = 1e3;
        rayStart.setAll(3e3,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1e3, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        sphereRadius = 1e3;
        rayStart.setAll(-3e3,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -1e3, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        sphereRadius = 1e3;
        rayStart.setAll(0,3e3,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1e3, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);
    }

    @Test
    public void testIntersectRaySphereDiagonal() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Vector3 sphereCentre = new Vector3();
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayStart.setAll(2,2,0);
        rayEnd.setAll(-2,-2,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1/Math.sqrt(2), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1/Math.sqrt(2), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(2,0,2);
        rayEnd.setAll(-2,0,-2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1/Math.sqrt(2), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 1/Math.sqrt(2), hitPoint.z, 1e-14);

        rayStart.setAll(0,2,2);
        rayEnd.setAll(0,-2,-2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1/Math.sqrt(2), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 1/Math.sqrt(2), hitPoint.z, 1e-14);

        rayStart.setAll(-2,-2,0);
        rayEnd.setAll(2,2,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -1/Math.sqrt(2), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", -1/Math.sqrt(2), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(-2,0,-2);
        rayEnd.setAll(2,0,2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -1/Math.sqrt(2), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", -1/Math.sqrt(2), hitPoint.z, 1e-14);

        rayStart.setAll(0,-2,-2);
        rayEnd.setAll(0,2,2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", -1/Math.sqrt(2), hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", -1/Math.sqrt(2), hitPoint.z, 1e-14);

        sphereRadius = 1e3;
        rayStart.setAll(2e3,2e3,0);
        rayEnd.setAll(-2e3,-2e3,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1e3/Math.sqrt(2), hitPoint.x, 1e-12);
        assertEquals("hitPoint.y", 1e3/Math.sqrt(2), hitPoint.y, 1e-12);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-12);

        sphereRadius = 1e3;
        rayStart.setAll(2e3,0,2e3);
        rayEnd.setAll(-2e3,0,-2e3);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1e3/Math.sqrt(2), hitPoint.x, 1e-12);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-12);
        assertEquals("hitPoint.z", 1e3/Math.sqrt(2), hitPoint.z, 1e-12);

        sphereRadius = 1e3;
        rayStart.setAll(0,2e3,2e3);
        rayEnd.setAll(0,-2e3,-2e3);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-12);
        assertEquals("hitPoint.y", 1e3/Math.sqrt(2), hitPoint.y, 1e-12);
        assertEquals("hitPoint.z", 1e3/Math.sqrt(2), hitPoint.z, 1e-12);
    }

    @Test
    public void testIntersectRaySphereTangent() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Vector3 sphereCentre = new Vector3();
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayStart.setAll(1,0,0);
        rayEnd.setAll(-1,0,0);
        sphereCentre.setAll(0,1,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(0,1,0);
        rayEnd.setAll(0,-1,0);
        sphereCentre.setAll(0,0,1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(0,0,1);
        rayEnd.setAll(0,0,-1);
        sphereCentre.setAll(1,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(1,0,0);
        rayEnd.setAll(-1,0,0);
        sphereCentre.setAll(0,-1,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(0,1,0);
        rayEnd.setAll(0,-1,0);
        sphereCentre.setAll(0,0,-1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);

        rayStart.setAll(0,0,1);
        rayEnd.setAll(0,0,-1);
        sphereCentre.setAll(-1,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.y, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.z, 1e-14);
    }

    @Test
    public void testIntersectRaySphereMiss() {
        Vector3 rayStart = new Vector3();
        Vector3 rayEnd = new Vector3();
        Vector3 sphereCentre = new Vector3();
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();
        Boolean result;

        rayStart.setAll(-1,1,1);
        rayEnd.setAll(1,1,1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertFalse(result);

        rayStart.setAll(-1/3d,-1/3d,-1/3d);
        rayEnd.setAll(1/3d,1/3d,1/3d);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertFalse(result);

        rayStart.setAll(-1,1,-1);
        rayEnd.setAll(1,1,-1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertFalse(result);

        rayStart.setAll(1/3d,1/3d,1/3d);
        rayEnd.setAll(-1/3d,-1/3d,-1/3d);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertFalse(result);
    }

}
