package org.rajawali3d.curves;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.*;

public class OpenCatmullRomTest {
    CatmullRomCurve3D curve;

    @Before
    public void setup() {
	curve = new CatmullRomCurve3D();
        assertNotNull(curve);

	curve.addPoint(Vector3.NEG_Y);
	curve.addPoint(Vector3.NEG_X);
	curve.addPoint(Vector3.Y);
	curve.addPoint(Vector3.X);
	curve.addPoint(Vector3.NEG_Y);
        curve.isClosedCurve(false);
        assertFalse(curve.isClosedCurve());
    }

    @After
    public void teardown() {
        curve = null;
    }

    @Test
    public void testGetNumPoints() {
	assertEquals(5,curve.getNumPoints());
    }

    @Test
    public void testSelectPoint() {
        assertEquals(0, curve.selectPoint(Vector3.NEG_Y));
        assertEquals(1, curve.selectPoint(Vector3.NEG_X));
        assertEquals(2, curve.selectPoint(Vector3.Y));
        assertEquals(3, curve.selectPoint(Vector3.X));
    }

    @Test
    public void testSetCalculateTangents() {
        curve.setCalculateTangents(true);
        curve.setCalculateTangents(false);
    }

    @Test
    public void testGetCurrentTangent() {
        double t;
        Vector3 result = new Vector3();

        curve.setCalculateTangents(false);
        for(t=0; t<1; t+=1/8d) {
            curve.calculatePoint(result,t);
      	    assertEquals("B(" + t + ").x", 0,curve.getCurrentTangent().x,1e-10);
      	    assertEquals("B(" + t + ").y", 0,curve.getCurrentTangent().y,1e-10);
      	    assertEquals("B(" + t + ").z", 0,curve.getCurrentTangent().z,1e-10);
        }

        t = 0/4d;
        curve.setCalculateTangents(true);
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", 0, curve.getCurrentTangent().x, 1e-10);
        assertEquals("B(" + t + ").y", 0, curve.getCurrentTangent().y, 1e-10);
        assertEquals("B(" + t + ").z", 0, curve.getCurrentTangent().z, 1e-10);

        t = 1/4d;
        curve.setCalculateTangents(true);
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", -Math.sqrt(1/2d), curve.getCurrentTangent().x, 1e-10);
        assertEquals("B(" + t + ").y", -Math.sqrt(1/2d), curve.getCurrentTangent().y, 1e-10);
        assertEquals("B(" + t + ").z", 0, curve.getCurrentTangent().z, 1e-10);

        t = 2/4d;
        curve.setCalculateTangents(true);
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", -1.0, curve.getCurrentTangent().x, 1e-10);
        assertEquals("B(" + t + ").y", 0, curve.getCurrentTangent().y, 1e-10);
        assertEquals("B(" + t + ").z", 0, curve.getCurrentTangent().z, 1e-10);

        t = 3/4d;
        curve.setCalculateTangents(true);
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", -Math.sqrt(1/2d), curve.getCurrentTangent().x, 1e-10);
        assertEquals("B(" + t + ").y", Math.sqrt(1/2d), curve.getCurrentTangent().y, 1e-10);
        assertEquals("B(" + t + ").z", 0, curve.getCurrentTangent().z, 1e-10);

        t = 4/4d;
        curve.setCalculateTangents(true);
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", 0, curve.getCurrentTangent().x, 1e-10);
        assertEquals("B(" + t + ").y", 0, curve.getCurrentTangent().y, 1e-10);
        assertEquals("B(" + t + ").z", 0, curve.getCurrentTangent().z, 1e-10);
    }

    @Test
    public void testCalculatePoint() {
        double t;
        Vector3 result = new Vector3();

        t = 0/4d;
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", -1, result.x, 1e-10);
        assertEquals("B(" + t + ").y", 0, result.y, 1e-10);
        assertEquals("B(" + t + ").z", 0, result.z, 1e-10);

        t = 1/4d;
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", -0.625, result.x, 1e-10);
        assertEquals("B(" + t + ").y", 0.625, result.y, 1e-10);
        assertEquals("B(" + t + ").z", 0, result.z, 1e-10);

        t = 2/4d;
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", 0, result.x, 1e-10);
        assertEquals("B(" + t + ").y", 1, result.y, 1e-10);
        assertEquals("B(" + t + ").z", 0, result.z, 1e-10);

        t = 3/4d;
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", 0.625, result.x, 1e-10);
        assertEquals("B(" + t + ").y", 0.625, result.y, 1e-10);
        assertEquals("B(" + t + ").z", 0, result.z, 1e-10);

        t = 4/4d;
        curve.calculatePoint(result,t);
        assertEquals("B(" + t + ").x", 1, result.x, 1e-10);
        assertEquals("B(" + t + ").y", 0, result.y, 1e-10);
        assertEquals("B(" + t + ").z", 0, result.z, 1e-10);
    }

    @Test
    public void testCalculateTangent() {
    }

}
