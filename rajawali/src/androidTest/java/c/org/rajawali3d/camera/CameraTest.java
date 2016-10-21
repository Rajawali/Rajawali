package c.org.rajawali3d.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.support.test.filters.SmallTest;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.scene.graph.NodeParent;
import org.junit.Test;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import java.util.Arrays;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class CameraTest {

    @Test
    public void testSetParent() throws Exception {
        final NodeParent parent = mock(NodeParent.class);
        final Camera camera = new Camera();
        camera.setParent(parent);
        assertEquals(parent, camera.parent);
    }

    @Test
    public void testModelMatrixUpdated() throws Exception {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                -2d, -5d, -10d, 1d
        };
        final NodeParent parent = mock(NodeParent.class);
        final Camera camera = spy(new Camera());
        camera.modelMatrixUpdated();
        verify(camera).modelMatrixUpdated();
        verify(camera).updateFrustum();

        final Matrix4 model = Matrix4.createTranslationMatrix(2d, 5d, 10d);
        doReturn(model).when(parent).getWorldModelMatrix();
        doNothing().when(camera).updateFrustum();
        camera.setParent(parent);
        camera.modelMatrixUpdated();
        final double[] result = camera.viewMatrix.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
        verify(camera, times(2)).updateFrustum();
    }

    @Test
    public void testIntersectBounds() throws Exception {
        final Camera camera = spy(new Camera());
        final Frustum frustum = mock(Frustum.class);
        final AABB bounds = mock(AABB.class);
        doReturn(frustum).when(camera).getFrustum();
        camera.intersectBounds(bounds);
        verify(frustum).intersectBounds(bounds);
    }

    @Test
    public void testRecalculateBounds() throws Exception {
        final Vector3 expectedMin = new Vector3(-99.41125496954281, -49.705627484771405, 1.0);
        final Vector3 expectedMax = new Vector3(99.41125496954281, 49.705627484771405, 120.0);
        final Camera camera = new Camera();
        camera.lastWidth = 200;
        camera.lastHeight = 100;
        final Vector3[] corners = new Vector3[]{
                new Vector3(-0.8284271247461901, 0.41421356237309503, 1.0),
                new Vector3(0.8284271247461901, 0.41421356237309503, 1.0),
                new Vector3(-0.8284271247461901, -0.41421356237309503, 1.0),
                new Vector3(0.8284271247461901, -0.41421356237309503, 1.0),
                new Vector3(-99.41125496954281, 49.705627484771405, 120.0),
                new Vector3(99.41125496954281, 49.705627484771405, 120.0),
                new Vector3(-99.41125496954281, -49.705627484771405, 120.0),
                new Vector3(99.41125496954281, -49.705627484771405, 120.0)
        };
        for (int i = 0; i < 8; ++i) {
            camera.frustumCorners[i].setAll(corners[i]);
        }
        camera.recalculateBounds();
        final Vector3 minBound = camera.getMinBound();
        final Vector3 maxBound = camera.getMaxBound();
        assertEquals(expectedMin, minBound);
        assertEquals(expectedMax, maxBound);
    }

    @Test
    public void testGetViewMatrix() throws Exception {
        final Camera camera = new Camera();
        assertSame(camera.viewMatrix, camera.getViewMatrix());
    }

    @Test
    public void testGetFrustum() throws Exception {
        final Camera camera = new Camera();
        assertSame(camera.frustum, camera.getFrustum());
    }

    @Test
    public void testGetFrustumCorners() throws Exception {
        final Vector3[] expected1 = new Vector3[]{
                new Vector3(-1.9999999999999998, 0.9999999999999999, 1.0),
                new Vector3(1.9999999999999998, 0.9999999999999999, 1.0),
                new Vector3(-1.9999999999999998, -0.9999999999999999, 1.0),
                new Vector3(1.9999999999999998, -0.9999999999999999, 1.0),
                new Vector3(-239.99999999999997, 119.99999999999999, 120.0),
                new Vector3(239.99999999999997, 119.99999999999999, 120.0),
                new Vector3(-239.99999999999997, -119.99999999999999, 120.0),
                new Vector3(239.99999999999997, -119.99999999999999, 120.0)
        };

        final Vector3[] expected3 = new Vector3[8];
        for (int i = 0; i < 8; ++i) {
            expected3[i] = new Vector3();
        }

        final Vector3[] expected2 = new Vector3[]{
                new Vector3(2.220446049250313E-16, 5.0, 7.0),
                new Vector3(4.0, 5.0, 7.0),
                new Vector3(2.220446049250313E-16, 3.0, 7.0),
                new Vector3(4.0, 3.0, 7.0),
                new Vector3(-237.99999999999997, 123.99999999999999, 126.0),
                new Vector3(241.99999999999997, 123.99999999999999, 126.0),
                new Vector3(-237.99999999999997, -115.99999999999999, 126.0),
                new Vector3(241.99999999999997, -115.99999999999999, 126.0)
        };

        final Camera camera = new Camera();
        camera.lastWidth = 200;
        camera.lastHeight = 100;
        camera.nearPlane = 1;
        camera.farPlane = 120;
        camera.fieldOfView = 90.0;
        Vector3[] points = new Vector3[8];

        // First test, no parent, dirty, transformed
        for (int i = 0; i < 8; ++i) {
            points[i] = new Vector3();
        }
        camera.cameraDirty = true;
        camera.getFrustumCorners(points);
        assertFalse(camera.cameraDirty);
        for (int i = 0; i < 8; ++i) {
            assertEquals(expected1[i], points[i]);
        }

        // Second test, parent, clean, transformed
        for (int i = 0; i < 8; ++i) {
            points[i] = new Vector3();
        }
        final NodeParent parent = mock(NodeParent.class);
        final Matrix4 matrix = Matrix4.createTranslationMatrix(1d, 2d, 3d);
        doReturn(matrix).when(parent).getWorldModelMatrix();
        camera.parent = parent;
        camera.getFrustumCorners(points);
        assertFalse(camera.cameraDirty);

        for (int i = 0; i < 8; ++i) {
            assertEquals(expected2[i], points[i]);
        }
    }

    @Test
    public void testUpdateFrustum() throws Exception {
        final Camera camera = spy(new Camera());
        final Frustum frustum = mock(Frustum.class);
        doReturn(frustum).when(camera).getFrustum();
        doNothing().when(camera).updateFrustumCorners();
        camera.updateFrustum();
        verify(camera).updateFrustumCorners();
        verify(frustum).update(camera.frustumCorners);
    }

    @Test
    public void testSetProjectionMatrixMatrix4() throws Exception {
        final Matrix4 matrix = mock(Matrix4.class);
        final Matrix4 clone = mock(Matrix4.class);
        doReturn(clone).when(matrix).clone();
        final Camera camera = spy(new Camera());
        final Matrix4 old = camera.projectionMatrix;
        camera.setProjectionMatrix(matrix);
        verify(matrix).clone();
        assertNotNull(camera.projectionMatrix);
        assertNotSame(old, camera.projectionMatrix);
        assertSame(clone, camera.projectionMatrix);
        assertTrue(camera.isInitialized);
        verify(camera).updateFrustum();
    }

    @Test
    public void testSetProjectionMatrixWithDimensions() throws Exception {
        final double[] expected = {
                2.414213562373095, 0.0, 0.0, 0.0,
                0.0, 1.2071067811865475, 0.0, 0.0,
                0.0, 0.0, -1.0168067226890756, -1.0,
                0.0, 0.0, -2.0168067226890756, 0.0
        };
        final Camera camera = spy(new Camera());
        camera.setProjectionMatrix(100, 200);
        assertFalse(camera.cameraDirty);
        assertEquals(100, camera.lastWidth);
        assertEquals(200, camera.lastHeight);
        assertNotNull(camera.projectionMatrix);
        final double[] result = camera.projectionMatrix.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
        assertTrue(camera.isInitialized);

        camera.cameraDirty = false;
        camera.setProjectionMatrix(100, 200);
        assertFalse(camera.cameraDirty);

        camera.cameraDirty = false;
        camera.setProjectionMatrix(90, 200);
        assertFalse(camera.cameraDirty);

        camera.cameraDirty = false;
        camera.setProjectionMatrix(90, 190);
        assertFalse(camera.cameraDirty);
        verify(camera, times(4)).updateFrustum();
    }

    @Test
    public void testSetProjectionMatrixWithFOVAndDimensions() throws Exception {
        final Camera camera = spy(new Camera());
        camera.fieldOfView = 10.0;
        camera.setProjectionMatrix(20.0, 100, 200);
        assertEquals(20.0, camera.getFieldOfView(), 1e-14);
        verify(camera).setProjectionMatrix(100, 200);
        verify(camera).updateFrustum();
    }

    @Test
    public void testUpdatePerspectiveWithSides() throws Exception {
        final Camera camera = spy(new Camera());
        camera.updatePerspective(30, 30, 20, 20);
        verify(camera).updatePerspective(60, 40);
        verify(camera).updateFrustum();
    }

    @Test
    public void testUpdatePerspectiveWithFieldsOfView() throws Exception {
        final double[] expected = {
                1.7320508075688774, 0.0, 0.0, 0.0,
                0.0, 2.598076211353316, 0.0, 0.0,
                0.0, 0.0, -1.0168067226890756, -1.0,
                0.0, 0.0, -2.0168067226890756, 0.0
        };
        final Camera camera = spy(new Camera());
        camera.updatePerspective(60, 40);
        assertEquals(60d, camera.fieldOfView, 1e-14);
        final double[] result = camera.projectionMatrix.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
        verify(camera).updateFrustum();
    }

    @Test
    public void testGetProjectionMatrix() throws Exception {
        final Camera camera = new Camera();
        final Matrix4 matrix = new Matrix4();
        camera.projectionMatrix = matrix;
        assertSame(matrix, camera.getProjectionMatrix());
    }

    @Test
    public void testGetNearPlane() throws Exception {
        final Camera camera = new Camera();
        camera.nearPlane = Double.MAX_VALUE;
        assertEquals(camera.nearPlane, camera.getNearPlane(), 1e-14);
    }

    @Test
    public void testSetNearPlane() throws Exception {
        final Camera camera = spy(new Camera());
        camera.lastHeight = 100;
        camera.lastWidth = 200;
        camera.cameraDirty = false;
        camera.nearPlane = 10.0;
        camera.setNearPlane(20.0);
        assertEquals(20.0, camera.nearPlane, 1e-14);
        verify(camera).setProjectionMatrix(200, 100);
        verify(camera).updateFrustum();
        assertFalse(camera.cameraDirty);
    }

    @Test
    public void testGetFarPlane() throws Exception {
        final Camera camera = new Camera();
        camera.farPlane = Double.MAX_VALUE;
        assertEquals(camera.farPlane, camera.getFarPlane(), 1e-14);
    }

    @Test
    public void testSetFarPlane() throws Exception {
        final Camera camera = spy(new Camera());
        camera.lastHeight = 100;
        camera.lastWidth = 200;
        camera.cameraDirty = false;
        camera.farPlane = 10.0;
        camera.setFarPlane(20.0);
        assertEquals(20.0, camera.farPlane, 1e-14);
        verify(camera).setProjectionMatrix(200, 100);
        verify(camera).updateFrustum();
        assertFalse(camera.cameraDirty);
    }

    @Test
    public void testGetFieldOfView() throws Exception {
        final Camera camera = new Camera();
        camera.fieldOfView = Double.MAX_VALUE;
        assertEquals(camera.fieldOfView, camera.getFieldOfView(), 1e-14);
    }

    @Test
    public void testSetFieldOfView() throws Exception {
        final Camera camera = spy(new Camera());
        camera.lastHeight = 100;
        camera.lastWidth = 200;
        camera.cameraDirty = false;
        camera.fieldOfView = 10.0;
        camera.setFieldOfView(20.0);
        assertEquals(20.0, camera.fieldOfView, 1e-14);
        verify(camera).setProjectionMatrix(200, 100);
        verify(camera).updateFrustum();
        assertFalse(camera.cameraDirty);
    }
}