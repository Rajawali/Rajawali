package org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.opengl.GLES20;
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
import org.mockito.Mockito;
import org.rajawali3d.math.vector.Vector3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@RequiresDevice
public class VBOGeometryTest extends GlTestCase {

    public class TestableVBOGeometry extends VBOGeometry {

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


    @SuppressWarnings({ "ConstantConditions", "WrongConstant" }) @Test
    public void createBuffers() throws Exception {
        final VBOGeometry geometry = Mockito.spy(new TestableVBOGeometry());
        final Buffer mockedBuffer = Mockito.mock(Buffer.class);

        // Setup testing for already created buffers
        final BufferInfo firstBuffer = new BufferInfo(BufferInfo.BYTE_BUFFER, ByteBuffer.allocate(1));
        geometry.addBuffer(firstBuffer);
        geometry.addBuffer(firstBuffer);
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                geometry.createBuffers();
            }
        });

        assertTrue(geometry.haveCreatedBuffers());
        assertTrue(firstBuffer.glHandle >= 0);

        // Test byte buffer
        final BufferInfo byteBuffer = new BufferInfo(BufferInfo.BYTE_BUFFER, ByteBuffer.allocate(1));
        geometry.addBuffer(byteBuffer);

        // Test float buffer
        final BufferInfo floatBuffer = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        geometry.addBuffer(floatBuffer);

        // Test double buffer
        final BufferInfo doubleBuffer = new BufferInfo(BufferInfo.DOUBLE_BUFFER, DoubleBuffer.allocate(1));
        geometry.addBuffer(doubleBuffer);

        // Test short buffer
        final BufferInfo shortBuffer = new BufferInfo(BufferInfo.SHORT_BUFFER, ShortBuffer.allocate(1));
        geometry.addBuffer(shortBuffer);

        // Test int buffer
        final BufferInfo intBuffer = new BufferInfo(BufferInfo.INT_BUFFER, IntBuffer.allocate(1));
        geometry.addBuffer(intBuffer);

        // Test long buffer
        final BufferInfo longBuffer = new BufferInfo(BufferInfo.LONG_BUFFER, LongBuffer.allocate(1));
        geometry.addBuffer(longBuffer);

        // Test char buffer
        final BufferInfo charBuffer = new BufferInfo(BufferInfo.CHAR_BUFFER, CharBuffer.allocate(1));
        final int lastKey = geometry.addBuffer(charBuffer);

        // Test unknown class buffer
        final BufferInfo mockedBufferInfo = new BufferInfo(Integer.MAX_VALUE, mockedBuffer);
        geometry.addBuffer(mockedBufferInfo);

        // Test null buffer info
        geometry.setBufferInfo(lastKey + 1, null);

        // Test buffer info with null buffer
        final BufferInfo nullBuffer = new BufferInfo(BufferInfo.FLOAT_BUFFER, null);
        geometry.addBuffer(nullBuffer);

        assertFalse(geometry.haveCreatedBuffers());

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                geometry.createBuffers();
            }
        });

        Mockito.verify(geometry).createBufferObject(byteBuffer);
        Mockito.verify(geometry).createBufferObject(floatBuffer);
        Mockito.verify(geometry).createBufferObject(doubleBuffer);
        Mockito.verify(geometry).createBufferObject(shortBuffer);
        Mockito.verify(geometry).createBufferObject(intBuffer);
        Mockito.verify(geometry).createBufferObject(longBuffer);
        Mockito.verify(geometry).createBufferObject(charBuffer);

        assertTrue(geometry.haveCreatedBuffers());

        // Verify the GL state was left as expected
        final int[] results = new int[2];
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                GLES20.glGetIntegerv(GLES20.GL_ELEMENT_ARRAY_BUFFER, results, 0);
                GLES20.glGetIntegerv(GLES20.GL_ARRAY_BUFFER, results, 1);
            }
        });

        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
    }

    @Test
    public void validateBuffers() throws Exception {

    }

    @Test
    public void reload() throws Exception {
        final VBOGeometry geometry = Mockito.spy(new TestableVBOGeometry());
        Mockito.doNothing().when(geometry).validateBuffers();
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        geometry.addBuffer(info1);

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                geometry.createBuffers();
            }
        });

        // Call method under test
        geometry.reload();

        assertEquals(-1, info1.glHandle);
        Mockito.verify(geometry).validateBuffers();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void destroy() throws Exception {
        final VBOGeometry geometry = new TestableVBOGeometry();
        final BufferInfo info1 = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(1));
        final BufferInfo info2 = new BufferInfo(BufferInfo.FLOAT_BUFFER, null);
        final int key = geometry.addBuffer(info1);
        geometry.addBuffer(info2);
        geometry.setBufferInfo(key + 2, null);

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                geometry.createBuffers();
            }
        });
        final int oldHandle = info1.glHandle;
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                geometry.destroy();
            }
        });
        assertEquals(-1, info1.glHandle);
        assertNull(info1.buffer);
        assertEquals(0, geometry.getBufferCount());

        // Verify handles are invalid
        final boolean[] result = new boolean[1];
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                result[0] = GLES20.glIsBuffer(oldHandle);
            }
        });
        assertFalse(result[0]);
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

        assertEquals(1, byteBuffer.elementSize);
        assertTrue(byteBuffer.glHandle >= 0);

        assertEquals(4, floatBuffer.elementSize);
        assertTrue(floatBuffer.glHandle >= 0);

        assertEquals(8, doubleBuffer.elementSize);
        assertTrue(doubleBuffer.glHandle >= 0);

        assertEquals(2, shortBuffer.elementSize);
        assertTrue(shortBuffer.glHandle >= 0);

        assertEquals(4, intBuffer.elementSize);
        assertTrue(intBuffer.glHandle >= 0);

        assertEquals(8, longBuffer.elementSize);
        assertTrue(longBuffer.glHandle >= 0);

        assertEquals(2, charBuffer.elementSize);
        assertTrue(charBuffer.glHandle >= 0);

        // The following verifies the GL state was left as expected
        final int[] results = new int[2];
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                GLES20.glGetIntegerv(GLES20.GL_ELEMENT_ARRAY_BUFFER, results, 0);
                GLES20.glGetIntegerv(GLES20.GL_ARRAY_BUFFER, results, 1);
            }
        });

        assertEquals(0, results[0]);
        assertEquals(0, results[1]);
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