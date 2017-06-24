package c.org.rajawali3d.util;

import android.support.annotation.NonNull;

import java.nio.FloatBuffer;

import c.org.rajawali3d.gl.buffers.BufferInfo;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */

public class FloatBufferWrapper {

    private final BufferInfo info;

    /**
     * offset + i * stride
     * | 0| 1| 2| 3| 4| 5| 6| 7| 8| 9|10|11|12|13|14|15|16|17|18|19|20|
     * |a0|a1|a2|b0|b1|b2|c0|a3|a4|a5|b3|b4|b5|c1|a6|a7|a8|b6|b7|b8|c2|
     *
     * b offset = 3
     * b stride = 7
     *
     * position = 2
     *
     * position <= offset, next position == offset
     *
     * position = 9
     * position > offset
     * delta = (position + 1) - offset = 7 mod stride = 1;
     *
     * next position for b = position + delta = 9 + 1 = 10
     *
     * position = 13
     * position > offset
     * delta = (position + 1) - offset = 11 mod stride = 4
     *
     * next position for b = position + delta = 13 + 4 = 17
     *
     * position = 10
     * position > offset
     * delta = (10 + 1) - 3 = 8 mod 7 = 1
     *
     * position >= offset + i * stride
     * position - offset >= i * stride
     * i <= (position - offset) / stride
     * i <= (10 - 3) / 7
     * i < = 1
     *
     * @param info
     */
    public FloatBufferWrapper(@NonNull BufferInfo info) {
        this.info = info;
    }

    public void rewind() {
        info.buffer.rewind();
    }

    public boolean hasRemaining() {
        // TODO: Check to make sure that remaining is true with offset and stride accounted for
        if (!info.buffer.hasRemaining()) {
            return false;
        } else {
            final int offset = info.offset;
            final int stride = info.stride;
            final int position = info.buffer.position();
            final int nextPosition =
        }
        return info.buffer.hasRemaining();
    }

    public float get() {
        // TODO: Fetch data with stride/offset
        return 0;
    }

    public float get(int i) {
        return 0;
    }

    public int limit() {
        return 0;
    }

    public void put(int i, float value) {

    }

    public int capacity() {
        return 0;
    }

    @NonNull
    public FloatBufferWrapper get(float[] vertices) {

        return this;
    }

    public void position(int position) {

    }

    public FloatBuffer getBuffer() {
        return (FloatBuffer) info.buffer;
    }
}
