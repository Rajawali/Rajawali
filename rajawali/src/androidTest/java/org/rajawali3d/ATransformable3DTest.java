package org.rajawali3d;

import static org.junit.Assert.*;
import org.junit.*;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.Quaternion;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class ATransformable3DTest {
    transformable3D transformable;

    class transformable3D extends ATransformable3D {
    }

    @Before
    public void setup() throws Exception {
        transformable = new transformable3D();
    }

    @After
    public void teardown() throws Exception {
        transformable = null;
    }

    @Test
    public void testGetScale() throws Exception {
        double expected = 1;
        Vector3 scale = transformable.getScale();
        assertEquals(expected, scale.x, 1e-10);
        assertEquals(expected, scale.y, 1e-10);
        assertEquals(expected, scale.z, 1e-10);
    }

    @Test
    public void testGetLookAt() throws Exception {
        double expected = 0;
        Vector3 rotation = transformable.getLookAt();
        assertEquals(expected, rotation.x, 1e-10);
        assertEquals(expected, rotation.y, 1e-10);
        assertEquals(expected, rotation.z, 1e-10);
    }

    @Test
    public void testGetRotX() throws Exception {
        double expected = 0;
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testGetRotY() throws Exception {
        double expected = 0;
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testGetRotZ() throws Exception {
        double expected = 0;
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }

    @Test
    public void testGetOrientation() throws Exception {
        Quaternion result = transformable.getOrientation();
        assertEquals(1, result.w, 1e-10);
        assertEquals(0, result.x, 1e-10);
        assertEquals(0, result.y, 1e-10);
        assertEquals(0, result.z, 1e-10);
    }

    @Test
    public void testSetScale() throws Exception {
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
    public void testSetLookAt() throws Exception {
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
    public void testSetRotX() throws Exception {
        double expected = 42;
        transformable.setRotX(expected);
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }

    @Test
    public void testSetRotY() throws Exception {
        double expected = 42;
        transformable.setRotY(expected);
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testSetRotZ() throws Exception {
        double expected = 42;
        transformable.setRotZ(expected);
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testSetOrientation() throws Exception {
        double w = 1/2d;
        double x = Math.sqrt(3)/4d;
        double y = 1/2d;
        double z = Math.sqrt(5)/4d;
        Quaternion expected = new Quaternion(w,x,y,z);
        transformable.setOrientation(expected);
        Quaternion result = transformable.getOrientation();
        assertEquals(expected.w, result.w, 1e-10);
        assertEquals(expected.x, result.x, 1e-10);
        assertEquals(expected.y, result.y, 1e-10);
        assertEquals(expected.z, result.z, 1e-10);
    }

    @Test
    public void testRotateX() throws Exception {
        double expected = 42;
        transformable.rotate(Vector3.Axis.X, expected);
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testRotateY() throws Exception {
        double expected = 42;
        transformable.rotate(Vector3.Axis.Y, expected);
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testRotateZ() throws Exception {
        double expected = 42;
        transformable.rotate(Vector3.Axis.Z, expected);
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }

    @Test
    public void testRotateAroundX() throws Exception {
        double expected = 42;
        transformable.rotate(Vector3.X, expected);
        assertEquals(expected, transformable.getRotX(), 1e-10);
    }

    @Test
    public void testRotateAroundY() throws Exception {
        double expected = 42;
        transformable.rotate(Vector3.Y, expected);
        assertEquals(expected, transformable.getRotY(), 1e-10);
    }

    @Test
    public void testRotateAroundZ() throws Exception {
        double expected = 42;
        transformable.rotate(Vector3.Z, expected);
        assertEquals(expected, transformable.getRotZ(), 1e-10);
    }
}
