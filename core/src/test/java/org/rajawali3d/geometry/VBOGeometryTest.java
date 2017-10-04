package org.rajawali3d.geometry;

import static org.junit.Assert.assertTrue;

import android.support.test.filters.SmallTest;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class VBOGeometryTest {

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

    @Test
    public void buildResizedByteBuffer() throws Exception {
        final byte[] original = new byte[] { 1, 2, 3, 4 };
        final byte[] subset = new byte[] { 5, 6 };
        final ByteBuffer originalBuffer = ByteBuffer.wrap(original);
        final ByteBuffer subsetBuffer = ByteBuffer.wrap(subset);
        final ByteBuffer result = (ByteBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 0, 2);
        assertTrue(Arrays.equals(new byte[] { 5, 6, 3, 4 }, result.array()));
        final ByteBuffer result2 = (ByteBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 4, 2);
        assertTrue(Arrays.equals(new byte[] { 5, 6, 3, 4, 5, 6 }, result2.array()));
    }

    @Test
    public void buildResizedFloatBuffer() throws Exception {
        final float[] original = new float[] { 1f, 2f, 3f, 4f };
        final float[] subset = new float[] { 5f, 6f };
        final FloatBuffer originalBuffer = FloatBuffer.wrap(original);
        final FloatBuffer subsetBuffer = FloatBuffer.wrap(subset);
        final FloatBuffer result = (FloatBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 0, 2);
        assertTrue(Arrays.equals(new float[] { 5f, 6f, 3f, 4f }, result.array()));
        final FloatBuffer result2 = (FloatBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 4, 2);
        assertTrue(Arrays.equals(new float[] { 5f, 6f, 3f, 4f, 5f, 6f }, result2.array()));
    }

    @Test
    public void buildResizedDoubleBuffer() throws Exception {
        final double[] original = new double[] { 1f, 2f, 3f, 4f };
        final double[] subset = new double[] { 5f, 6f };
        final DoubleBuffer originalBuffer = DoubleBuffer.wrap(original);
        final DoubleBuffer subsetBuffer = DoubleBuffer.wrap(subset);
        final DoubleBuffer result = (DoubleBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 0, 2);
        assertTrue(Arrays.equals(new double[] { 5d, 6d, 3d, 4d }, result.array()));
        final DoubleBuffer result2 = (DoubleBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 4, 2);
        assertTrue(Arrays.equals(new double[] { 5d, 6d, 3d, 4d, 5d, 6d }, result2.array()));
    }

    @Test
    public void buildResizedShortBuffer() throws Exception {
        final short[] original = new short[] { 1, 2, 3, 4 };
        final short[] subset = new short[] { 5, 6 };
        final ShortBuffer originalBuffer = ShortBuffer.wrap(original);
        final ShortBuffer subsetBuffer = ShortBuffer.wrap(subset);
        final ShortBuffer result = (ShortBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 0, 2);
        assertTrue(Arrays.equals(new short[] { 5, 6, 3, 4 }, result.array()));
        final ShortBuffer result2 = (ShortBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 4, 2);
        assertTrue(Arrays.equals(new short[] { 5, 6, 3, 4, 5, 6 }, result2.array()));
    }

    @Test
    public void buildResizedIntBuffer() throws Exception {
        final int[] original = new int[] { 1, 2, 3, 4 };
        final int[] subset = new int[] { 5, 6 };
        final IntBuffer originalBuffer = IntBuffer.wrap(original);
        final IntBuffer subsetBuffer = IntBuffer.wrap(subset);
        final IntBuffer result = (IntBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 0, 2);
        assertTrue(Arrays.equals(new int[] { 5, 6, 3, 4 }, result.array()));
        final IntBuffer result2 = (IntBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 4, 2);
        assertTrue(Arrays.equals(new int[] { 5, 6, 3, 4, 5, 6 }, result2.array()));
    }

    @Test
    public void buildResizedLongBuffer() throws Exception {
        final long[] original = new long[] { 1, 2, 3, 4 };
        final long[] subset = new long[] { 5, 6 };
        final LongBuffer originalBuffer = LongBuffer.wrap(original);
        final LongBuffer subsetBuffer = LongBuffer.wrap(subset);
        final LongBuffer result = (LongBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 0, 2);
        assertTrue(Arrays.equals(new long[] { 5, 6, 3, 4 }, result.array()));
        final LongBuffer result2 = (LongBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 4, 2);
        assertTrue(Arrays.equals(new long[] { 5, 6, 3, 4, 5, 6 }, result2.array()));
    }

    @Test
    public void buildResizedCharBuffer() throws Exception {
        final char[] original = new char[] { 1, 2, 3, 4 };
        final char[] subset = new char[] { 5, 6 };
        final CharBuffer originalBuffer = CharBuffer.wrap(original);
        final CharBuffer subsetBuffer = CharBuffer.wrap(subset);
        final CharBuffer result = (CharBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 0, 2);
        assertTrue(Arrays.equals(new char[] { 5, 6, 3, 4 }, result.array()));
        final CharBuffer result2 = (CharBuffer) VBOGeometry.buildResizedBuffer(originalBuffer, subsetBuffer, 4, 2);
        assertTrue(Arrays.equals(new char[] { 5, 6, 3, 4, 5, 6 }, result2.array()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildResizedUnknownBuffer() throws Exception {
        final Buffer unknownBuffer = Mockito.mock(Buffer.class);
        final Buffer newUnknownBuffer = Mockito.mock(Buffer.class);
        VBOGeometry.buildResizedBuffer(unknownBuffer, newUnknownBuffer, 0, 1);
    }
}