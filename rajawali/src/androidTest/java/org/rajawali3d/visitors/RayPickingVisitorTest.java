package org.rajawali3d.utils;

import static org.junit.Assert.*;
import org.junit.*;

import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.rajawali3d.visitors.RayPickingVisitor;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;

import java.util.Arrays;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
@SmallTest
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
    public void setup() throws Exception {
        rayStart = new Vector3(1,1,1).multiply(3);
        rayEnd = new Vector3(-1,-1,-1).multiply(3);
        visitor = new RayPickingVisitor(rayStart, rayEnd);
    }

    @After
    public void teardown() throws Exception {
        Vector3 rayStart = null;
        Vector3 rayEnd = null;
        visitor = null;
    }


    @Test
    public void testGetPickedObjectNull() throws Exception {
        assertNull(visitor.getPickedObject());
    }

    @Test
    public void testApplyNull() throws Exception {
        visitor.apply(null);
    }

    @Test
    public void testApplySphere() throws Exception {
        Sphere sphere = new Sphere(1,4,2,false,false,false,false);
        sphere.getGeometry().getBoundingSphere();
        visitor.apply(sphere);
        assertNull(visitor.getPickedObject());
    }

    @Test
    public void testApplyInstrumentedSphere() throws Exception {
        InstrumentedSphere sphere = new InstrumentedSphere(1,4,2,false,false,false,false);
        sphere.getGeometry().getBoundingSphere();
        sphere.setFrustumTest(false);
        sphere.setInFrustum(true);
        visitor.apply(sphere);
        assertEquals(sphere, visitor.getPickedObject());
    }

    @Test
    public void testApplyCube() throws Exception {
        Cube cube = new Cube(1,false,false,false,false,false);
        cube.getGeometry().getBoundingBox();
        visitor.apply(cube);
        assertNull(visitor.getPickedObject());
    }

    @Test
    public void testApplyInstrumentedCube() throws Exception {
        InstrumentedCube cube = new InstrumentedCube(1,false,false,false,false,false);
        cube.getGeometry().getBoundingBox();
        cube.setFrustumTest(false);
        cube.setInFrustum(true);
        visitor.apply(cube);
        assertEquals(cube, visitor.getPickedObject());
    }
}
