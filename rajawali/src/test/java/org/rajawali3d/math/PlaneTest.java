package org.rajawali3d.math.vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PlaneTest {
    Plane plane;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void Construction() {
	plane = new Plane();
	assertNotNull(plane);

	plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
	assertNotNull(plane);
    }

    @Test
    public void SetComponents() {
	plane = new Plane();
	plane.setComponents(1,0,0,0);
    }

    @Test
    public void GetDistanceTo() {
	plane = new Plane();
	assertEquals(0, plane.getDistanceTo(Vector3.ZERO), 1e-14);

	plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
	assertEquals(1/Math.sqrt(3), plane.getDistanceTo(Vector3.ZERO), 1e-14);
    }

    @Test
    public void IsFrontFacing() {
	Vector3 inverted = Vector3.ONE.invertAndCreate();

	plane = new Plane();
	assertTrue(plane.isFrontFacing(Vector3.ONE));
	assertTrue(plane.isFrontFacing(inverted));

	plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
	assertTrue(plane.isFrontFacing(Vector3.ONE));
	assertFalse(plane.isFrontFacing(inverted));
    }

    @Test
    public void Normalize() {
	plane = new Plane();
	plane.normalize();
	assertEquals(Double.NaN, plane.getD(), 1e-14);
	assertEquals(Double.NaN, plane.getNormal().x, 1e-14);
	assertEquals(Double.NaN, plane.getNormal().y, 1e-14);
	assertEquals(Double.NaN, plane.getNormal().z, 1e-14);

	plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
	plane.normalize();
	assertEquals(1/Math.sqrt(3), plane.getD(), 1e-14);
	assertEquals(-1/Math.sqrt(3), plane.getNormal().x, 1e-14);
	assertEquals(-1/Math.sqrt(3), plane.getNormal().y, 1e-14);
	assertEquals(-1/Math.sqrt(3), plane.getNormal().z, 1e-14);
    }

    @Test
    public void GetNormal() {
	plane = new Plane();
	assertEquals(0, plane.getNormal().x, 1e-14);
	assertEquals(0, plane.getNormal().y, 1e-14);
	assertEquals(0, plane.getNormal().z, 1e-14);

	plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
	assertEquals(-1/Math.sqrt(3), plane.getNormal().x, 1e-14);
	assertEquals(-1/Math.sqrt(3), plane.getNormal().y, 1e-14);
	assertEquals(-1/Math.sqrt(3), plane.getNormal().z, 1e-14);
	assertNotNull(plane);
    }

    @Test
    public void GetD() {
	plane = new Plane();
	assertEquals(0, plane.getD(), 1e-14);

	plane = new Plane(Vector3.X, Vector3.Y, Vector3.Z);
	assertEquals(1/Math.sqrt(3), plane.getD(), 1e-14);
    }
}
