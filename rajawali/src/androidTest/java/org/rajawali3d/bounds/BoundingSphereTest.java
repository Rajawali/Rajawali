package org.rajawali3d.bounds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class BoundingSphereTest {
    BoundingSphere bounds;

    @Before
    public void setup() throws Exception {
        bounds = new BoundingSphere();
    }

    @After
    public void teardown() throws Exception {
        bounds = null;
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(bounds);
    }

    @Test
    public void testGetPosition() throws Exception {
        Vector3 position = bounds.getPosition();
        assertEquals(0, position.x, 1e-14);
        assertEquals(0, position.y, 1e-14);
        assertEquals(0, position.z, 1e-14);
    }

    @Test
    public void testGetRadius() throws Exception {
        double radius = bounds.getRadius();
        assertEquals(0, radius, 1e-14);
    }

    @Test
    public void testGetScale() throws Exception {
        double scale = bounds.getScale();
        assertEquals(0, scale, 1e-14);
    }

    @Test
    public void testGetScaledRadius() throws Exception {
        double scaledRadius = bounds.getScaledRadius();
        assertEquals(0, scaledRadius, 1e-14);
    }

}
