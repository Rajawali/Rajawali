package org.rajawali3d.geometry;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.math.vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * Implementation of {@link IndexedGeometry} which interleaves some or all vertex data into a single buffer. There is
 * no requirement that any or all data be interleaved, but if no interleaving is done, this class effectively
 * degenerates to {@link NonInterleavedGeometry}. Data which will not expected to change should be interleaved, and
 * all other data should be stored in separate buffers. Data can be interleaved in any order (there is no requirement
 * that it be [position, normal, texture, color] for example). When adding data, you can specify the index within all
 * vertex attributes where it should appear. For example, you can specify position to be index 0, normals index 1,
 * etc. If a gap in indices is left, an error will be generated when building the VBO as this will lead to an
 * indeterminate behavior. For simplicity of implementation, no means to change interleaved data is supplied.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public class InterleavedGeometry extends NonInterleavedGeometry {

    private static final String TAG = "InterleavedGeometry";

    private final SparseArrayCompat<Mapping> byteData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> floatData = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<Mapping> doubleData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> shortData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> intData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> longData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> charData = new SparseArrayCompat<>();

    private final SparseArrayCompat<BufferInfo> byteBufferInfos   = new SparseArrayCompat<>();
    private final SparseArrayCompat<BufferInfo> floatBufferInfos  = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<BufferInfo> doubleBufferInfos = new SparseArrayCompat<>();
    private final SparseArrayCompat<BufferInfo> shortBufferInfos  = new SparseArrayCompat<>();
    private final SparseArrayCompat<BufferInfo> intBufferInfos    = new SparseArrayCompat<>();
    private final SparseArrayCompat<BufferInfo> longBufferInfos   = new SparseArrayCompat<>();
    private final SparseArrayCompat<BufferInfo> charBufferInfos   = new SparseArrayCompat<>();

    private BufferInfo byteBufferInfo;
    private BufferInfo floatBufferInfo;
    private BufferInfo doubleBufferInfo;
    private BufferInfo shortBufferInfo;
    private BufferInfo intBufferInfo;
    private BufferInfo longBufferInfo;
    private BufferInfo charBufferInfo;

    private int vertexInterleavedIndex = -1;
    private int normalsInterleavedIndex = -1;
    private int textureInterleavedIndex = -1;
    private int colorsInterleavedIndex = -1;

    /**
     * Indicates whether this geometry contains normals or not.
     */
    protected boolean hasNormals;

    /**
     * Indicates whether this geometry contains texture coordinates or not.
     */
    protected boolean hasTextureCoordinates;

    private int floatIndex = 0;

    private static class Mapping {

        final Object data;
        final int stride;

        int offset;

        public Mapping(@NonNull Object data, @IntRange(from = 0) int stride) {
            this.data = data;
            this.stride = stride;
            offset = 0;
        }
    }

    /**
     * The number of indices currently stored in the index buffer.
     */
    private int numVertices;

    public void addInterleavedData(@NonNull float[] data, @IntRange(from = 0) int index,
                                   @IntRange(from = 0) int stride) {
        floatData.put(index, new Mapping(data, stride));
    }

    @Override
    public void createBuffers() {
        buildInterleavedByteData();
        buildInterleavedFloatData();
        buildInterleavedDoubleData();
        buildInterleavedShortData();
        buildInterleavedIntData();
        buildInterleavedLongData();
        buildInterleavedCharData();

        super.createBuffers();
    }

    @Override
    public void calculateAABounds(@NonNull Vector3 min, @NonNull Vector3 max) {

    }

    @Override
    public void issueDrawCalls() {

    }

    @Override
    @Nullable
    public FloatBuffer getVertices() {
        //TODO
        return super.getVertices();
    }

    @Override
    @Nullable
    public FloatBuffer getNormals() {
        //TODO
        return super.getNormals();
    }

    @Override
    @Nullable
    public FloatBuffer getTextureCoords() {
        //TODO
        return super.getTextureCoords();
    }

    @Override
    @Nullable
    public FloatBuffer getColors() {
        //TODO
        return super.getColors();
    }

    public void setVertices(@NonNull float[] vertices, boolean override, boolean interleaved) {
        if (interleaved) {
            vertexInterleavedIndex = floatIndex++;
            //noinspection Range
            addInterleavedData(vertices, vertexInterleavedIndex, 3);
        } else {
            super.setVertices(vertices, override);
        }
    }

    public void setNormals(@NonNull float[] normals, boolean override, boolean interleaved) {
        if (interleaved) {
            normalsInterleavedIndex = floatIndex++;
            //noinspection Range
            addInterleavedData(normals, normalsInterleavedIndex, 3);
        } else {
            setNormals(normals, override);
        }
    }

    public void setTextureCoords(@NonNull float[] textureCoords, boolean override, boolean interleaved) {
        if (interleaved) {
            textureInterleavedIndex = floatIndex++;
            //noinspection Range
            addInterleavedData(textureCoords, textureInterleavedIndex, 2);
        } else {
            setTextureCoords(textureCoords, override);
        }
    }

    @Override
    protected int getVertexCount() {
        return numVertices;
    }

    @Override
    protected boolean hasVertexData() {
        return false;
    }

    @Nullable
    @Override
    public BufferInfo getVertexBufferInfo() {
        if (vertexInterleavedIndex < 0) {
            return super.getVertexBufferInfo();
        } else {
            return floatBufferInfos.get(vertexInterleavedIndex);
        }
    }

    protected void onByteBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        Log.d(TAG, "Default InterleavedGeometry#onByteBufferInfoConstructed() - NonOp");
    }

    protected void onFloatBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        Log.d(TAG, "Default InterleavedGeometry#onFloatBufferInfoConstructed() - NonOp");
    }

    protected void onDoubleBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        Log.d(TAG, "Default InterleavedGeometry#onDoubleBufferInfoConstructed() - NonOp");
    }

    protected void onShortBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        Log.d(TAG, "Default InterleavedGeometry#onShortBufferInfoConstructed() - NonOp");
    }

    protected void onIntBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        Log.d(TAG, "Default InterleavedGeometry#onIntBufferInfoConstructed() - NonOp");
    }

    protected void onLongBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        Log.d(TAG, "Default InterleavedGeometry#onLongBufferInfoConstructed() - NonOp");
    }

    protected void onCharBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        Log.d(TAG, "Default InterleavedGeometry#onCharBufferInfoConstructed() - NonOp");
    }

    private void buildInterleavedByteData() throws GapInInterleavedDataException {
        // We need to build the interleaved data

        // First check to make sure there are no gaps in keys
        for (int i = 0; i < byteData.size(); ++i) {
            if (byteData.keyAt(i) != i) {
                throw new GapInInterleavedDataException("There is a gap in interleaved byte data at index " + i);
            }
        }

        int bufferSize = 0;
        for (int i = 0; i < byteData.size(); ++i) {
            // Iterate the interleaved data and come up with a VBO size. We can be sure the index and key equate now
            bufferSize += ((byte[]) byteData.get(i).data).length;
        }

        // Create a byte buffer to hold the data
        if (bufferSize > 0) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
            final BufferInfo info = new BufferInfo(BufferInfo.BYTE_BUFFER, buffer);

            // Clear any old interleaving info
            numVertices = 0;
            byteBufferInfos.clear();
            // Add all the float data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < byteData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = byteData.get(i);
                    final byte[] data = (byte[]) mapping.data;
                    BufferInfo bufferInfo;
                    if (byteBufferInfos.get(i) == null) {
                        // Build and store a BufferInfo if needed
                        bufferInfo = new BufferInfo(BufferInfo.BYTE_BUFFER, buffer);
                        bufferInfo.offset = buffer.position();
                        bufferInfo.stride = mapping.stride;
                        byteBufferInfos.put(i, bufferInfo);
                        onByteBufferInfoConstructed(i, bufferInfo);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            byteBufferInfo = info;
        } else {
            Log.d(TAG, "No byte data to interleave.");
        }
    }

    private void buildInterleavedFloatData() throws GapInInterleavedDataException {
        // We need to build the interleaved data

        // First check to make sure there are no gaps in keys
        for (int i = 0; i < floatData.size(); ++i) {
            if (floatData.keyAt(i) != i) {
                throw new GapInInterleavedDataException("There is a gap in interleaved float data at index " + i);
            }
        }

        int bufferSize = 0;
        for (int i = 0; i < floatData.size(); ++i) {
            // Iterate the interleaved data and come up with a VBO size. We can be sure the index and key equate now
            bufferSize += ((float[]) floatData.get(i).data).length;
        }

        // Create a float buffer to hold the data
        if (bufferSize > 0) {
            final FloatBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asFloatBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, buffer);

            // Clear any old interleaving info
            numVertices = 0;
            floatBufferInfos.clear();
            // Add all the float data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < floatData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = floatData.get(i);
                    final float[] data = (float[]) mapping.data;
                    BufferInfo bufferInfo;
                    if (floatBufferInfos.get(i) == null) {
                        // Build and store a BufferInfo if needed
                        bufferInfo = new BufferInfo(BufferInfo.FLOAT_BUFFER, buffer);
                        bufferInfo.offset = buffer.position();
                        bufferInfo.stride = mapping.stride;
                        floatBufferInfos.put(i, bufferInfo);
                        floatBufferInfoConstructed(i, bufferInfo);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            floatBufferInfo = info;
        } else {
            Log.d(TAG, "No float data to interleave.");
        }
    }

    @SuppressWarnings("Range")
    private void floatBufferInfoConstructed(@IntRange(from = 0) int key, @NonNull BufferInfo bufferInfo) {
        if (key == vertexInterleavedIndex) {
            setVertexBufferInfo(bufferInfo);
        } else if (key == normalsInterleavedIndex) {
            setNormalBufferInfo(bufferInfo);
        } else if (key == textureInterleavedIndex) {
            setTexCoordBufferInfo(bufferInfo);
        } else if (key == colorsInterleavedIndex) {
            setColorBufferInfo(bufferInfo);
        } else {
            onFloatBufferInfoConstructed(key, bufferInfo);
        }
    }

    private void buildInterleavedDoubleData() throws GapInInterleavedDataException {
        // We need to build the interleaved data

        // First check to make sure there are no gaps in keys
        for (int i = 0; i < doubleData.size(); ++i) {
            if (doubleData.keyAt(i) != i) {
                throw new GapInInterleavedDataException("There is a gap in interleaved double data at index " + i);
            }
        }

        int bufferSize = 0;
        for (int i = 0; i < doubleData.size(); ++i) {
            // Iterate the interleaved data and come up with a VBO size. We can be sure the index and key equate now
            bufferSize += ((double[]) doubleData.get(i).data).length;
        }

        // Create a double buffer to hold the data
        if (bufferSize > 0) {
            final DoubleBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asDoubleBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.DOUBLE_BUFFER, buffer);

            // Clear any old interleaving info
            numVertices = 0;
            doubleBufferInfos.clear();
            // Add all the double data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < doubleData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = doubleData.get(i);
                    final double[] data = (double[]) mapping.data;
                    BufferInfo bufferInfo;
                    if (doubleBufferInfos.get(i) == null) {
                        // Build and store a BufferInfo if needed
                        bufferInfo = new BufferInfo(BufferInfo.DOUBLE_BUFFER, buffer);
                        bufferInfo.offset = buffer.position();
                        bufferInfo.stride = mapping.stride;
                        doubleBufferInfos.put(i, bufferInfo);
                        onDoubleBufferInfoConstructed(i, bufferInfo);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            doubleBufferInfo = info;
        } else {
            Log.d(TAG, "No double data to interleave.");
        }
    }

    private void buildInterleavedShortData() throws GapInInterleavedDataException {
        // We need to build the interleaved data

        // First check to make sure there are no gaps in keys
        for (int i = 0; i < shortData.size(); ++i) {
            if (shortData.keyAt(i) != i) {
                throw new GapInInterleavedDataException("There is a gap in interleaved short data at index " + i);
            }
        }

        int bufferSize = 0;
        for (int i = 0; i < shortData.size(); ++i) {
            // Iterate the interleaved data and come up with a VBO size. We can be sure the index and key equate now
            bufferSize += ((short[]) shortData.get(i).data).length;
        }

        // Create a short buffer to hold the data
        if (bufferSize > 0) {
            final ShortBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asShortBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.SHORT_BUFFER, buffer);

            // Clear any old interleaving info
            numVertices = 0;
            shortBufferInfos.clear();
            // Add all the short data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < shortData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = shortData.get(i);
                    final short[] data = (short[]) mapping.data;
                    BufferInfo bufferInfo;
                    if (shortBufferInfos.get(i) == null) {
                        // Build and store a BufferInfo if needed
                        bufferInfo = new BufferInfo(BufferInfo.SHORT_BUFFER, buffer);
                        bufferInfo.offset = buffer.position();
                        bufferInfo.stride = mapping.stride;
                        shortBufferInfos.put(i, bufferInfo);
                        onShortBufferInfoConstructed(i, bufferInfo);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            shortBufferInfo = info;
        } else {
            Log.d(TAG, "No short data to interleave.");
        }
    }

    private void buildInterleavedIntData() throws GapInInterleavedDataException {
        // We need to build the interleaved data

        // First check to make sure there are no gaps in keys
        for (int i = 0; i < intData.size(); ++i) {
            if (intData.keyAt(i) != i) {
                throw new GapInInterleavedDataException("There is a gap in interleaved int data at index " + i);
            }
        }

        int bufferSize = 0;
        for (int i = 0; i < intData.size(); ++i) {
            // Iterate the interleaved data and come up with a VBO size. We can be sure the index and key equate now
            bufferSize += ((int[]) intData.get(i).data).length;
        }

        // Create a int buffer to hold the data
        if (bufferSize > 0) {
            final IntBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asIntBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.INT_BUFFER, buffer);

            // Clear any old interleaving info
            numVertices = 0;
            intBufferInfos.clear();
            // Add all the int data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < intData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = intData.get(i);
                    final int[] data = (int[]) mapping.data;
                    BufferInfo bufferInfo;
                    if (intBufferInfos.get(i) == null) {
                        // Build and store a BufferInfo if needed
                        bufferInfo = new BufferInfo(BufferInfo.INT_BUFFER, buffer);
                        bufferInfo.offset = buffer.position();
                        bufferInfo.stride = mapping.stride;
                        intBufferInfos.put(i, bufferInfo);
                        onIntBufferInfoConstructed(i, bufferInfo);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            intBufferInfo = info;
        } else {
            Log.d(TAG, "No int data to interleave.");
        }
    }

    private void buildInterleavedLongData() throws GapInInterleavedDataException {
        // We need to build the interleaved data

        // First check to make sure there are no gaps in keys
        for (int i = 0; i < longData.size(); ++i) {
            if (longData.keyAt(i) != i) {
                throw new GapInInterleavedDataException("There is a gap in interleaved long data at index " + i);
            }
        }

        int bufferSize = 0;
        for (int i = 0; i < longData.size(); ++i) {
            // Iterate the interleaved data and come up with a VBO size. We can be sure the index and key equate now
            bufferSize += ((long[]) longData.get(i).data).length;
        }

        // Create a long buffer to hold the data
        if (bufferSize > 0) {
            final LongBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asLongBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.LONG_BUFFER, buffer);

            // Clear any old interleaving info
            numVertices = 0;
            longBufferInfos.clear();
            // Add all the long data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < longData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = longData.get(i);
                    final long[] data = (long[]) mapping.data;
                    BufferInfo bufferInfo;
                    if (longBufferInfos.get(i) == null) {
                        // Build and store a BufferInfo if needed
                        bufferInfo = new BufferInfo(BufferInfo.LONG_BUFFER, buffer);
                        bufferInfo.offset = buffer.position();
                        bufferInfo.stride = mapping.stride;
                        longBufferInfos.put(i, bufferInfo);
                        onLongBufferInfoConstructed(i, bufferInfo);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            longBufferInfo = info;
        } else {
            Log.d(TAG, "No long data to interleave.");
        }
    }

    private void buildInterleavedCharData() throws GapInInterleavedDataException {
        // We need to build the interleaved data

        // First check to make sure there are no gaps in keys
        for (int i = 0; i < charData.size(); ++i) {
            if (charData.keyAt(i) != i) {
                throw new GapInInterleavedDataException("There is a gap in interleaved char data at index " + i);
            }
        }

        int bufferSize = 0;
        for (int i = 0; i < charData.size(); ++i) {
            // Iterate the interleaved data and come up with a VBO size. We can be sure the index and key equate now
            bufferSize += ((char[]) charData.get(i).data).length;
        }

        // Create a char buffer to hold the data
        if (bufferSize > 0) {
            final CharBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asCharBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.CHAR_BUFFER, buffer);

            // Clear any old interleaving info
            numVertices = 0;
            charBufferInfos.clear();
            // Add all the char data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < charData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = charData.get(i);
                    final char[] data = (char[]) mapping.data;
                    BufferInfo bufferInfo;
                    if (charBufferInfos.get(i) == null) {
                        // Build and store a BufferInfo if needed
                        bufferInfo = new BufferInfo(BufferInfo.CHAR_BUFFER, buffer);
                        bufferInfo.offset = buffer.position();
                        bufferInfo.stride = mapping.stride;
                        charBufferInfos.put(i, bufferInfo);
                        onCharBufferInfoConstructed(i, bufferInfo);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            charBufferInfo = info;
        } else {
            Log.d(TAG, "No char data to interleave.");
        }
    }

    public static class GapInInterleavedDataException extends RuntimeException {

        public GapInInterleavedDataException(String message) {
            super(message);
        }
    }
}
