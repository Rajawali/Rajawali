package org.rajawali3d;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertEquals;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class ATransformable3DTest {

    private Transformable3D transformable;

    @Before
    public void setup() {
        transformable = new Transformable3D();
    }

    @After
    public void teardown() {
        transformable = null;
    }

    @Test
    public void testGetScale() {
        double expected = 1;
        Vector3 scale = transformable.getScale();
        assertEquals(expected, scale.x, 1e-10);
        assertEquals(expected, scale.y, 1e-10);
        assertEquals(expected, scale.z, 1e-10);
    }

    @Test
    public void testGetLookAt() {
        double expected = 0;
        Vector3 rotation = transformable.getLookAt();
        assertEquals(expected, rotation.x, 1e-10);
        assertEquals(expected, rotation.y, 1e-10);
        assertEquals(expected, rotation.z, 1e-10);
    }

    @Test
    public void testGetRotX() {
        double expected = 0;
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testGetRotY() {
        double expected = 0;
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testGetRotZ() {
        double expected = 0;
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }

    @Test
    public void testGetOrientation() {
        Quaternion result = transformable.getOrientation();
        assertEquals(1, result.w, 1e-10);
        assertEquals(0, result.x, 1e-10);
        assertEquals(0, result.y, 1e-10);
        assertEquals(0, result.z, 1e-10);
    }

    @Test
    public void testSetScale() {
        double expectedX = 60;
        double expectedY = 120;
        double expectedZ = 180;
        transformable.setScale(expectedX, expectedY, expectedZ);
        Vector3 scale = transformable.getScale();
        assertEquals(expectedX, scale.x, 1e-10);
        assertEquals(expectedY, scale.y, 1e-10);
        assertEquals(expectedZ, scale.z, 1e-10);
    }

    @Test
    public void testSetLookAt() {
        double expectedX = 60;
        double expectedY = 120;
        double expectedZ = 180;
        transformable.setLookAt(expectedX, expectedY, expectedZ);
        Vector3 rotation = transformable.getLookAt();
        assertEquals(expectedX, rotation.x, 1e-10);
        assertEquals(expectedY, rotation.y, 1e-10);
        assertEquals(expectedZ, rotation.z, 1e-10);
    }

    @Test
    public void testSetRotX() {
        double expected = 42;
        transformable.setRotX(expected);
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }

    @Test
    public void testSetRotY() {
        double expected = 42;
        transformable.setRotY(expected);
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testSetRotZ() {
        double expected = 42;
        transformable.setRotZ(expected);
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testSetOrientation() {
        double w = 1 / 2d;
        double x = Math.sqrt(3) / 4d;
        double y = 1 / 2d;
        double z = Math.sqrt(5) / 4d;
        Quaternion expected = new Quaternion(w, x, y, z);
        transformable.setOrientation(expected);
        Quaternion result = transformable.getOrientation();
        assertEquals(expected.w, result.w, 1e-10);
        assertEquals(expected.x, result.x, 1e-10);
        assertEquals(expected.y, result.y, 1e-10);
        assertEquals(expected.z, result.z, 1e-10);
    }

    @Test
    public void testRotateX() {
        double expected = 42;
        transformable.rotate(Vector3.Axis.X, expected);
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testRotateY() {
        double expected = 42;
        transformable.rotate(Vector3.Axis.Y, expected);
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testRotateZ() {
        double expected = 42;
        transformable.rotate(Vector3.Axis.Z, expected);
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }

    @Test
    public void testRotateAroundX() {
        double expected = 42;
        transformable.rotate(Vector3.X, expected);
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testRotateAroundY() {
        double expected = 42;
        transformable.rotate(Vector3.Y, expected);
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testRotateAroundZ() {
        double expected = 42;
        transformable.rotate(Vector3.Z, expected);
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }
}
