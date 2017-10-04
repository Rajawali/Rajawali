package c.org.rajawali3d.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import c.org.rajawali3d.gl.buffers.BufferInfo;
import org.junit.Test;

import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class InterleavedFloatBufferWrapperTest {

    private static BufferInfo createNewBuffer() {
        /*                                |a0|a1|a2|b0|b1|b2|c0|a3|a4|a5| b3| b4| b5| c1| a6| a7| a8| b6| b7| b8| c2| */
        final float[] array = new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        final FloatBuffer buffer = FloatBuffer.wrap(array);
        final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, buffer);
        info.count = 3;
        info.stride = 7;
        info.offset = 3;
        return info;
    }

    @Test
    public void bufferPosition() throws Exception {
        final BufferInfo info = createNewBuffer();
        assertEquals(3, FloatBufferWrapper.bufferPosition(info, 0));
        assertEquals(4, FloatBufferWrapper.bufferPosition(info, 1));
        assertEquals(5, FloatBufferWrapper.bufferPosition(info, 2));
        assertEquals(10, FloatBufferWrapper.bufferPosition(info, 3));
        assertEquals(11, FloatBufferWrapper.bufferPosition(info, 4));
        assertEquals(12, FloatBufferWrapper.bufferPosition(info, 5));
        assertEquals(17, FloatBufferWrapper.bufferPosition(info, 6));
        assertEquals(18, FloatBufferWrapper.bufferPosition(info, 7));
        assertEquals(19, FloatBufferWrapper.bufferPosition(info, 8));
    }

    @Test
    public void capacity() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        assertEquals(9, wrapper.capacity());
    }

    @Test
    public void hasRemaining() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        assertTrue(wrapper.hasRemaining());
        wrapper.position(4); // b3, index 11
        assertTrue(wrapper.hasRemaining());
        wrapper.position(5); // b5, index 12
        assertTrue(wrapper.hasRemaining());
        wrapper.position(8); // b8, index 19
        assertFalse(wrapper.hasRemaining());
    }

    @Test
    public void limit() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        assertEquals(9, wrapper.limit());
    }

    @Test
    public void position() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        wrapper.position(5);
        assertEquals(5, wrapper.position());
    }

    @Test
    public void remaining() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        assertEquals(9, wrapper.remaining());
        wrapper.get();
        assertEquals(8, wrapper.remaining());
        wrapper.get();
        assertEquals(7, wrapper.remaining());
        wrapper.get();
        assertEquals(6, wrapper.remaining());
        wrapper.get();
        assertEquals(5, wrapper.remaining());
        wrapper.get();
        assertEquals(4, wrapper.remaining());
        wrapper.get();
        assertEquals(3, wrapper.remaining());
        wrapper.get();
        assertEquals(2, wrapper.remaining());
        wrapper.get();
        assertEquals(1, wrapper.remaining());
        wrapper.get();
        assertEquals(0, wrapper.remaining());
    }

    @Test
    public void rewind() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        wrapper.position(6);
        wrapper.rewind();
        assertEquals(0, wrapper.position());
    }

    @Test
    public void get() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        assertEquals(Float.floatToRawIntBits(3), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(4), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(5), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(10), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(11), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(12), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(17), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(18), Float.floatToRawIntBits(wrapper.get()));
        assertEquals(Float.floatToRawIntBits(19), Float.floatToRawIntBits(wrapper.get()));
    }

    @Test
    public void getFloatArray() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        final float[] buffer = new float[9];
        FloatBufferWrapper retval = wrapper.get(buffer);
        assertEquals(wrapper, retval);
        assertTrue(Arrays.equals(buffer, new float[] {3, 4, 5, 10, 11, 12, 17, 18, 19}));
    }

    @Test
    public void getFloatArrayOffsetLength() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        wrapper.position(3);
        final float[] buffer = new float[6];
        FloatBufferWrapper retval = wrapper.get(buffer, 3, 3);
        assertEquals(wrapper, retval);
        final float[] expected = new float[] {0, 0, 0, 10, 11, 12};
        assertTrue("Expected = " + Arrays.toString(expected) + " Actual = " + Arrays.toString(buffer),
                   Arrays.equals(buffer, expected));
    }

    @Test
    public void getIndex() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        assertEquals(Float.floatToRawIntBits(3), Float.floatToRawIntBits(wrapper.get(0)));
        assertEquals(Float.floatToRawIntBits(4), Float.floatToRawIntBits(wrapper.get(1)));
        assertEquals(Float.floatToRawIntBits(5), Float.floatToRawIntBits(wrapper.get(2)));
        assertEquals(Float.floatToRawIntBits(10), Float.floatToRawIntBits(wrapper.get(3)));
        assertEquals(Float.floatToRawIntBits(11), Float.floatToRawIntBits(wrapper.get(4)));
        assertEquals(Float.floatToRawIntBits(12), Float.floatToRawIntBits(wrapper.get(5)));
        assertEquals(Float.floatToRawIntBits(17), Float.floatToRawIntBits(wrapper.get(6)));
        assertEquals(Float.floatToRawIntBits(18), Float.floatToRawIntBits(wrapper.get(7)));
        assertEquals(Float.floatToRawIntBits(19), Float.floatToRawIntBits(wrapper.get(8)));
    }

    @Test
    public void putIndex() throws Exception {
        final FloatBufferWrapper wrapper = new InterleavedFloatBufferWrapper(createNewBuffer());
        wrapper.put(2, 42);
        assertEquals(Float.floatToRawIntBits(42), Float.floatToRawIntBits(wrapper.get(2)));
    }
}