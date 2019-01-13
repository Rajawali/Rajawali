package org.rajawali3d.visitors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class RayPickingVisitorTest {
    RayPickingVisitor visitor;
    Vector3 rayStart;
    Vector3 rayEnd;

    class InstrumentedSphere extends Sphere {
        public InstrumentedSphere(float radius, int segmentsW, int segmentsH, boolean createTextureCoordinates, boolean createVertexColorBuffer, boolean createVBOs, boolean mirrorTextureCoords) {
            super(radius, segmentsW, segmentsH, createTextureCoordinates, createVertexColorBuffer, createVBOs, mirrorTextureCoords);
        }

        void setInFrustum(boolean isInFrustum) {
            mIsInFrustum = isInFrustum;
        }
    }

    class InstrumentedCube extends Cube {
        public InstrumentedCube(float size, boolean isSkybox, boolean hasCubemapTexture, boolean createTextureCoordinates, boolean createVertexColorBuffer, boolean createVBOs) {
            super(size, isSkybox, hasCubemapTexture, createTextureCoordinates, createVertexColorBuffer, createVBOs);
        }

        void setInFrustum(boolean isInFrustum) {
            mIsInFrustum = isInFrustum;
        }
    }

    @Before
    public void setup() {
        rayStart = new Vector3(1, 1, 1).multiply(3);
        rayEnd = new Vector3(-1, -1, -1).multiply(3);
        visitor = new RayPickingVisitor(rayStart, rayEnd);
    }

    @After
    public void teardown() {
        Vector3 rayStart = null;
        Vector3 rayEnd = null;
        visitor = null;
    }


    @Test
    public void testGetPickedObjectNull() {
        assertNull(visitor.getPickedObject());
    }

    @Test
    public void testApplyNull() {
        visitor.apply(null);
    }

    @Test
    public void testApplySphere() {
        Sphere sphere = new Sphere(1, 4, 2, false, false, false, false);
        sphere.getGeometry().getBoundingSphere();
        visitor.apply(sphere);
        assertNull(visitor.getPickedObject());
    }

    @Test
    public void testApplyInstrumentedSphere() {
        InstrumentedSphere sphere = new InstrumentedSphere(1, 4, 2, false, false, false, false);
        sphere.getGeometry().getBoundingSphere();
        sphere.setFrustumTest(false);
        sphere.setInFrustum(true);
        visitor.apply(sphere);
        assertEquals(sphere, visitor.getPickedObject());
    }

    @Test
    public void testApplyCube() {
        Cube cube = new Cube(1, false, false, false, false, false);
        cube.getGeometry().getBoundingBox();
        visitor.apply(cube);
        assertNull(visitor.getPickedObject());
    }

    @Test
    public void testApplyInstrumentedCube() {
        InstrumentedCube cube = new InstrumentedCube(1, false, false, false, false, false);
        cube.getGeometry().getBoundingBox();
        cube.setFrustumTest(false);
        cube.setInFrustum(true);
        visitor.apply(cube);
        assertEquals(cube, visitor.getPickedObject());
    }
}
