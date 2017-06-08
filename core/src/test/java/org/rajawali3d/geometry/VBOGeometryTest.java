package org.rajawali3d.geometry;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.mockito.Mockito;

import java.nio.FloatBuffer;

import c.org.rajawali3d.gl.buffers.BufferInfo;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class VBOGeometryTest {

    @Test
    public void reload() throws Exception {
        final VBOGeometry geometry = Mockito.mock(VBOGeometry.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doNothing().when(geometry).validateBuffers();

        // Call method under test
        geometry.reload();

        Mockito.verify(geometry).validateBuffers();
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

    @SuppressWarnings("WrongConstant")
    @Test(expected = IllegalArgumentException.class)
    public void createBufferObjectWithInfoTypeTargetUsageFailByteSize() throws Exception {
        final VBOGeometry geometry = Mockito.mock(VBOGeometry.class, Mockito.CALLS_REAL_METHODS);
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
        Mockito.verify(geometry).createBufferObject(info1, info1.bufferType, info1.target, info1.usage);
    }
}