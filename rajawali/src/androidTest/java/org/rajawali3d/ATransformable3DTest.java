package org.rajawali3d;

import static org.junit.Assert.*;
import org.junit.*;

import java.lang.Math;
import org.rajawali3d.ATransformable3D;
import org.rajawali3d.math.vector.Vector3;

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
        assertEquals(Math.toRadians(expected), transformable.getRotZ(), 1e-10);
    }

    @Test
    public void testSetRotY() throws Exception {
        double expected = 42;
        transformable.setRotY(expected);
        assertEquals(Math.toRadians(expected), transformable.getRotY(), 1e-10);
    }

    @Test
    public void testSetRotZ() throws Exception {
        double expected = 42;
        transformable.setRotZ(expected);
        assertEquals(Math.toRadians(expected), transformable.getRotX(), 1e-10);
    }
}
