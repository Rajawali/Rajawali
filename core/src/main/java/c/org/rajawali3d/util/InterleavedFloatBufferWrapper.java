package c.org.rajawali3d.util;

import android.support.annotation.NonNull;
import c.org.rajawali3d.gl.buffers.BufferInfo;

import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;

/**
 * Implementation of {@link FloatBufferWrapper} which allows for interleaving data into a single {@link FloatBuffer}.
 * It is intended that a new {@link InterleavedFloatBufferWrapper} will be created for each "view" of the buffer, and
 * along with it the necessary {@link BufferInfo} which describes how to move through the {@link FloatBuffer} to skip
 * interleaved data.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class InterleavedFloatBufferWrapper extends FloatBufferWrapper {

    private final int capacity;
    private int position = 0;
    private int bufferPosition;

    public InterleavedFloatBufferWrapper(@NonNull BufferInfo info) {
        super(info);
        bufferPosition = info.offset;
        capacity = info.buffer.capacity() / info.stride * info.count;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean hasRemaining() {
        if (!info.buffer.hasRemaining()) {
            return false;
        } else {
            return (info.buffer.capacity() >= bufferPosition(info, position + 1));
        }
    }

    @Override
    public int limit() {
        return capacity;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public void position(int position) {
        this.position = position;
        bufferPosition = bufferPosition(info, position);
    }

    @Override
    public int remaining() {
        return (capacity - position);
    }

    @Override
    public void rewind() {
        position = 0;
    }

    @Override
    public float get() {
        final float retval = ((FloatBuffer) info.buffer).get(bufferPosition);
        position(position + 1); // We use the setter to ensure that the bufferPosition field is updated properly
        return retval;
    }

    @Override
    @NonNull
    public FloatBufferWrapper get(float[] array) {
        return get(array, 0, array.length);
    }

    @Override
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

    @Override
    public float get(int index) {
        return ((FloatBuffer) info.buffer).get(bufferPosition(info, index));
    }

    @Override
    public void put(int index, float value) {
        ((FloatBuffer) info.buffer).put(bufferPosition(info, index), value);
    }
}
