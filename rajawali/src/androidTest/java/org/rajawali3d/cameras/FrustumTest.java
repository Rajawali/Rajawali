package org.rajawali3d.cameras;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class FrustumTest {
    Frustum frustum;

    @Before
    public void setup() throws Exception {
        frustum = new Frustum();
        assertNotNull(new Frustum());
        final Matrix4 inverseProjectionView = new Matrix4();
    	frustum.update(inverseProjectionView);
    }

    @After
    public void teardown() throws Exception {
        frustum = null;
    }

    @Test
    public void testSphereInFrustum() throws Exception {
	final Vector3 center = new Vector3();
        final double radius = 0;
	boolean result = frustum.sphereInFrustum(center, radius);
        assertTrue(result);
    }

    @Test
    public void testBoundsInFrustum() throws Exception {
        final BoundingBox bounds = new BoundingBox();
	boolean result = frustum.boundsInFrustum(bounds);
        assertTrue(result);
    }

    @Test
    public void testPointInFrustum() throws Exception {
	final Vector3 point = new Vector3();
	boolean result = frustum.pointInFrustum(point);
        assertTrue(result);
    }
}
