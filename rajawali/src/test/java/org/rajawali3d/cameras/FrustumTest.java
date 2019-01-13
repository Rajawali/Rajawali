package org.rajawali3d.cameras;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class FrustumTest {
    Frustum frustum;

    @Before
    public void setup() {
        frustum = new Frustum();
        assertNotNull(new Frustum());
        final Matrix4 inverseProjectionView = new Matrix4();
        frustum.update(inverseProjectionView);
    }

    @After
    public void teardown() {
        frustum = null;
    }

    @Test
    public void testSphereInFrustum() {
        final Vector3 center = new Vector3();
        final double radius = 0;
        boolean result = frustum.sphereInFrustum(center, radius);
        assertTrue(result);
    }

    @Test
    public void testBoundsInFrustum() {
        final BoundingBox bounds = new BoundingBox();
        boolean result = frustum.boundsInFrustum(bounds);
        assertTrue(result);
    }

    @Test
    public void testPointInFrustum() {
        final Vector3 point = new Vector3();
        boolean result = frustum.pointInFrustum(point);
        assertTrue(result);
    }
}
