package org.rajawali3d.geometry;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import net.jcip.annotations.NotThreadSafe;

import org.rajawali3d.math.vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import c.org.rajawali3d.gl.buffers.BufferInfo;

import static android.media.CamcorderProfile.get;

/**
 * Implementation of {@link IndexedGeometry} which interleaves some or all vertex data into a single buffer.
 * There is no requirement that any or all data be interleaved, but if no interleaving is done, this class
 * effectively degenerates to {@link NonInterleavedGeometry}. Data which is not expected to change should be
 * interleaved, and all other data should be stored in separate buffers. Data can be interleaved in any order
 * (there is no requirement that it be [position, normal, texture, color] for example). When adding data, you
 * can specify the index within all vertex attributes where it should appear. For example, you can specify
 * position to be index 0, normals index 1, etc. If a gap in indices is left, an error will be generated when
 * building the VBO as this will lead to an indeterminate behavior.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public class InterleavedGeometry extends IndexedGeometry {

    private static final String TAG = "InterleavedGeometry";

    private final SparseArrayCompat<Mapping> byteData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> floatData = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<Mapping> doubleData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> shortData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> intData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> longData = new SparseArrayCompat<>();
    private final SparseArrayCompat<Mapping> charData = new SparseArrayCompat<>();

    private final SparseArrayCompat<AttributeDescriptor> byteDescriptors = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<AttributeDescriptor> floatDescriptors = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<AttributeDescriptor> doubleDescriptors = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<AttributeDescriptor> shortDescriptors = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<AttributeDescriptor> intDescriptors = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<AttributeDescriptor> longDescriptors = new SparseArrayCompat<>(4);
    private final SparseArrayCompat<AttributeDescriptor> charDescriptors = new SparseArrayCompat<>(4);

    private int byteBufferKey = -1;
    private int floatBufferKey = -1;
    private int doubleBufferKey = -1;
    private int shortBufferKey = -1;
    private int intBufferKey = -1;
    private int longBufferKey = -1;
    private int charBufferKey = -1;

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
     * References to this class should not be retained as it will go out of scope if the geometry's data goes out of scope.
     */
    public static class AttributeDescriptor {

        final BufferInfo bufferInfo;
        final int offset;
        final int stride;

        int attribute;

        public AttributeDescriptor(@NonNull BufferInfo bufferInfo, @IntRange(from = 0) int offset,
                                   @IntRange(from = 0) int stride) {
            this.bufferInfo = bufferInfo;
            this.offset = offset;
            this.stride = stride;
        }
    }

    /**
     * The number of indices currently stored in the index buffer.
     */
    private int numVertices;

    public void addInterleavedData(@NonNull float[] data, @IntRange(from = 0) int index, @IntRange(from = 0) int stride) {
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
    protected int getVertexCount() {
        return numVertices;
    }

    @Override
    protected boolean hasVertexData() {
        return false;
    }

    @Nullable
    @Override
    protected BufferInfo getVertexBufferInfo() {
        return null;
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
            byteDescriptors.clear();
            // Add all the float data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < byteData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = byteData.get(i);
                    final byte[] data = (byte[]) mapping.data;
                    AttributeDescriptor descriptor = null;
                    if (byteDescriptors.get(i) == null) {
                        // Build and store an AttributeDescriptor if needed
                        descriptor = new AttributeDescriptor(info, buffer.position(), data.length);
                        byteDescriptors.put(i, descriptor);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            byteBufferKey = addBuffer(info);
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
            floatDescriptors.clear();
            // Add all the float data to the buffer
            while (buffer.hasRemaining()) {
                ++numVertices;
                // Walk through each set of data for each vertex
                for (int i = 0; i < floatData.size(); ++i) {
                    // We can be sure the index and key equate now
                    final Mapping mapping = floatData.get(i);
                    final float[] data = (float[]) mapping.data;
                    AttributeDescriptor descriptor = null;
                    if (floatDescriptors.get(i) == null) {
                        // Build and store an AttributeDescriptor if needed
                        descriptor = new AttributeDescriptor(info, buffer.position(), data.length);
                        floatDescriptors.put(i, descriptor);
                    }
                    // Add the data for this attribute to the vertex
                    buffer.put(data, mapping.offset, mapping.stride);
                    mapping.offset += mapping.stride;
                }
            }

            // Add the interleaved buffer
            floatBufferKey = addBuffer(info);
        } else {
            Log.d(TAG, "No float data to interleave.");
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
            bufferSize += doubleData.get(i).length;
        }

        // Create a double buffer to hold the data
        if (bufferSize > 0) {
            final DoubleBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asDoubleBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.DOUBLE_BUFFER, buffer);

            // Add all the double data to the buffer
            for (int i = 0; i < doubleData.size(); ++i) {
                // We can be sure the index and key equate now
                buffer.put(doubleData.get(i));
            }

            // Add the interleaved buffer
            doubleBufferKey = addBuffer(info);
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
            bufferSize += shortData.get(i).length;
        }

        // Create a short buffer to hold the data
        if (bufferSize > 0) {
            final ShortBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asShortBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.SHORT_BUFFER, buffer);

            // Add all the short data to the buffer
            for (int i = 0; i < shortData.size(); ++i) {
                // We can be sure the index and key equate now
                buffer.put(shortData.get(i));
            }

            // Add the interleaved buffer
            shortBufferKey = addBuffer(info);
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
            bufferSize += intData.get(i).length;
        }

        // Create a int buffer to hold the data
        if (bufferSize > 0) {
            final IntBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asIntBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.INT_BUFFER, buffer);

            // Add all the int data to the buffer
            for (int i = 0; i < intData.size(); ++i) {
                // We can be sure the index and key equate now
                buffer.put(intData.get(i));
            }

            // Add the interleaved buffer
            intBufferKey = addBuffer(info);
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
            bufferSize += longData.get(i).length;
        }

        // Create a long buffer to hold the data
        if (bufferSize > 0) {
            final LongBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asLongBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.LONG_BUFFER, buffer);

            // Add all the long data to the buffer
            for (int i = 0; i < longData.size(); ++i) {
                // We can be sure the index and key equate now
                buffer.put(longData.get(i));
            }

            // Add the interleaved buffer
            longBufferKey = addBuffer(info);
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
            bufferSize += charData.get(i).length;
        }

        // Create a char buffer to hold the data
        if (bufferSize > 0) {
            final CharBuffer buffer = ByteBuffer.allocateDirect(bufferSize).asCharBuffer();
            final BufferInfo info = new BufferInfo(BufferInfo.CHAR_BUFFER, buffer);

            // Add all the char data to the buffer
            for (int i = 0; i < charData.size(); ++i) {
                // We can be sure the index and key equate now
                buffer.put(charData.get(i));
            }

            // Add the interleaved buffer
            charBufferKey = addBuffer(info);
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
