package c.org.rajawali3d.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import c.org.rajawali3d.gl.buffers.BufferInfo;

import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class FloatBufferWrapper {

    private final BufferInfo info;
    private final int capacity;
    private int position = 0;
    private int bufferPosition;

    @VisibleForTesting
    static int bufferPosition(@NonNull BufferInfo info, @IntRange(from = 0) int position) {
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
        bufferPosition = info.offset;
        capacity = info.buffer.capacity() / info.stride * info.count;
    }

    public int capacity() {
        return capacity;
    }

    public boolean hasRemaining() {
        if (!info.buffer.hasRemaining()) {
            return false;
        } else {
            return (info.buffer.capacity() >= bufferPosition(info, position + 1));
        }
    }

    public int limit() {
        return capacity;
    }

    public int position() {
        return position;
    }

    public void position(int position) {
        this.position = position;
        bufferPosition = bufferPosition(info, position);
    }

    public int remaining() {
        return (capacity - position);
    }

    public void rewind() {
        position = 0;
    }

    public float get() {
        final float retval = ((FloatBuffer) info.buffer).get(bufferPosition);
        position(position + 1); // We use the setter to ensure that the bufferPosition field is updated properly
        return retval;
    }

    @NonNull
    public FloatBufferWrapper get(float[] array) {
        return get(array, 0, array.length);
    }

    @NonNull
    public FloatBufferWrapper get(float[] array, int offset, int length) {
        if (length > remaining()) {
            throw new BufferUnderflowException();
        }
        for (int i = offset; i < offset + length; ++i) {
            array[i] = get();
        }
        return this;
    }

    public float get(int index) {
        return ((FloatBuffer) info.buffer).get(bufferPosition(info, index));
    }

    public void put(int i, float value) {

    }

    public FloatBuffer getBuffer() {
        return (FloatBuffer) info.buffer;
    }
}
