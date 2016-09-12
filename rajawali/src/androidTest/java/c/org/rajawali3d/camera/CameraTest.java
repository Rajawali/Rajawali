package c.org.rajawali3d.camera;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.scene.graph.NodeParent;
import org.junit.Test;
import org.rajawali3d.math.Matrix4;

import java.util.Arrays;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class CameraTest {

    @Test
    public void testGetViewMatrix() throws Exception {

    }

    @Test
    public void testGetFrustumCorners() throws Exception {

    }

    @Test
    public void testGetFrustumCorners1() throws Exception {

    }

    @Test
    public void testUpdateFrustum() throws Exception {

    }

    @Test
    public void testSetProjectionMatrix() throws Exception {

    }

    @Test
    public void testSetProjectionMatrix1() throws Exception {

    }

    @Test
    public void testSetProjectionMatrix2() throws Exception {

    }

    @Test
    public void testUpdatePerspective() throws Exception {

    }

    @Test
    public void testUpdatePerspective1() throws Exception {

    }

    @Test
    public void testGetProjectionMatrix() throws Exception {

    }

    @Test
    public void testGetNearPlane() throws Exception {

    }

    @Test
    public void testSetNearPlane() throws Exception {

    }

    @Test
    public void testGetFarPlane() throws Exception {

    }

    @Test
    public void testSetFarPlane() throws Exception {

    }

    @Test
    public void testGetFieldOfView() throws Exception {

    }

    @Test
    public void testSetFieldOfView() throws Exception {

    }

    @Test
    public void testSetParent() throws Exception {
        final NodeParent parent = mock(NodeParent.class);
        final Camera camera = new Camera();
        camera.setParent(parent);
        assertEquals(parent, camera.parent);
    }

    @Test
    public void testModelMatrixUpdated() throws Exception {
        final double[] expected = new double[] {
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                -2d, -5d, -10d, 1d
        };
        final NodeParent parent = mock(NodeParent.class);
        final Camera camera = spy(new Camera());
        camera.modelMatrixUpdated();
        verify(camera).modelMatrixUpdated();
        verifyNoMoreInteractions(camera);

        final Matrix4 model = Matrix4.createTranslationMatrix(2d, 5d, 10d);
        doReturn(model).when(parent).getWorldModelMatrix();
        camera.setParent(parent);
        camera.modelMatrixUpdated();
        final double[] result = camera.viewMatrix.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testGetMaxBound() throws Exception {

    }

    @Test
    public void testGetMinBound() throws Exception {

    }

    @Test
    public void testRecalculateBounds() throws Exception {

    }

    @Test
    public void testRecalculateBoundsForAdd() throws Exception {
        // This method is a non-op in Camera so test that it acts that way
        final Camera camera = spy(new Camera());
        final AABB added = mock(AABB.class);
        camera.recalculateBoundsForAdd(added);
        verifyZeroInteractions(added);
        verify(camera).recalculateBoundsForAdd(added); // Contrived I know, but necessary
        verifyNoMoreInteractions(camera);
    }
}