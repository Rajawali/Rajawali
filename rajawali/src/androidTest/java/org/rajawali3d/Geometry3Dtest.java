package org.rajawali3d;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
