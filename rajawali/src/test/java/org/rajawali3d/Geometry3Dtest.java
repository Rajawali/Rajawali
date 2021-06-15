package org.rajawali3d;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class Geometry3Dtest {

    private Geometry3D geometry;

    @Before
    public void setup() {
        geometry = new Geometry3D();
    }

    @After
    public void teardown() {
        geometry = null;
    }

    @Test
    public void testConstructor() {
        assertNotNull(geometry);
    }

    @Test
    public void testGetNumIndices() {
        assertEquals(0, geometry.getNumIndices());
    }

    @Test
    public void testGetNumTriangles() {
        assertEquals(0, geometry.getNumTriangles());
    }

    @Test
    public void testGetNumVertices() {
        assertEquals(0, geometry.getNumVertices());
    }

    @Test
    public void testBoundingBox() {
        assertFalse(geometry.hasBoundingBox());
        assertNotNull(geometry.getBoundingBox());
        assertTrue(geometry.hasBoundingBox());
    }


    @Test
    public void testBoundingSphere() {
        assertFalse(geometry.hasBoundingSphere());
        assertNotNull(geometry.getBoundingSphere());
        assertTrue(geometry.hasBoundingSphere());
    }

}
