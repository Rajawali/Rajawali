package org.rajawali3d.geometry;

import android.opengl.GLES20;
import android.support.test.filters.SmallTest;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class BufferInfoTest {

    @Test
    public void testBufferInfoNoArgsConstructor() {
        final BufferInfo floatInfo = new BufferInfo();
        assertEquals("Buffer info usage set to wrong type.", GLES20.GL_STATIC_DRAW, floatInfo.usage);
    }

    @Test
    public void testBufferInfoConstructor() {
        final FloatBuffer floatBuffer = FloatBuffer.allocate(4);
        final BufferInfo floatInfo = new BufferInfo(BufferInfo.FLOAT_BUFFER, floatBuffer);
        assertEquals("Float buffer info pointing to wrong buffer.", floatBuffer, floatInfo.buffer);
        assertEquals("Float buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, floatInfo.bufferType);

        final IntBuffer intBuffer = IntBuffer.allocate(4);
        final BufferInfo intInfo = new BufferInfo(BufferInfo.INT_BUFFER, intBuffer);
        assertEquals("Int buffer info pointing to wrong buffer.", intBuffer, intInfo.buffer);
        assertEquals("Int buffer info set to wrong type.", BufferInfo.INT_BUFFER, intInfo.bufferType);

        final ShortBuffer shortBuffer = ShortBuffer.allocate(4);
        final BufferInfo shortInfo = new BufferInfo(BufferInfo.SHORT_BUFFER, shortBuffer);
        assertEquals("Short buffer info pointing to wrong buffer.", shortBuffer, shortInfo.buffer);
        assertEquals("Short buffer info set to wrong type.", BufferInfo.SHORT_BUFFER, shortInfo.bufferType);

        final ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        final BufferInfo byteInfo = new BufferInfo(BufferInfo.BYTE_BUFFER, byteBuffer);
        assertEquals("Byte buffer info pointing to wrong buffer.", byteBuffer, byteInfo.buffer);
        assertEquals("Byte buffer info set to wrong type.", BufferInfo.BYTE_BUFFER, byteInfo.bufferType);
    }

    @Test
    public void testBufferInfoToString() {
        final BufferInfo floatInfo = new BufferInfo();
        assertNotNull(floatInfo.toString());
    }
}