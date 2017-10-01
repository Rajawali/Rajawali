package c.org.rajawali3d.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import org.rajawali3d.geometry.NonInterleavedGeometry;

import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * Base wrapper class for accessing {@link FloatBuffer} instances in a controlled manner which differs from the
 * default behavior of the {@link FloatBuffer} class. This implementation simply calls through to the wrapped
 * {@link FloatBuffer}, implementing default behavior. This is useful for dealing with VBOs which contain only a
 * single attribute of data such as would be found in {@link NonInterleavedGeometry}.
 *
 * Note that in there interests of minimizing the code base to test, not all methods present in {@link Buffer} and
 * {@link FloatBuffer} have been implemented. The can however be added in the future if a need for them arises.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class FloatBufferWrapper {

    protected final BufferInfo info;

    protected static int bufferPosition(@NonNull BufferInfo info, @IntRange(from = 0) int position) {
        final int offset = info.offset;
        final int stride = info.stride;
        final int count = info.count;
        final int vertex = position / count;
        final int vertexElement = position % count;
        return offset + (vertex * stride) + vertexElement;
    }

    /**
     * | 0| 1| 2| 3| 4| 5| 6| 7| 8| 9|10|11|12|13|14|15|16|17|18|19|20|
     * |a0|a1|a2|b0|b1|b2|c0|a3|a4|a5|b3|b4|b5|c1|a6|a7|a8|b6|b7|b8|c2|
     *
     * @param info
     */
    public FloatBufferWrapper(@NonNull BufferInfo info) {
        if (info.bufferType != BufferInfo.FLOAT_BUFFER || !(info.buffer instanceof FloatBuffer)) {
            throw new IllegalArgumentException("Provided BufferInfo object does not reference a FloatBuffer instance.");
        }
        this.info = info;
    }

    public FloatBuffer getBuffer() {
        return (FloatBuffer) info.buffer;
    }

    /**
     * Returns this buffer's capacity, as viewed through the parameters of the {@link BufferInfo}.
     *
     * @return This buffer's capacity, as viewed through the parameters of the {@link BufferInfo}.
     */
    public int capacity() {
        return info.buffer.capacity();
    }

    public boolean hasRemaining() {
        return info.buffer.hasRemaining();
    }

    public int limit() {
        return info.buffer.limit();
    }

    public int position() {
        return info.buffer.position();
    }

    public void position(int position) {
        info.buffer.position(position);
    }

    public int remaining() {
        return info.buffer.remaining();
    }

    public void rewind() {
        info.buffer.rewind();
    }

    public float get() {
        return ((FloatBuffer) info.buffer).get();
    }

    @NonNull
    public FloatBufferWrapper get(float[] array) {
        ((FloatBuffer) info.buffer).get(array);
        return this;
    }

    @NonNull
    public FloatBufferWrapper get(float[] array, int offset, int length) {
        ((FloatBuffer) info.buffer).get(array, offset, length);
        return this;
    }

    @NonNull
    public float get(int index) {
        return ((FloatBuffer) info.buffer).get(index);
    }

    public void put(int index, float value) {
        ((FloatBuffer) info.buffer).put(index, value);
    }
}
