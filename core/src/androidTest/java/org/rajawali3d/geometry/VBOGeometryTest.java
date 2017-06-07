package org.rajawali3d.geometry;

import android.support.annotation.NonNull;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.rajawali3d.math.vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.buffers.BufferInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

    @SuppressWarnings("WrongConstant")
    @Test
    public void createBufferObjectWithInfoTypeTarget() throws Exception {
        final VBOGeometry geometry = Mockito.mock(VBOGeometry.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doNothing().when(geometry).createBufferObject(Mockito.any(BufferInfo.class), Mockito.anyInt(),
            Mockito.anyInt(), Mockito.anyInt());
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        geometry.createBufferObject(info1, -10, -20);
        Mockito.verify(geometry).createBufferObject(info1, -10, -20);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void createBufferObjectWithInfoTypeTargetUsage() throws Exception {
        final VBOGeometry geometry = new TestableVBOGeometry();

        // Test byte buffer
        final BufferInfo byteBuffer = new BufferInfo(BufferInfo.BYTE_BUFFER, ByteBuffer.allocate(1));
        geometry.createBufferObject(byteBuffer, byteBuffer.bufferType, byteBuffer.target, byteBuffer.usage);

        // Test float buffer
        final BufferInfo floatBuffer = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        geometry.createBufferObject(floatBuffer, floatBuffer.bufferType, floatBuffer.target, floatBuffer.usage);

        // Test double buffer
        final BufferInfo doubleBuffer = new BufferInfo(BufferInfo.DOUBLE_BUFFER, DoubleBuffer.allocate(1));
        geometry.createBufferObject(doubleBuffer, doubleBuffer.bufferType, doubleBuffer.target, doubleBuffer.usage);

        // Test short buffer
        final BufferInfo shortBuffer = new BufferInfo(BufferInfo.SHORT_BUFFER, ShortBuffer.allocate(1));
        geometry.createBufferObject(shortBuffer, shortBuffer.bufferType, shortBuffer.target, shortBuffer.usage);

        // Test int buffer
        final BufferInfo intBuffer = new BufferInfo(BufferInfo.INT_BUFFER, IntBuffer.allocate(1));
        geometry.createBufferObject(intBuffer, intBuffer.bufferType, intBuffer.target, intBuffer.usage);

        // Test long buffer
        final BufferInfo longBuffer = new BufferInfo(BufferInfo.LONG_BUFFER, LongBuffer.allocate(1));
        geometry.createBufferObject(longBuffer, longBuffer.bufferType, longBuffer.target, longBuffer.usage);

        // Test char buffer
        final BufferInfo charBuffer = new BufferInfo(BufferInfo.CHAR_BUFFER, CharBuffer.allocate(1));
        geometry.createBufferObject(charBuffer, charBuffer.bufferType, charBuffer.target, charBuffer.usage);

        // Test null buffer
        final BufferInfo nullBuffer = new BufferInfo(BufferInfo.FLOAT_BUFFER, null);
        geometry.createBufferObject(nullBuffer, nullBuffer.bufferType, nullBuffer.target, nullBuffer.usage);
    }

    @SuppressWarnings("WrongConstant")
    @Test(expected = IllegalArgumentException.class)
    public void createBufferObjectWithInfoTypeTargetUsageFailByteSize() throws Exception {
        final VBOGeometry geometry = new TestableVBOGeometry();
        final BufferInfo byteBuffer = new BufferInfo(BufferInfo.BYTE_BUFFER, FloatBuffer.allocate(1));
        geometry.createBufferObject(byteBuffer, -1, byteBuffer.target, byteBuffer.usage);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void createBufferObjectWithInfo() throws Exception {
        final VBOGeometry geometry = Mockito.mock(VBOGeometry.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doNothing().when(geometry).createBufferObject(Mockito.any(BufferInfo.class), Mockito.anyInt(),
            Mockito.anyInt(), Mockito.anyInt());
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        geometry.createBufferObject(info1);
        Mockito.verify(geometry).createBufferObject(info1);
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
        final VBOGeometry geometry = new TestableVBOGeometry();
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        final int key = geometry.addBuffer(info1);
        final BufferInfo goodResult = geometry.getBufferInfo(key);
        assertNotNull(goodResult);
        assertEquals(info1, goodResult);
        final BufferInfo badResult = geometry.getBufferInfo(50);
        assertNull(badResult);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void setBufferInfo() throws Exception {
        final VBOGeometry geometry = new TestableVBOGeometry();
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        geometry.setBufferInfo(1, info1);
        assertEquals(info1, geometry.getBufferInfo(1));
        geometry.setBufferInfo(1, null);
        assertNull(geometry.getBufferInfo(1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void hasBuffer() throws Exception {
        final VBOGeometry geometry = new TestableVBOGeometry();
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        final int key = geometry.addBuffer(info1);
        // Test when buffer is valid
        assertTrue(geometry.hasBuffer(key));
        // Test a bad key
        assertFalse(geometry.hasBuffer(50));
        geometry.setBufferInfo(key, null);
        // Test key exists with null mapping - rare
        assertFalse(geometry.hasBuffer(key));
    }
}