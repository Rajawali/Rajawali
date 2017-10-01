package c.org.rajawali3d.util;

import static org.junit.Assert.assertEquals;

import c.org.rajawali3d.gl.buffers.BufferInfo;
import org.junit.Test;

import java.nio.FloatBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class FloatBufferWrapperTest {

    private static BufferInfo createNewBuffer() {
        /*                                |a0|a1|a2|b0|b1|b2|c0|a3|a4|a5| b3| b4| b5| c1| a6| a7| a8| b6| b7| b8| c2| */
        final float[] array = new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        final FloatBuffer buffer = FloatBuffer.wrap(array);
        final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, buffer);
        info.count = 3;
        info.stride = 3;
        info.offset = 0;
        return info;
    }

    @Test
    public void bufferPosition() throws Exception {
        final BufferInfo info = createNewBuffer();
        assertEquals(0, FloatBufferWrapper.bufferPosition(info, 0));
        assertEquals(1, FloatBufferWrapper.bufferPosition(info, 1));
        assertEquals(2, FloatBufferWrapper.bufferPosition(info, 2));
        assertEquals(3, FloatBufferWrapper.bufferPosition(info, 3));
        assertEquals(4, FloatBufferWrapper.bufferPosition(info, 4));
        assertEquals(5, FloatBufferWrapper.bufferPosition(info, 5));
        assertEquals(6, FloatBufferWrapper.bufferPosition(info, 6));
        assertEquals(7, FloatBufferWrapper.bufferPosition(info, 7));
        assertEquals(8, FloatBufferWrapper.bufferPosition(info, 8));
    }

    @Test
    public void getBuffer() throws Exception {
        final BufferInfo info = createNewBuffer();
        final FloatBufferWrapper wrapper = new FloatBufferWrapper(info);
        assertEquals(wrapper.getBuffer(), info.buffer);
    }
}