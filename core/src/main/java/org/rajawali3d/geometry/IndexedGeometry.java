package org.rajawali3d.geometry;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.gl.buffers.BufferInfo;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Implementation of {@link VBOGeometry} which uses element indices to represent it's primitives. This implementation
 * expects that vertex data will be present prior to the element indices being set. Unfortunately, the creators of Java
 * thought they were doing us a favor by making it impossible to use unsigned integers. As a result, even though OpenGL
 * could in theory support a max index value of 4294967294, we can only support 2147483646. This is because we cant use
 * anything larger than an {@code int} to index into an array or {@link Buffer}. Granted, if you have that many vertices
 * in a geometry, you will probably melt down your GPU.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class IndexedGeometry extends VBOGeometry {

    private static final String TAG = "IndexedGeometry";

    private static final long MAX_COUNT_UNSIGNED_BYTE  = 256;
    private static final long MAX_COUNT_UNSIGNED_SHORT = 65536;
    private static final long MAX_COUNT_UNSIGNED_INT   = 4294967295L;

    /**
     * The internal buffer key used for the index VBO.
     */
    private int indexBufferKey = -1;

    /**
     * The number of indices currently stored in the index buffer.
     */
    private int numIndices;

    /**
     * Returns the number of vertices contained within this geometry. Implementation is delegated to subclasses as
     * the meaning of this will change depending on if the geometry is interleaved or not.
     *
     * @return {@code int} The vertex count.
     */
    protected abstract int getVertexCount();

    /**
     * Checks if a valid {@link BufferInfo} exists for vertex data.
     *
     * @return {@code true} if there is valid {@link BufferInfo} for the vertex data.
     */
    protected abstract boolean hasVertexData();

    /**
     * Fetches the {@link BufferInfo} which contains the vertex data. This may be a unique buffer or an interleaved
     * buffer.
     *
     * @return The {@link BufferInfo} containing the vertex data.
     */
    @Nullable
    protected abstract BufferInfo getVertexBufferInfo();

    /**
     * Copies another {@link IndexedGeometry}'s BufferInfo object. This means that it doesn't copy or clone the actual
     * data, but rather will just use the pointers to the other {@link IndexedGeometry}'s buffers.
     *
     * @param geometry The {@link IndexedGeometry} to copy from.
     *
     * @see BufferInfo
     */
    public void copyFrom(@NonNull IndexedGeometry geometry) {
        final BufferInfo info = geometry.getIndexBufferInfo();
        if (info != null) {
            if (indexBufferKey < 0) {
                indexBufferKey = addBuffer(info);
            } else {
                setBufferInfo(indexBufferKey, info);
            }
        }
        numIndices = geometry.getNumberIndices();
    }

    @SuppressWarnings("ConstantConditions")
    @RenderThread
    @Override
    public boolean isValid() {
        // Indexed geometry requires a minimum of vertex positions and element indices, so we validate against those
        // We don't need a defensive check here because `hasBuffer()` checks for null
        return hasVertexData() && hasBuffer(indexBufferKey)
               && GLES20.glIsBuffer(getVertexBufferInfo().glHandle)
               && GLES20.glIsBuffer(getBufferInfo(indexBufferKey).glHandle);
    }

    @Override
    public int getTriangleCount() {
        return numIndices / 3;
    }

    /**
     * Sets the element index data. If there is already current data, it will be overwritten. No size checking is
     * made and it is assumed the new data will fit in the existing buffers capacity. See class description for a
     * description of limitations. The data type of of the element indices is determined automatically by using the
     * smallest type which can contain the number of vertices, therefore, you must ensure that vertex data can be
     * found before calling this. If there is existing data which is of a different data type (you are
     * adding/removing a number of vertices sufficient to transition between byte and short indices for example), it
     * will force a replacement of any existing data. See class description for a description of limitations.
     *
     * @param indices {@code int[]} The element index data.
     *
     * @throws IllegalStateException if no vertex data is present.
     */
    public void setIndices(@NonNull int[] indices) throws IllegalStateException {
        setIndices(indices, false);
    }

    /**
     * Sets the element index data. If there is already current data, it will be overwritten. No size checking is
     * made and it is assumed the new data will fit in the existing buffers capacity. Optionally, you may elect to
     * replace all data, allocating a new buffer. The data type of of the element indices is determined automatically by
     * using the smallest type which can contain the number of vertices, therefore, you must ensure that vertex data can
     * be found before calling this. If there is existing data which is of a different data type (you are
     * adding/removing a number of vertices sufficient to transition between byte and short indices for example), it
     * will force a replacement of any existing data. See class description for a description of limitations.
     *
     * @param indices     {@code int[]} The element index data.
     * @param replaceData If {@code true}, it will force a replacement of any existing buffer data.
     *
     * @throws IllegalStateException if no vertex data is present.
     */
    public void setIndices(@NonNull int[] indices, boolean replaceData) throws IllegalStateException {
        if (!hasVertexData()) {
            throw new IllegalStateException("Vertex data must be present before setting element indices.");
        }
        final int indexCount = getVertexCount();
        if (indexCount < MAX_COUNT_UNSIGNED_BYTE) {
            // We can treat the indices as bytes
            setByteIndices(indices, replaceData);
        } else if (indexCount < MAX_COUNT_UNSIGNED_SHORT) {
            // We can treat the indices as shorts
            setShortIndices(indices, replaceData);
        } else {
            // We must treat them as integers
            setIntIndices(indices, replaceData);
        }
        numIndices = indices.length;
    }

    /**
     * Fetches the {@link Buffer} containing the element index data. This could be either a {@link ByteBuffer},
     * {@link ShortBuffer} or {@link IntBuffer}, and callers will need to check accordingly.
     *
     * @return The {@link Buffer} of element index data.
     */
    @Nullable
    public Buffer getIndices() {
        final BufferInfo info = getBufferInfo(indexBufferKey);
        if (info == null) {
            return null;
        } else {
            return info.buffer;
        }
    }

    /**
     * Gets the number of indices.
     *
     * @return {@code int} Element index count.
     */
    public int getNumberIndices() {
        return numIndices;
    }

    /**
     * Fetches the {@link BufferInfo} which contains the element index data.
     *
     * @return The {@link BufferInfo} containing the element index data.
     */
    @Nullable
    public BufferInfo getIndexBufferInfo() {
        return getBufferInfo(indexBufferKey);
    }


    /**
     * Sets the {@link BufferInfo} which contains the element index data.
     *
     * @param indexBufferInfo  The {@link BufferInfo} containing the element index data.
     */
    public void setIndexBufferInfo(BufferInfo indexBufferInfo) {
        if (indexBufferKey < 0) {
            indexBufferKey = addBuffer(indexBufferInfo);
        } else {
            final BufferInfo newInfo = indexBufferInfo.copyWithKey(indexBufferKey);
            final BufferInfo info = setBufferInfo(indexBufferKey, newInfo);
            // TODO: Cleanup old info if necessary
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @VisibleForTesting
    void setByteIndices(@NonNull int[] indices, boolean replaceData) {
        BufferInfo indexInfo = getBufferInfo(indexBufferKey);
        if (indexInfo == null) {
            indexInfo = new BufferInfo();
            indexInfo.rajawaliHandle = indexBufferKey;
            indexInfo.bufferType = BufferInfo.BYTE_BUFFER;
            indexInfo.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
            indexBufferKey = addBuffer(indexInfo);
        }
        if (replaceData || indexInfo.buffer == null) {
            indexInfo.bufferType = BufferInfo.BYTE_BUFFER;
            indexInfo.buffer = ByteBuffer.allocateDirect(indices.length * BYTE_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder());
            final ByteBuffer buffer = ((ByteBuffer) indexInfo.buffer);
            for (int i = 0; i < indices.length; ++i) {
                buffer.put((byte) (0xFF & indices[i]));
            }
            buffer.rewind();
        } else {
            indexInfo.buffer.rewind();

            // If the buffer types differ, we must forcefully replace the buffer
            if (!(indexInfo.buffer instanceof ByteBuffer)) {
                setByteIndices(indices, true);
                return;
            }
            final ByteBuffer buffer = ((ByteBuffer) indexInfo.buffer);
            for (int i = 0; i < indices.length; ++i) {
                buffer.put((byte) (0xFF & indices[i]));
            }
            buffer.position(0);
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @VisibleForTesting
    void setShortIndices(@NonNull int[] indices, boolean replaceData) {
        BufferInfo indexInfo = getBufferInfo(indexBufferKey);
        if (indexInfo == null) {
            indexInfo = new BufferInfo();
            indexInfo.rajawaliHandle = indexBufferKey;
            indexInfo.bufferType = BufferInfo.SHORT_BUFFER;
            indexInfo.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
            indexBufferKey = addBuffer(indexInfo);
        }
        if (replaceData || indexInfo.buffer == null) {
            indexInfo.bufferType = BufferInfo.SHORT_BUFFER;
            indexInfo.buffer = ByteBuffer.allocateDirect(indices.length * SHORT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asShortBuffer();
            final ShortBuffer buffer = ((ShortBuffer) indexInfo.buffer);
            for (int i = 0; i < indices.length; ++i) {
                buffer.put((short) (0xFFFF & indices[i]));
            }
            buffer.rewind();
        } else {
            indexInfo.buffer.rewind();
            if (!(indexInfo.buffer instanceof ShortBuffer)) {
                setShortIndices(indices, true);
                return;
            }
            final ShortBuffer buffer = ((ShortBuffer) indexInfo.buffer);
            for (int i = 0; i < indices.length; ++i) {
                buffer.put((short) (0xFFFF & indices[i]));
            }
            buffer.position(0);
        }
    }

    @VisibleForTesting
    void setIntIndices(@NonNull int[] indices, boolean replaceData) {
        BufferInfo indexInfo = getBufferInfo(indexBufferKey);
        if (indexInfo == null) {
            indexInfo = new BufferInfo();
            indexInfo.rajawaliHandle = indexBufferKey;
            indexInfo.bufferType = BufferInfo.INT_BUFFER;
            indexInfo.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
            indexBufferKey = addBuffer(indexInfo);
        }
        if (replaceData || indexInfo.buffer == null) {
            indexInfo.bufferType = BufferInfo.INT_BUFFER;
            indexInfo.buffer = ByteBuffer.allocateDirect(indices.length * INT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asIntBuffer();
            ((IntBuffer) indexInfo.buffer).put(indices).rewind();
        } else {
            indexInfo.buffer.rewind();
            if (!(indexInfo.buffer instanceof IntBuffer)) {
                setIntIndices(indices, true);
                return;
            }
            ((IntBuffer) indexInfo.buffer).put(indices);
            indexInfo.buffer.rewind();
        }
    }
}
