package org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.opengl.GLES20;
import android.test.suitebuilder.annotation.SmallTest;
import org.rajawali3d.geometry.Geometry.BufferType;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

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
        final BufferInfo floatInfo = new BufferInfo(BufferType.FLOAT_BUFFER, floatBuffer);
        assertEquals("Float buffer info pointing to wrong buffer.", floatBuffer, floatInfo.buffer);
        assertEquals("Float buffer info set to wrong type.", BufferType.FLOAT_BUFFER, floatInfo.bufferType);

        final IntBuffer intBuffer = IntBuffer.allocate(4);
        final BufferInfo intInfo = new BufferInfo(BufferType.INT_BUFFER, intBuffer);
        assertEquals("Int buffer info pointing to wrong buffer.", intBuffer, intInfo.buffer);
        assertEquals("Int buffer info set to wrong type.", BufferType.INT_BUFFER, intInfo.bufferType);

        final ShortBuffer shortBuffer = ShortBuffer.allocate(4);
        final BufferInfo shortInfo = new BufferInfo(BufferType.SHORT_BUFFER, shortBuffer);
        assertEquals("Short buffer info pointing to wrong buffer.", shortBuffer, shortInfo.buffer);
        assertEquals("Short buffer info set to wrong type.", BufferType.SHORT_BUFFER, shortInfo.bufferType);

        final ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        final BufferInfo byteInfo = new BufferInfo(BufferType.BYTE_BUFFER, byteBuffer);
        assertEquals("Byte buffer info pointing to wrong buffer.", byteBuffer, byteInfo.buffer);
        assertEquals("Byte buffer info set to wrong type.", BufferType.BYTE_BUFFER, byteInfo.bufferType);
    }

    @Test
    public void testBufferInfoToString() {
        final BufferInfo floatInfo = new BufferInfo();
        assertNotNull(floatInfo.toString());
    }
}