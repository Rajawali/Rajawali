package org.rajawali3d;

import static org.junit.Assert.*;
import org.junit.*;

import java.lang.Math;
import org.rajawali3d.ATransformable3D;

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
