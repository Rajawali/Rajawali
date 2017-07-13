package org.rajawali3d.cameras;

import static org.junit.Assert.*;
import org.junit.*;

import org.junit.Test;
import org.rajawali3d.cameras.Frustum;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.bounds.BoundingBox;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
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
