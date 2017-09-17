package org.rajawali3d;

import static org.junit.Assert.*;
import org.junit.*;

import org.rajawali3d.Geometry3D;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class Geometry3Dtest {
    Geometry3D geometry;

    @Before
    public void setup() throws Exception {
        geometry = new Geometry3D();
    }

    @After
    public void teardown() throws Exception {
        geometry = null;
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(geometry);
    }

    @Test
    public void testGetNumIndices() throws Exception {
        assertEquals(0, geometry.getNumIndices());
    }

    @Test
    public void testGetNumTriangles() throws Exception {
        assertEquals(0, geometry.getNumTriangles());
    }

    @Test
    public void testGetNumVertices() throws Exception {
        assertEquals(0, geometry.getNumVertices());
    }

}
