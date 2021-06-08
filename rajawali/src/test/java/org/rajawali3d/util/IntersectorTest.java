package org.rajawali3d.util;

import org.junit.Test;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class IntersectorTest {

    @Test
    public void testIntersectRayPlane() {
        Vector3 rayStart = new Vector3(0, 0, 0);
        Vector3 rayEnd = new Vector3(1, 1, 1);
        Plane plane = new Plane();
        Vector3 hitPoint = new Vector3();
        Boolean result;

        result = Intersector.intersectRayPlane(rayStart, rayEnd, plane, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);
    }

    @Test
    public void testIntersectRayTriangle() {
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
        assertEquals("hitPoint.x", Math.sqrt(1/3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", Math.sqrt(1/3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", Math.sqrt(1/3), hitPoint.x, 1e-14);

        rayEnd.setAll(-1, -1, -1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -Math.sqrt(1/3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", -Math.sqrt(1/3), hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", -Math.sqrt(1/3), hitPoint.x, 1e-14);
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
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);

        rayStart.setAll(-3,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", -1, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);

        rayStart.setAll(0,3,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);

        rayStart.setAll(0,-3,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", -1, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);

        rayStart.setAll(0,0,3);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 1, hitPoint.x, 1e-14);

        rayStart.setAll(0,0,-3);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", -1, hitPoint.x, 1e-14);
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
        assertEquals("hitPoint.x", Math.sqrt(2), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", Math.sqrt(2), hitPoint.x, 1e-14);

        rayStart.setAll(2,0,2);
        rayEnd.setAll(-2,0,-2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", Math.sqrt(2), hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", Math.sqrt(2), hitPoint.x, 1e-14);

        rayStart.setAll(0,2,2);
        rayEnd.setAll(0,-2,-2);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", Math.sqrt(2), hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", Math.sqrt(2), hitPoint.x, 1e-14);
    }

    @Test
    public void testIntersectRaySphereTangent() {
        Vector3 rayStart = new Vector3(2,0,0);
        Vector3 rayEnd = new Vector3(-2,0,0);
        Vector3 sphereCentre = new Vector3();
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();
        Boolean result;

        sphereCentre.setAll(1,0,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);

        sphereCentre.setAll(0,1,0);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);

        sphereCentre.setAll(0,0,1);
        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);
    }

    @Test
    public void testIntersectRaySphereMiss() {
        Vector3 rayStart = new Vector3(-1,1,1);
        Vector3 rayEnd = new Vector3(1,1,1);
        Vector3 sphereCentre = new Vector3();
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();
        Boolean result;

        result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertFalse(result);
    }

}
