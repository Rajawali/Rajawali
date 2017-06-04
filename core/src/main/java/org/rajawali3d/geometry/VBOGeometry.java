package org.rajawali3d.geometry;

import android.opengl.GLES20;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import c.org.rajawali3d.gl.buffers.BufferInfo.BufferType;
import c.org.rajawali3d.gl.buffers.BufferTarget;
import c.org.rajawali3d.gl.buffers.BufferUsage;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * {@link Geometry} implementation which stores all data in one or more Vertex Buffer Objects.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class VBOGeometry implements Geometry {

    private static final String TAG = "VBOGeometry";

    public static final int BYTE_SIZE_BYTES   = 1;
    public static final int FLOAT_SIZE_BYTES  = 4;
    public static final int DOUBLE_SIZE_BYTES = 8;
    public static final int SHORT_SIZE_BYTES  = 2;
    public static final int INT_SIZE_BYTES    = 4;
    public static final int LONG_SIZE_BYTES   = 8;
    public static final int CHAR_SIZE_BYTES   = 2;

    /**
     * Mapping of {@link BufferInfo} objects.
     */
    private final SparseArrayCompat<BufferInfo> buffers;

    /**
     * Boolean to keep track of if the buffers for this geometry have been through their initial creation.
     */
    protected boolean haveCreatedBuffers;

    public VBOGeometry() {
        haveCreatedBuffers = false;
        buffers = new SparseArrayCompat<>(5);
    }

    @RenderThread
    @Override
    public void createBuffers() {
        // For each buffer, compact it to ensure the correct size and create the VBO in GL
        for (int i = 0; i < buffers.size(); ++i) {
            final int key = buffers.keyAt(i);
            final BufferInfo info = buffers.get(key);
            // Defensive check in case someone ignores the non-null requirement
            if (info != null && info.buffer != null) {
                if (info.buffer instanceof ByteBuffer) {
                    ((ByteBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof FloatBuffer) {
                    ((FloatBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof DoubleBuffer) {
                    ((DoubleBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof ShortBuffer) {
                    ((ShortBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof IntBuffer) {
                    ((IntBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof LongBuffer) {
                    ((LongBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof CharBuffer) {
                    ((CharBuffer) info.buffer).compact().position(0);
                }
            }

            // Create the buffer
            createBufferObject(info);
        }

        // Clear any buffer bindings to prevent unexpected errors
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        haveCreatedBuffers = true;
    }

    @RenderThread
    @Override
    public void validateBuffers() {
        if (!haveCreatedBuffers) {
            createBuffers();
            return;
        }

        // TODO: Check for a dirty buffer
        for (int i = 0, j = buffers.size(); i < j; ++i) {
            final BufferInfo info = buffers.get(i);
            if (info != null && info.glHandle == 0) {
                createBufferObject(info);
            }
        }
    }

    @RenderThread
    @Override
    public void reload() {
        validateBuffers();
    }

    @RenderThread
    @Override
    public void destroy() {
        int[] glHandles = new int[buffers.size()];
        int index = 0;
        for (int i = 0; i < buffers.size(); ++i) {
            final int key = buffers.keyAt(i);
            final BufferInfo info = buffers.get(key);
            // Defensive check in case someone ignores the non-null requirement
            if (info != null) {
                glHandles[index++] = info.glHandle;
                if (info.buffer != null) {
                    info.buffer.clear();
                    info.buffer = null;
                }
            }
        }
        GLES20.glDeleteBuffers(index, glHandles, 0);
        buffers.clear();
    }

    /**
     * Creates and uploads a buffer to the GPU assuming the buffer will be used for static drawing only.
     *
     * @param bufferInfo {@link BufferInfo} Handle for the buffer data to create.
     * @param type       {@link BufferType} The data type of the buffer.
     * @param target     {@link BufferTarget} The GL target buffer.
     */
    public void createBufferObject(@NonNull BufferInfo bufferInfo, @BufferType int type, @BufferTarget int target) {
        createBufferObject(bufferInfo, type, target, bufferInfo.usage);
    }

    /**
     * Creates a buffer and uploads it to the GPU.
     *
     * @param bufferInfo {@link BufferInfo} Handle for the buffer data to create.
     * @param type       {@link BufferType} The data type of the buffer.
     * @param target     {@link BufferTarget} The GL target buffer.
     * @param usage      {@link BufferUsage} The usage hint for the buffer.
     */
    public void createBufferObject(@NonNull BufferInfo bufferInfo, @BufferInfo.BufferType int type, @BufferTarget int target,
                                   @BufferUsage int usage) {

        // Determine the byte size per element
        int byteSize = 0;
        if (type == BufferInfo.BYTE_BUFFER) {
            byteSize = BYTE_SIZE_BYTES;
        } else if (type == BufferInfo.FLOAT_BUFFER) {
            byteSize = FLOAT_SIZE_BYTES;
        } else if (type == BufferInfo.DOUBLE_BUFFER) {
            byteSize = DOUBLE_SIZE_BYTES;
        } else if (type == BufferInfo.SHORT_BUFFER) {
            byteSize = SHORT_SIZE_BYTES;
        } else if (type == BufferInfo.INT_BUFFER) {
            byteSize = INT_SIZE_BYTES;
        } else if (type == BufferInfo.LONG_BUFFER) {
            byteSize = LONG_SIZE_BYTES;
        } else if (type == BufferInfo.CHAR_BUFFER) {
            byteSize = CHAR_SIZE_BYTES;
        }

        // Frankly, this shouldn't happen, and if it does, its not our problem, they are doing something they shouldn't.
        if (byteSize == 0) {
            throw new IllegalArgumentException("Unsupported buffer data type: " + type + ". This means you did not "
                                               + "follow the contract.");
        }

        bufferInfo.byteSize = byteSize;

        // Generate the buffer handle
        int buff[] = new int[1];
        GLES20.glGenBuffers(1, buff, 0);
        int handle = buff[0];

        // Push the data
        final Buffer buffer = bufferInfo.buffer;
        if (buffer != null) {
            buffer.rewind();
            GLES20.glBindBuffer(target, handle);
            GLES20.glBufferData(target, buffer.capacity() * byteSize, buffer, usage);
            GLES20.glBindBuffer(target, 0);
        }

        // Store the buffer info
        bufferInfo.glHandle = handle;
        bufferInfo.bufferType = type;
        bufferInfo.target = target;
        bufferInfo.usage = usage;
    }

    /**
     * Creates a buffer and uploads it to the GPU, using the {@link BufferType}, {@link BufferTarget} and
     * {@link BufferUsage} specified in the buffer info.
     *
     * @param bufferInfo {@link BufferInfo} Handle for the buffer data to create.
     */
    public void createBufferObject(@NonNull BufferInfo bufferInfo) {
        createBufferObject(bufferInfo, bufferInfo.bufferType, bufferInfo.target, bufferInfo.usage);
    }

    /**
     * Adds a new VBO to this geometry for tracking. This is useful when custom vertex attributes need to be supplied
     * for things such as animation or other custom effects.
     *
     * @param bufferInfo {@link BufferInfo} Handle for the buffer data to add.
     * @param type       {@link BufferType} The data type of the buffer.
     * @param target     {@link BufferTarget} The GL target buffer.
     * @param usage      {@link BufferUsage} The usage hint for the buffer.
     *
     * @return {@code int} The internal engine handle for the buffer data.
     */
    public int addBuffer(@NonNull BufferInfo bufferInfo, @BufferInfo.BufferType int type, @BufferTarget int target,
                         @BufferUsage int usage) {
        createBufferObject(bufferInfo, type, target, usage);
        final int key = buffers.size();
        bufferInfo.rajawaliHandle = key;
        buffers.put(key, bufferInfo);
        return key;
    }

    /**
     * Adds a new VBO to this geometry for tracking. This is useful when custom vertex attributes need to be supplied
     * for things such as animation or other custom effects.
     *
     * @param bufferInfo {@link BufferInfo} Handle for the buffer data to add.
     * @param type       {@link BufferType} The data type of the buffer.
     * @param target     {@link BufferTarget} The GL target buffer.
     *
     * @return {@code int} The internal engine handle for the buffer data.
     */
    public int addBuffer(@NonNull BufferInfo bufferInfo, @BufferInfo.BufferType int type, @BufferTarget int target) {
        return addBuffer(bufferInfo, type, target, bufferInfo.usage);
    }

    /**
     * Adds a new VBO to this geometry for tracking. This is useful when custom vertex attributes need to be supplied
     * for things such as animation or other custom effects.
     *
     * @param bufferInfo {@link BufferInfo} Handle for the buffer data to add.
     *
     * @return {@code int} The internal engine handle for the buffer data.
     */
    public int addBuffer(@NonNull BufferInfo bufferInfo) {
        return addBuffer(bufferInfo, bufferInfo.bufferType, bufferInfo.target, bufferInfo.usage);
    }

    /**
     * Specifies the expected usage pattern of the data store. The symbolic constant must be
     * GLES20.GL_STREAM_DRAW, GLES20.GL_STREAM_READ, GLES20.GL_STREAM_COPY, GLES20.GL_STATIC_DRAW,
     * GLES20.GL_STATIC_READ, GLES20.GL_STATIC_COPY, GLES20.GL_DYNAMIC_DRAW, GLES20.GL_DYNAMIC_READ,
     * or GLES20.GL_DYNAMIC_COPY.
     * <p>
     * Usage is a hint to the GL implementation as to how a buffer object's data store will be
     * accessed. This enables the GL implementation to make more intelligent decisions that may
     * significantly impact buffer object performance. It does not, however, constrain the actual
     * usage of the data store. usage can be broken down into two parts: first, the frequency of
     * access (modification and usage), and second, the nature of that access. The frequency of
     * access may be one of these:
     * <p>
     * STREAM
     * The data store contents will be modified once and used at most a few times.
     * <p>
     * STATIC
     * The data store contents will be modified once and used many times.
     * <p>
     * DYNAMIC
     * The data store contents will be modified repeatedly and used many times.
     * <p>
     * The nature of access may be one of these:
     * <p>
     * DRAW
     * The data store contents are modified by the application, and used as the source for GL drawing and image
     * specification commands.
     * <p>
     * READ
     * The data store contents are modified by reading data from the GL, and used to return that data when queried by
     * the application.
     * <p>
     * COPY
     * The data store contents are modified by reading data from the GL, and used as the source for GL drawing and
     * image specification commands.
     *
     * @param bufferInfo The {@link BufferInfo} handle for the buffer to be changed.
     * @param usage      {@link BufferUsage} The new buffer usage.
     */
    @RenderThread
    public void changeBufferUsage(@NonNull BufferInfo bufferInfo, @BufferUsage int usage) {
        GLES20.glDeleteBuffers(1, new int[]{ bufferInfo.glHandle }, 0);
        createBufferObject(bufferInfo, bufferInfo.bufferType, bufferInfo.target, usage);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length. Note that no check is
     * performed to prevent a buffer overrun in Open GL, the results of which are up to the GL platform.
     *
     * @param bufferInfo The {@link BufferInfo} handle for the buffer to be changed.
     * @param newData    {@link Buffer} containing the new data to push.
     * @param index      The starting index in the existing buffer for the new data.
     *
     * @throws IllegalArgumentException Thrown if either buffer is an unsupported type.
     */
    @RenderThread
    public void changeBufferData(@NonNull BufferInfo bufferInfo, @NonNull Buffer newData, int index)
            throws IllegalArgumentException {
        changeBufferData(bufferInfo, newData, index, newData.capacity());
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length. Note that no check is
     * performed to prevent a buffer overrun in Open GL, the results of which are up to the GL platform.
     *
     * @param bufferInfo The {@link BufferInfo} handle for the buffer to be changed.
     * @param newData    {@link Buffer} containing the new data to push.
     * @param index      The starting index in the existing buffer for the new data.
     * @param count      The number of elements to change in the buffer. If {@code resizeBuffer} is {@code true} this
     *                   parameter is the element count of the new buffer.
     *
     * @throws IllegalArgumentException Thrown if either buffer is an unsupported type.
     */
    @RenderThread
    public void changeBufferData(@NonNull BufferInfo bufferInfo, @NonNull Buffer newData, int index, int count)
            throws IllegalArgumentException {
        changeBufferData(bufferInfo, newData, index, count, false);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the extent of the new data, optionally
     * resizing the buffer as needed. Note that no check is performed to prevent a buffer overrun in Open GL, the
     * results of which are up to the GL platform.
     *
     * @param bufferInfo   The {@link BufferInfo} handle for the buffer to be changed.
     * @param newData      {@link Buffer} containing the new data to push.
     * @param index        The starting index in the existing buffer for the new data.
     * @param resizeBuffer If {@code true}, the existing buffer will be replaced to end at {@code count} elements.
     *
     * @throws IllegalArgumentException Thrown if either buffer is an unsupported type.
     */
    @RenderThread
    public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index, boolean resizeBuffer)
            throws IllegalArgumentException {
        changeBufferData(bufferInfo, newData, index, newData.capacity(), resizeBuffer);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length, optionally resizing
     * the buffer as needed. Note that no check is performed to prevent a buffer overrun in Open GL, the results of
     * which are up to the GL platform.
     *
     * @param bufferInfo   The {@link BufferInfo} handle for the buffer to be changed.
     * @param newData      {@link Buffer} containing the new data to push.
     * @param index        The starting index in the existing buffer for the new data.
     * @param count        The number of elements to change in the buffer. If {@code resizeBuffer} is {@code true} this
     *                     parameter is the element count of the new buffer.
     * @param resizeBuffer If {@code true}, the existing buffer will be replaced to end at {@code count} elements.
     *
     * @throws IllegalArgumentException Thrown if either buffer is an unsupported type.
     */
    @RenderThread
    public void changeBufferData(@NonNull BufferInfo bufferInfo, @NonNull Buffer newData, int index, int count,
                                 boolean resizeBuffer) throws IllegalArgumentException {
        newData.rewind();
        GLES20.glBindBuffer(bufferInfo.target, bufferInfo.glHandle);
        if (resizeBuffer) {
            if (index == 0) {
                // If we are starting from 0, just replace the data
                bufferInfo.buffer = newData;
                GLES20.glBufferData(bufferInfo.target, count * bufferInfo.byteSize, newData, bufferInfo.usage);
            } else {
                // Otherwise build a resized buffer
                bufferInfo.buffer = buildResizedBuffer(bufferInfo.buffer, newData, index, count);
            }
        } else {
            // We aren't supposed to resize, so replace the data range
            GLES20.glBufferSubData(bufferInfo.target, index * bufferInfo.byteSize, count * bufferInfo.byteSize,
                                   newData);
        }
        // Clear the buffer binding to avoid unexpected behaviors
        GLES20.glBindBuffer(bufferInfo.target, 0);
    }

    /**
     * Fetches the {@link BufferInfo} mapped to the provided buffer key, or {@code null} if no such mapping exists.
     *
     * @param bufferKey {@code int} The buffer key to look up.
     * @return The mapped {@link BufferInfo} or {@code null} if no such mapping exists.
     */
    @Nullable
    protected BufferInfo getBufferInfo(int bufferKey) {
        return buffers.get(bufferKey);
    }

    /**
     * Sets the {@link BufferInfo} for the specified buffer key, replacing any existing info that was present. The
     * previous mapping, if any, is returned so proper cleanup can be managed.
     *
     * @param bufferKey  {@code int} The buffer key to set. Must be greater than or equal to 0.
     * @param bufferInfo {@link BufferInfo} The new buffer info handle.
     */
    @Nullable
    protected BufferInfo setBufferInfo(@IntRange(from = 0) int bufferKey, @NonNull BufferInfo bufferInfo) {
        final BufferInfo retval = buffers.get(bufferKey);
        buffers.put(bufferKey, bufferInfo);
        return retval;
    }

    /**
     * Adds a list of the GL handles for all buffers tracked by this geometry to the provided {@link StringBuilder}.
     *
     * @param builder The {@link StringBuilder} to write to.
     */
    protected void addBufferHandles(@NonNull StringBuilder builder) {
        // Inheriting classes can use/replace this as an opportunity to add their buffers, if any
        for (int i = 0; i < buffers.size(); ++i) {
            final int key = buffers.keyAt(i);
            builder.append("Custom Buffer Handle: ").append(buffers.get(key).glHandle).append("\n");
        }
    }

    /**
     * Checks if {@link BufferInfo} exists for the specified buffer key.
     *
     * @param bufferKey {@code int} The buffer key to check.
     *
     * @return {@code true} if {@link BufferInfo} exists for the specified buffer key.
     */
    protected final boolean hasBuffer(int bufferKey) {
        return (buffers.indexOfKey(bufferKey) >= 0 && buffers.get(bufferKey) != null);
    }

    /**
     * Takes two buffers, one an original, the other replacement data and creates a new buffer using the specified
     * amount of the old plus all of the new.
     *
     * @param oldBuffer The original data {@link Buffer}.
     * @param newBuffer The new data {@link Buffer}.
     * @param index     The offset into the original data buffer to insert the new data at. All old data before this
     *                  index will be retained.
     * @param count     The number of elements from the new data to use. If the combination of offset and count does not
     *                  exceed the count of the original data buffer, all non-overlapped data will be retained.
     *
     * @return The resized {@link Buffer}.
     *
     * @throws IllegalArgumentException Thrown if either buffer is an unsupported type.
     */
    @NonNull
    private static Buffer buildResizedBuffer(@NonNull Buffer oldBuffer, @NonNull Buffer newBuffer, int index,
                                             int count) throws IllegalArgumentException {
        if (oldBuffer instanceof ByteBuffer) {
            ByteBuffer old = (ByteBuffer) oldBuffer;
            ByteBuffer buffer;
            if (old.capacity() < index + count) {
                buffer = ByteBuffer.allocate(index + count);
                oldBuffer.rewind();
                newBuffer.rewind();
                while (old.position() < index) {
                    buffer.put(old.get());
                }
            } else {
                buffer = old;
                buffer.position(index);
            }
            buffer.put((ByteBuffer) newBuffer);
            return buffer;
        } else if (oldBuffer instanceof ShortBuffer) {
            ShortBuffer old = (ShortBuffer) oldBuffer;
            ShortBuffer buffer;
            if (old.capacity() < index + count) {
                buffer = ShortBuffer.allocate(index + count);
                oldBuffer.rewind();
                newBuffer.rewind();
                while (old.position() < index) {
                    buffer.put(old.get());
                }
            } else {
                buffer = old;
                buffer.position(index);
            }
            buffer.put((ShortBuffer) newBuffer);
            return buffer;
        } else if (oldBuffer instanceof IntBuffer) {
            IntBuffer old = (IntBuffer) oldBuffer;
            IntBuffer buffer;
            if (old.capacity() < index + count) {
                buffer = IntBuffer.allocate(index + count);
                oldBuffer.rewind();
                newBuffer.rewind();
                while (old.position() < index) {
                    buffer.put(old.get());
                }
            } else {
                buffer = old;
                buffer.position(index);
            }
            buffer.put((IntBuffer) newBuffer);
            return buffer;
        } else if (oldBuffer instanceof FloatBuffer) {
            FloatBuffer old = (FloatBuffer) oldBuffer;
            FloatBuffer buffer;
            if (old.capacity() < index + count) {
                buffer = FloatBuffer.allocate(index + count);
                oldBuffer.rewind();
                newBuffer.rewind();
                while (old.position() < index) {
                    buffer.put(old.get());
                }
            } else {
                buffer = old;
                buffer.position(index);
            }
            buffer.put((FloatBuffer) newBuffer);
            return buffer;
        } else if (oldBuffer instanceof DoubleBuffer) {
            DoubleBuffer old = (DoubleBuffer) oldBuffer;
            DoubleBuffer buffer;
            if (old.capacity() < index + count) {
                buffer = DoubleBuffer.allocate(index + count);
                oldBuffer.rewind();
                newBuffer.rewind();
                while (old.position() < index) {
                    buffer.put(old.get());
                }
            } else {
                buffer = old;
                buffer.position(index);
            }
            buffer.put((DoubleBuffer) newBuffer);
            return buffer;
        } else {
            throw new IllegalArgumentException("Unsupported Buffer Type: " + oldBuffer.getClass().getName());
        }
    }
}
