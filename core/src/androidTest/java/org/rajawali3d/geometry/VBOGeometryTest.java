package org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;

import android.support.annotation.NonNull;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.math.vector.Vector3;

import java.nio.FloatBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@RequiresDevice
public class VBOGeometryTest extends GlTestCase {

    private final class TestableVBOGeometry extends VBOGeometry {

        @Override public boolean isValid() {
            return false;
        }

        @Override public void calculateAABounds(@NonNull Vector3 min, @NonNull Vector3 max) {

        }

        @Override public void issueDrawCalls() {

        }

        @Override public int getTriangleCount() {
            return 0;
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp(getClass().getSimpleName());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void createBuffers() throws Exception {

    }

    @Test
    public void validateBuffers() throws Exception {

    }

    @Test
    public void reload() throws Exception {

    }

    @Test
    public void destroy() throws Exception {

    }

    @Test
    public void createBufferObject() throws Exception {

    }

    @Test
    public void createBufferObject1() throws Exception {

    }

    @Test
    public void createBufferObject2() throws Exception {

    }

    @Test
    public void addBuffer() throws Exception {
        final VBOGeometry geometry = new TestableVBOGeometry();
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        final BufferInfo info2 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        geometry.addBuffer(info1);
        geometry.addBuffer(info2);
        assertEquals(0, info1.rajawaliHandle);
        assertEquals(1, info2.rajawaliHandle);
        assertEquals(info1, geometry.getBufferInfo(0));
        assertEquals(info2, geometry.getBufferInfo(1));
    }

    @Test
    public void changeBufferUsage() throws Exception {

    }

    @Test
    public void changeBufferData() throws Exception {

    }

    @Test
    public void changeBufferData1() throws Exception {

    }

    @Test
    public void changeBufferData2() throws Exception {

    }

    @Test
    public void changeBufferData3() throws Exception {

    }

    @Test
    public void getBufferInfo() throws Exception {

    }

    @Test
    public void setBufferInfo() throws Exception {

    }

    @Test
    public void addBufferHandles() throws Exception {

    }

    @Test
    public void hasBuffer() throws Exception {

    }

}