/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d;

import android.graphics.Color;
import android.opengl.GLES20;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.bounds.BoundingSphere;
import org.rajawali3d.math.vector.Vector3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is where the vertex, normal, texture coordinate, color and index data is stored.
 * The data is stored in FloatBuffers, IntBuffers and ShortBuffers. The data is uploaded
 * to the graphics card using Vertex Buffer Objects (VBOs). The data in the FloatBuffers
 * is kept in memory in order to restore the VBOs when the OpenGL context needs to be
 * restored (typically when the application regains focus).
 * <p>
 * An object's Geometry3D and its data can be accessed by calling the getGeometry() and its methods:
 * <pre><code> // Get the geometry instance
 * Geometry3D geom = mMyObject3D.getGeometry();
 * // Get vertices (x, y, z)
 * FloatBuffer verts = geom.getVertices();
 * // Get normals (x, y, z)
 * FloatBuffer normals = geom.getNormals();
 * // Get texture coordinates (u, v)
 * FloatBuffer texCoords = geom.getTextureCoords();
 * // Get colors (r, g, b, a)
 * FloatBuffer colors = geom.getColors();
 * // Get indices.
 * IntBuffer indices = geom.getIndices();
 * </pre></code>
 *
 * @author dennis.ippel
 */
public class Geometry3D {

    public static final int FLOAT_SIZE_BYTES = 4;
    public static final int INT_SIZE_BYTES   = 4;
    public static final int SHORT_SIZE_BYTES = 2;
    public static final int BYTE_SIZE_BYTES  = 1;

    public static final int VERTEX_BUFFER_KEY  = 0;
    public static final int NORMAL_BUFFER_KEY  = 1;
    public static final int TEXTURE_BUFFER_KEY = 2;
    public static final int COLOR_BUFFER_KEY   = 3;
    public static final int INDEX_BUFFER_KEY   = 4;

    protected final ArrayList<BufferInfo> mBuffers;

    /**
     * The number of indices currently stored in the index buffer.
     */
    protected int        mNumIndices;
    /**
     * The number of vertices currently stored in the vertex buffer.
     */
    protected int        mNumVertices;
    /**
     * A pointer to the original geometry. This is not null when the object has been cloned.
     * When cloning a BaseObject3D the data isn't copied over, only the handle to the OpenGL
     * buffers are used.
     */
    protected Geometry3D mOriginalGeometry;

    /**
     * Boolean to keep track of if the buffers for this geometry have been through their initial creation.
     */
    protected boolean mHaveCreatedBuffers;

    /**
     * The bounding box for this geometry. This is used for collision detection.
     */
    protected BoundingBox    mBoundingBox;
    /**
     * The bounding sphere for this geometry. This is used for collision detection.
     */
    protected BoundingSphere mBoundingSphere;
    /**
     * Indicates whether this geometry contains normals or not.
     */
    protected boolean        mHasNormals;
    /**
     * Indicates whether this geometry contains texture coordinates or not.
     */
    protected boolean        mHasTextureCoordinates;

    public enum BufferType {
        FLOAT_BUFFER,
        INT_BUFFER,
        SHORT_BUFFER,
        BYTE_BUFFER
    }

    public Geometry3D() {
        mHaveCreatedBuffers = false;
        mBuffers = new ArrayList<>(8);
        mBuffers.add(new BufferInfo());
        mBuffers.add(new BufferInfo());
        mBuffers.add(new BufferInfo());
        mBuffers.add(new BufferInfo());
        mBuffers.add(new BufferInfo());

        mBuffers.get(VERTEX_BUFFER_KEY).rajawaliHandle = VERTEX_BUFFER_KEY;
        mBuffers.get(VERTEX_BUFFER_KEY).bufferType = BufferType.FLOAT_BUFFER;
        mBuffers.get(VERTEX_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        mBuffers.get(NORMAL_BUFFER_KEY).rajawaliHandle = NORMAL_BUFFER_KEY;
        mBuffers.get(NORMAL_BUFFER_KEY).bufferType = BufferType.FLOAT_BUFFER;
        mBuffers.get(NORMAL_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        mBuffers.get(TEXTURE_BUFFER_KEY).rajawaliHandle = TEXTURE_BUFFER_KEY;
        mBuffers.get(TEXTURE_BUFFER_KEY).bufferType = BufferType.FLOAT_BUFFER;
        mBuffers.get(TEXTURE_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        mBuffers.get(COLOR_BUFFER_KEY).rajawaliHandle = COLOR_BUFFER_KEY;
        mBuffers.get(COLOR_BUFFER_KEY).bufferType = BufferType.FLOAT_BUFFER;
        mBuffers.get(COLOR_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        mBuffers.get(INDEX_BUFFER_KEY).rajawaliHandle = INDEX_BUFFER_KEY;
        mBuffers.get(INDEX_BUFFER_KEY).bufferType = BufferType.INT_BUFFER;
        mBuffers.get(INDEX_BUFFER_KEY).target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
    }

    /**
     * Concatenates a list of float arrays into a single array.
     *
     * @param arrays The arrays.
     *
     * @return The concatenated array.
     *
     * @see <a href=http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java>Stack Overflow</a>
     */
    public static float[] concatAllFloat(float[]... arrays) {
        int totalLength = 0;
        final int subArrayCount = arrays.length;
        for (int i = 0; i < subArrayCount; ++i) {
            totalLength += arrays[i].length;
        }
        float[] result = Arrays.copyOf(arrays[0], totalLength);
        int offset = arrays[0].length;
        for (int i = 1; i < subArrayCount; ++i) {
            System.arraycopy(arrays[i], 0, result, offset, arrays[i].length);
            offset += arrays[i].length;
        }
        return result;
    }

    /**
     * Concatenates a list of int arrays into a single array.
     *
     * @param arrays The arrays.
     *
     * @return The concatenated array.
     *
     * @see <a href=http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java>Stack Overflow</a>
     */
    public static int[] concatAllInt(int[]... arrays) {
        int totalLength = 0;
        final int subArrayCount = arrays.length;
        for (int i = 0; i < subArrayCount; ++i) {
            totalLength += arrays[i].length;
        }
        int[] result = Arrays.copyOf(arrays[0], totalLength);
        int offset = arrays[0].length;
        for (int i = 1; i < subArrayCount; ++i) {
            System.arraycopy(arrays[i], 0, result, offset, arrays[i].length);
            offset += arrays[i].length;
        }
        return result;
    }

    public static float[] getFloatArrayFromBuffer(FloatBuffer buffer) {
        float[] array = new float[0];
        if (buffer != null) {
            if (buffer.hasArray()) {
                array = buffer.array();
            } else {
                buffer.rewind();
                array = new float[buffer.capacity()];
                buffer.get(array);
            }
        }
        return array;
    }

    public static int[] getIntArrayFromBuffer(Buffer buffer) {
        int[] array = new int[0];
        if (buffer != null) {
            if (buffer.hasArray()) {
                array = (int[]) buffer.array();
            } else {
                buffer.rewind();
                array = new int[buffer.capacity()];
                if (buffer instanceof IntBuffer) {
                    ((IntBuffer) buffer).get(array);
                } else if (buffer instanceof ShortBuffer) {
                    int count = 0;
                    while (buffer.hasRemaining()) {
                        array[count] = (int) (((ShortBuffer) buffer).get());
                        ++count;
                    }
                }
            }
        }
        return array;
    }

    /**
     * Copies another Geometry3D's BufferInfo objects. This means that it
     * doesn't copy or clone the actual data. It will just use the pointers
     * to the other Geometry3D's buffers.
     *
     * @param geom
     *
     * @see BufferInfo
     */
    public void copyFromGeometry3D(Geometry3D geom) {
        this.mNumIndices = geom.getNumIndices();
        this.mNumVertices = geom.getNumVertices();

        mBuffers.add(VERTEX_BUFFER_KEY, geom.getVertexBufferInfo());
        mBuffers.add(NORMAL_BUFFER_KEY, geom.getNormalBufferInfo());
        mBuffers.add(TEXTURE_BUFFER_KEY, geom.getTexCoordBufferInfo());
        if (mBuffers.get(COLOR_BUFFER_KEY).buffer == null) {
            mBuffers.add(COLOR_BUFFER_KEY, geom.getColorBufferInfo());
        }
        mBuffers.add(INDEX_BUFFER_KEY, geom.getIndexBufferInfo());
        this.mOriginalGeometry = geom;
        this.mHasNormals = geom.hasNormals();
        this.mHasTextureCoordinates = geom.hasTextureCoordinates();
    }

    /**
     * Adds the geometry from the incoming geometry with the specified offset.
     * Note that the offset is only applied to the vertex positions.
     *
     * @param offset     {@link Vector3} containing the offset in each direction. Can be null.
     * @param geometry   {@link Geometry3D} to be added.
     * @param createVBOs {@code boolean} If true, create the VBOs immediately.
     */
    public void addFromGeometry3D(Vector3 offset, Geometry3D geometry, boolean createVBOs) {
        float[] newVertices = null;
        float[] newNormals = null;
        float[] newColors = null;
        float[] newTextureCoords = null;
        int[] newIntIndices = null;
        float[] mVerticesArray = null;
        float[] mNormalsArray = null;
        float[] mColorsArray = null;
        float[] mTextureCoordsArray = null;
        int[] mIndicesArray = null;

        //Get the old data
        mVerticesArray = getFloatArrayFromBuffer((FloatBuffer) mBuffers.get(VERTEX_BUFFER_KEY).buffer);
        mNormalsArray = getFloatArrayFromBuffer((FloatBuffer) mBuffers.get(NORMAL_BUFFER_KEY).buffer);
        mColorsArray = getFloatArrayFromBuffer((FloatBuffer) mBuffers.get(COLOR_BUFFER_KEY).buffer);
        mTextureCoordsArray = getFloatArrayFromBuffer((FloatBuffer) mBuffers.get(TEXTURE_BUFFER_KEY).buffer);
        mIndicesArray = getIntArrayFromBuffer(mBuffers.get(INDEX_BUFFER_KEY).buffer);

        //Get the new data, offset the vertices
        int axis = 0;
        float[] addVertices = getFloatArrayFromBuffer(geometry.getVertices());
        if (offset != null) {
            for (int i = 0, j = addVertices.length; i < j; ++i) {
                switch (axis) {
                    case 0:
                        addVertices[i] += offset.x;
                        break;
                    case 1:
                        addVertices[i] += offset.y;
                        break;
                    case 2:
                        addVertices[i] += offset.z;
                        break;
                }
                ++axis;
                if (axis > 2) {
                    axis = 0;
                }
            }
        }
        float[] addNormals = getFloatArrayFromBuffer(geometry.getNormals());
        float[] addColors = getFloatArrayFromBuffer(geometry.getColors());
        float[] addTextureCoords = getFloatArrayFromBuffer(geometry.getTextureCoords());
        int[] addIndices = getIntArrayFromBuffer(geometry.getIndices());
        int index_offset = 0;
        if (mVerticesArray != null) {
            index_offset = (mVerticesArray.length / 3);
        }
        if (addIndices != null) {
            for (int i = 0, j = addIndices.length; i < j; ++i) {
                addIndices[i] += index_offset;
            }
        }

        //Concatenate the old and new data
        newVertices = concatAllFloat(mVerticesArray, addVertices);
        newNormals = concatAllFloat(mNormalsArray, addNormals);
        newColors = concatAllFloat(mColorsArray, addColors);
        newTextureCoords = concatAllFloat(mTextureCoordsArray, addTextureCoords);
        newIntIndices = concatAllInt(mIndicesArray, addIndices);

        //Set the new data
        setVertices(newVertices, true);
        setNormals(newNormals, true);
        setTextureCoords(newTextureCoords, true);
        setColors(newColors, true);
        setIndices(newIntIndices, true);

        if (createVBOs) {
            //Create the new buffers
            createBuffers();
        }
    }

    /**
     * Sets the data. This methods takes two BufferInfo objects which means it'll use another
     * Geometry3D instance's data (vertices and normals). The remaining parameters are arrays
     * which will be used to create buffers that are unique to this instance.
     * <p>
     * This is typically used with VertexAnimationObject3D instances.
     *
     * @param vertexBufferInfo
     * @param normalBufferInfo
     * @param textureCoords
     * @param colors
     * @param indices
     * @param createVBOs
     *
     * @see VertexAnimationObject3D
     */
    public void setData(BufferInfo vertexBufferInfo, BufferInfo normalBufferInfo,
                        float[] textureCoords, float[] colors, int[] indices, boolean createVBOs) {
        if (textureCoords == null || textureCoords.length == 0) {
            textureCoords = new float[(mNumVertices / 3) * 2];
        }
        setTextureCoords(textureCoords);
        if (colors == null || colors.length == 0) {
            setColors(0xff000000 + (int) (Math.random() * 0xffffff));
        } else {
            setColors(colors);
        }
        setIndices(indices);

        mBuffers.add(VERTEX_BUFFER_KEY, vertexBufferInfo);
        mBuffers.add(NORMAL_BUFFER_KEY, normalBufferInfo);

        mOriginalGeometry = null;

        if (createVBOs) {
            createBuffers();
        }
    }

    /**
     * Sets the data. Assumes that the data will never be changed and passes GLES20.GL_STATIC_DRAW
     * to the OpenGL context when the buffers are created.
     *
     * @param vertices
     * @param normals
     * @param textureCoords
     * @param colors
     * @param indices
     * @param createVBOs
     *
     * @see GLES20#GL_STATIC_DRAW
     */
    public void setData(float[] vertices, float[] normals,
                        float[] textureCoords, float[] colors, int[] indices, boolean createVBOs) {
        setData(vertices, GLES20.GL_STATIC_DRAW, normals, GLES20.GL_STATIC_DRAW, textureCoords,
                GLES20.GL_STATIC_DRAW, colors, GLES20.GL_STATIC_DRAW, indices, GLES20.GL_STATIC_DRAW, createVBOs);
    }

    /**
     * Sets the data. This method takes an additional parameters that specifies the data used for each buffer.
     * <p>
     * Usage is a hint to the GL implementation as to how a buffer object's data store will be accessed. This enables
     * the GL implementation to make more intelligent decisions that may significantly impact buffer object
     * performance. It does not, however, constrain the actual usage of the data store.
     * <p>
     * Usage can be broken down into two parts: first, the frequency of access (modification and usage), and second,
     * the nature of that access. The frequency of access may be one of these:
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
     * @param vertices
     * @param verticesUsage
     * @param normals
     * @param normalsUsage
     * @param textureCoords
     * @param textureCoordsUsage
     * @param colors
     * @param colorsUsage
     * @param indices
     * @param indicesUsage
     * @param createVBOs
     */
    public void setData(float[] vertices, int verticesUsage, float[] normals, int normalsUsage,
                        float[] textureCoords, int textureCoordsUsage, float[] colors, int colorsUsage,
                        int[] indices, int indicesUsage, boolean createVBOs) {
        mBuffers.get(VERTEX_BUFFER_KEY).usage = verticesUsage;
        mBuffers.get(NORMAL_BUFFER_KEY).usage = normalsUsage;
        mBuffers.get(TEXTURE_BUFFER_KEY).usage = textureCoordsUsage;
        mBuffers.get(COLOR_BUFFER_KEY).usage = colorsUsage;
        mBuffers.get(INDEX_BUFFER_KEY).usage = indicesUsage;
        setVertices(vertices);
        if (normals != null) {
            setNormals(normals);
        }
        if (textureCoords == null || textureCoords.length == 0) {
            textureCoords = new float[(vertices.length / 3) * 2];
        }

        setTextureCoords(textureCoords);
        if (colors != null && colors.length > 0) {
            setColors(colors);
        }
        setIndices(indices);

        if (createVBOs) {
            createBuffers();
        }
    }

    /**
     * Creates the actual Buffer objects.
     */
    public void createBuffers() {

        for (BufferInfo info : mBuffers) {
            if (info.buffer != null) {
                if (info.buffer instanceof FloatBuffer) {
                    ((FloatBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof IntBuffer) {
                    ((IntBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof ShortBuffer) {
                    ((ShortBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof ByteBuffer) {
                    ((ByteBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof DoubleBuffer) {
                    ((DoubleBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof LongBuffer) {
                    ((LongBuffer) info.buffer).compact().position(0);
                } else if (info.buffer instanceof CharBuffer) {
                    ((CharBuffer) info.buffer).compact().position(0);
                }
            }

            createBuffer(info);
        }

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mHaveCreatedBuffers = true;
    }

    /**
     * Reload is typically called whenever the OpenGL context needs to be restored.
     * All buffer data is re-uploaded and a new handle is obtained.
     * It is not recommended to call this function manually.
     */
    public void reload() {
        if (mOriginalGeometry != null) {
            if (!mOriginalGeometry.isValid()) {
                mOriginalGeometry.reload();
            }
            copyFromGeometry3D(mOriginalGeometry);
        }
        createBuffers();
    }

    /**
     * Checks whether the handle to the vertex buffer is still valid or not.
     * The handle typically becomes invalid whenever the OpenGL context is lost.
     * This usually happens when the application regains focus.
     *
     * @return
     */
    public boolean isValid() {
        return GLES20.glIsBuffer(mBuffers.get(VERTEX_BUFFER_KEY).bufferHandle);
    }

    /**
     * Creates the vertex and normal buffers only. This is typically used for a
     * VertexAnimationObject3D's frames.
     *
     * @see VertexAnimationObject3D
     */
    public void createVertexAndNormalBuffersOnly() {
        ((FloatBuffer) mBuffers.get(VERTEX_BUFFER_KEY).buffer).compact().position(0);
        ((FloatBuffer) mBuffers.get(NORMAL_BUFFER_KEY).buffer).compact().position(0);

        createBuffer(mBuffers.get(VERTEX_BUFFER_KEY), BufferType.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);
        createBuffer(mBuffers.get(NORMAL_BUFFER_KEY), BufferType.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Creates a buffer and assumes the buffer will be used for static drawing only.
     *
     * @param bufferInfo
     * @param type
     * @param target
     */
    public void createBuffer(BufferInfo bufferInfo, BufferType type, int target) {
        createBuffer(bufferInfo, type, target, bufferInfo.usage);
    }

    /**
     * Creates a buffer and uploads it to the GPU.
     *
     * @param bufferInfo
     * @param type
     * @param target
     * @param usage
     */
    public void createBuffer(BufferInfo bufferInfo, BufferType type, int target, int usage) {
        int byteSize = FLOAT_SIZE_BYTES;
        if (type == BufferType.SHORT_BUFFER) {
            byteSize = SHORT_SIZE_BYTES;
        } else if (type == BufferType.BYTE_BUFFER) {
            byteSize = BYTE_SIZE_BYTES;
        } else if (type == BufferType.INT_BUFFER) {
            byteSize = INT_SIZE_BYTES;
        }
        //TODO: Other types
        bufferInfo.byteSize = byteSize;

        int buff[] = new int[1];
        GLES20.glGenBuffers(1, buff, 0);

        int handle = buff[0];

        final Buffer buffer = bufferInfo.buffer;

        if (buffer != null) {
            buffer.rewind();
            GLES20.glBindBuffer(target, handle);
            GLES20.glBufferData(target, buffer.capacity() * byteSize, buffer, usage);
            GLES20.glBindBuffer(target, 0);
        }

        bufferInfo.bufferHandle = handle;
        bufferInfo.bufferType = type;
        bufferInfo.target = target;
        bufferInfo.usage = usage;
    }

    public void createBuffer(BufferInfo bufferInfo) {
        createBuffer(bufferInfo, bufferInfo.bufferType, bufferInfo.target, bufferInfo.usage);
    }

    public int addBuffer(BufferInfo bufferInfo, BufferType type, int target, int usage) {
        createBuffer(bufferInfo, type, target, usage);
        final int key = mBuffers.size();
        bufferInfo.rajawaliHandle = key;
        mBuffers.add(bufferInfo);
        return key;
    }

    public int addBuffer(BufferInfo bufferInfo, BufferType type, int target) {
        return addBuffer(bufferInfo, type, target, bufferInfo.usage);
    }

    public void validateBuffers() {
        if (!mHaveCreatedBuffers) {
            createBuffers();
        }
        if (mOriginalGeometry != null) {
            mOriginalGeometry.validateBuffers();
            return;
        }

        for (int i = 0, j = mBuffers.size(); i < j; ++i) {
            final BufferInfo info = mBuffers.get(i);
            if (info != null && info.bufferHandle == 0) {
                createBuffer(info);
            }
        }
    }

    /**
     * Specifies the expected usage pattern of the data store. The symbolic constant must be
     * GLES20.GL_STREAM_DRAW, GLES20.GL_STREAM_READ, GLES20.GL_STREAM_COPY, GLES20.GL_STATIC_DRAW,
     * GLES20.GL_STATIC_READ, GLES20.GL_STATIC_COPY, GLES20.GL_DYNAMIC_DRAW, GLES20.GL_DYNAMIC_READ,
     * or GLES20.GL_DYNAMIC_COPY.
     *
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
     * @param bufferInfo
     * @param usage
     */
    public void changeBufferUsage(BufferInfo bufferInfo, final int usage) {
        GLES20.glDeleteBuffers(1, new int[]{ bufferInfo.bufferHandle }, 0);
        createBuffer(bufferInfo, bufferInfo.bufferType, bufferInfo.target, usage);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length.
     *
     * @param bufferInfo
     * @param newData
     * @param index
     */
    public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index) {
        this.changeBufferData(bufferInfo, newData, index, false);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length.
     *
     * @param bufferInfo
     * @param newData
     * @param index
     * @param size
     */
    public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index, int size) {
        this.changeBufferData(bufferInfo, newData, index, size, false);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length.
     *
     * @param bufferInfo
     * @param newData
     * @param index
     * @param resizeBuffer
     */
    public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index, boolean resizeBuffer) {
        changeBufferData(bufferInfo, newData, index, newData.capacity(), resizeBuffer);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length.
     *
     * @param bufferInfo
     * @param newData
     * @param index
     * @param size
     * @param resizeBuffer
     */
    public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index, int size, boolean resizeBuffer) {
        newData.rewind();

        GLES20.glBindBuffer(bufferInfo.target, bufferInfo.bufferHandle);
        if (resizeBuffer) {
            bufferInfo.buffer = newData;
            GLES20.glBufferData(bufferInfo.target, size * bufferInfo.byteSize, newData, bufferInfo.usage);
        } else {
            GLES20.glBufferSubData(bufferInfo.target, index * bufferInfo.byteSize, size * bufferInfo.byteSize, newData);
        }
        GLES20.glBindBuffer(bufferInfo.target, 0);
    }

    public void setVertices(float[] vertices) {
        setVertices(vertices, false);
    }

    public void setVertices(float[] vertices, boolean override) {
        final BufferInfo vertexInfo = mBuffers.get(VERTEX_BUFFER_KEY);
        if (vertexInfo.buffer == null || override == true) {
            if (vertexInfo.buffer != null) {
                vertexInfo.buffer.clear();
            }
            vertexInfo.buffer = ByteBuffer
                    .allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();

            ((FloatBuffer) vertexInfo.buffer).put(vertices);
            vertexInfo.buffer.position(0);
            mNumVertices = vertices.length / 3;
        } else {
            ((FloatBuffer) vertexInfo.buffer).put(vertices);
        }
    }

    public void setVertices(FloatBuffer vertices) {
        vertices.position(0);
        float[] v = new float[vertices.capacity()];
        vertices.get(v);
        setVertices(v);
    }

    public FloatBuffer getVertices() {
        if (mOriginalGeometry != null) {
            return mOriginalGeometry.getVertices();
        }
        return (FloatBuffer) mBuffers.get(VERTEX_BUFFER_KEY).buffer;
    }

    public void setNormals(float[] normals) {
        setNormals(normals, false);
    }

    public void setNormals(float[] normals, boolean override) {
        if (normals == null) {
            return;
        }
        final BufferInfo normalInfo = mBuffers.get(NORMAL_BUFFER_KEY);
        if (normalInfo.buffer == null || override == true) {
            normalInfo.buffer = ByteBuffer.allocateDirect(normals.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) normalInfo.buffer).put(normals);
            normalInfo.buffer.position(0);
        } else {
            normalInfo.buffer.position(0);
            ((FloatBuffer) normalInfo.buffer).put(normals);
            normalInfo.buffer.position(0);
        }

        mHasNormals = true;
    }

    public void setNormals(FloatBuffer normals) {
        normals.position(0);
        float[] n = new float[normals.capacity()];
        normals.get(n);
        setNormals(n);
    }


    public FloatBuffer getNormals() {
        if (mOriginalGeometry != null) {
            return mOriginalGeometry.getNormals();
        }
        return (FloatBuffer) mBuffers.get(NORMAL_BUFFER_KEY).buffer;
    }

    public boolean hasNormals() {
        return mHasNormals;
    }

    public void setIndices(int[] indices) {
        setIndices(indices, false);
    }

    public void setIndices(int[] indices, boolean override) {
        final BufferInfo indexInfo = mBuffers.get(INDEX_BUFFER_KEY);
        if (indexInfo.buffer == null || override == true) {
            indexInfo.buffer = ByteBuffer.allocateDirect(indices.length * INT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asIntBuffer();
            ((IntBuffer) indexInfo.buffer).put(indices).position(0);

            mNumIndices = indices.length;
        } else {
            ((IntBuffer) indexInfo.buffer).put(indices);
        }
    }

    public IntBuffer getIndices() {
        if (mBuffers.get(INDEX_BUFFER_KEY).buffer == null && mOriginalGeometry != null) {
            return mOriginalGeometry.getIndices();
        }
        return (IntBuffer) mBuffers.get(INDEX_BUFFER_KEY).buffer;
    }

    public void setTextureCoords(float[] textureCoords) {
        setTextureCoords(textureCoords, false);
    }

    public void setTextureCoords(float[] textureCoords, boolean override) {
        if (textureCoords == null) {
            return;
        }
        final BufferInfo textureInfo = mBuffers.get(TEXTURE_BUFFER_KEY);
        if (textureInfo.buffer == null || override == true) {
            textureInfo.buffer = ByteBuffer
                    .allocateDirect(textureCoords.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) textureInfo.buffer).put(textureCoords);
            textureInfo.buffer.position(0);
        } else {
            ((FloatBuffer) textureInfo.buffer).put(textureCoords);
        }
        mHasTextureCoordinates = true;
    }

    public FloatBuffer getTextureCoords() {
        if (mBuffers.get(TEXTURE_BUFFER_KEY).buffer == null && mOriginalGeometry != null) {
            return mOriginalGeometry.getTextureCoords();
        }
        return (FloatBuffer) mBuffers.get(TEXTURE_BUFFER_KEY).buffer;
    }

    public boolean hasTextureCoordinates() {
        return mHasTextureCoordinates;
    }

    public void setColors(int color) {
        setColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
    }

    public void setColors(float[] colors) {
        setColors(colors, false);
    }

    public void setColors(float[] colors, boolean override) {
        final BufferInfo colorInfo = mBuffers.get(COLOR_BUFFER_KEY);
        if (colorInfo.buffer == null || override == true) {
            colorInfo.buffer = ByteBuffer
                    .allocateDirect(colors.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) colorInfo.buffer).put(colors);
            colorInfo.buffer.position(0);
        } else {
            ((FloatBuffer) colorInfo.buffer).put(colors);
            colorInfo.buffer.position(0);
        }
    }

    public FloatBuffer getColors() {
        if (mBuffers.get(COLOR_BUFFER_KEY).buffer == null && mOriginalGeometry != null) {
            return mOriginalGeometry.getColors();
        }
        return (FloatBuffer) mBuffers.get(COLOR_BUFFER_KEY).buffer;
    }

    public int getNumIndices() {
        return mNumIndices;
    }

    public int getNumVertices() {
        return mNumVertices;
    }

    public void setNumVertices(int numVertices) {
        mNumVertices = numVertices;
    }

    public void setColor(float r, float g, float b, float a) {
        setColor(r, g, b, a, false);
    }

    public void setColor(float r, float g, float b, float a, boolean createNewBuffer) {
        BufferInfo colorInfo = mBuffers.get(COLOR_BUFFER_KEY);
        if (colorInfo.buffer == null || colorInfo.buffer.limit() == 0) {
            colorInfo = new BufferInfo();
            colorInfo.buffer = ByteBuffer.allocateDirect(mNumVertices * 4 * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            createNewBuffer = true;
            mBuffers.add(COLOR_BUFFER_KEY, colorInfo);
        }

        colorInfo.buffer.position(0);

        while (colorInfo.buffer.remaining() > 3) {
            ((FloatBuffer) colorInfo.buffer).put(r);
            ((FloatBuffer) colorInfo.buffer).put(g);
            ((FloatBuffer) colorInfo.buffer).put(b);
            ((FloatBuffer) colorInfo.buffer).put(a);
        }
        colorInfo.buffer.position(0);

        if (createNewBuffer) {
            createBuffer(colorInfo, BufferType.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);
        } else {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorInfo.bufferHandle);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorInfo.buffer.limit() * FLOAT_SIZE_BYTES, colorInfo.buffer,
                                GLES20.GL_STATIC_DRAW);
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public String toString() {
        final StringBuilder buff = new StringBuilder();
        if (mBuffers.get(INDEX_BUFFER_KEY).buffer != null) {
            buff.append("Geometry3D indices: ").append(mBuffers.get(INDEX_BUFFER_KEY).buffer.capacity());
        }
        if (mBuffers.get(VERTEX_BUFFER_KEY).buffer != null) {
            buff.append(", vertices: ").append(mBuffers.get(VERTEX_BUFFER_KEY).buffer.capacity());
        }
        if (mBuffers.get(NORMAL_BUFFER_KEY).buffer != null) {
            buff.append(", normals: ").append(mBuffers.get(NORMAL_BUFFER_KEY).buffer.capacity());
        }
        if (mBuffers.get(TEXTURE_BUFFER_KEY).buffer != null) {
            buff.append(", uvs: ").append(mBuffers.get(TEXTURE_BUFFER_KEY).buffer.capacity()).append("\n");
        }
        if (mBuffers.get(COLOR_BUFFER_KEY).buffer != null) {
            buff.append(", colors: ").append(mBuffers.get(COLOR_BUFFER_KEY).buffer.capacity()).append("\n");
        }

        if (mBuffers.get(VERTEX_BUFFER_KEY) != null) {
            buff.append("vertex buffer handle: ").append(mBuffers.get(VERTEX_BUFFER_KEY).bufferHandle).append("\n");
        }
        if (mBuffers.get(INDEX_BUFFER_KEY) != null) {
            buff.append("index buffer handle: ").append(mBuffers.get(INDEX_BUFFER_KEY).bufferHandle).append("\n");
        }
        if (mBuffers.get(NORMAL_BUFFER_KEY) != null) {
            buff.append("normal buffer handle: ").append(mBuffers.get(NORMAL_BUFFER_KEY).bufferHandle).append("\n");
        }
        if (mBuffers.get(TEXTURE_BUFFER_KEY) != null) {
            buff.append("texcoord buffer handle: ").append(mBuffers.get(TEXTURE_BUFFER_KEY).bufferHandle).append("\n");
        }
        if (mBuffers.get(COLOR_BUFFER_KEY) != null) {
            buff.append("color buffer handle: ").append(mBuffers.get(COLOR_BUFFER_KEY).bufferHandle).append("\n");
        }

        return buff.toString();
    }

    public void destroy() {
        int[] buffers = new int[mBuffers.size()];
        int index = 0;
        for (BufferInfo info : mBuffers) {
            buffers[index++] = info.bufferHandle;
            if (info.buffer != null) {
                info.buffer.clear();
                info.buffer = null;
            }
        }
        GLES20.glDeleteBuffers(buffers.length, buffers, 0);

        mOriginalGeometry = null;

        mBuffers.clear();
    }

    public boolean hasBoundingBox() {
        return mBoundingBox != null;
    }

    /**
     * Gets the bounding box for this geometry. If there is no current bounding
     * box it will be calculated.
     *
     * @return
     */
    public BoundingBox getBoundingBox() {
        if (mBoundingBox == null) {
            mBoundingBox = new BoundingBox(this);
        }
        return mBoundingBox;
    }

    public boolean hasBoundingSphere() {
        return mBoundingSphere != null;
    }

    /**
     * Gets the bounding sphere for this geometry. If there is not current bounding
     * sphere it will be calculated.
     *
     * @return
     */
    public BoundingSphere getBoundingSphere() {
        if (mBoundingSphere == null) {
            mBoundingSphere = new BoundingSphere(this);
        }
        return mBoundingSphere;
    }

    public BufferInfo getVertexBufferInfo() {
        return mBuffers.get(VERTEX_BUFFER_KEY);
    }

    public void setVertexBufferInfo(BufferInfo vertexBufferInfo) {
        mBuffers.add(VERTEX_BUFFER_KEY, vertexBufferInfo);
    }

    public BufferInfo getIndexBufferInfo() {
        return mBuffers.get(INDEX_BUFFER_KEY);
    }

    public void setIndexBufferInfo(BufferInfo indexBufferInfo) {
        mBuffers.add(INDEX_BUFFER_KEY, indexBufferInfo);
    }

    public BufferInfo getTexCoordBufferInfo() {
        return mBuffers.get(TEXTURE_BUFFER_KEY);
    }

    public void setTexCoordBufferInfo(BufferInfo texCoordBufferInfo) {
        mBuffers.add(TEXTURE_BUFFER_KEY, texCoordBufferInfo);
        this.mHasTextureCoordinates = true;
    }

    public BufferInfo getColorBufferInfo() {
        return mBuffers.get(COLOR_BUFFER_KEY);
    }

    public void setColorBufferInfo(BufferInfo colorBufferInfo) {
        mBuffers.add(COLOR_BUFFER_KEY, colorBufferInfo);
    }

    public BufferInfo getNormalBufferInfo() {
        return mBuffers.get(NORMAL_BUFFER_KEY);
    }

    public void setNormalBufferInfo(BufferInfo normalBufferInfo) {
        mBuffers.add(NORMAL_BUFFER_KEY, normalBufferInfo);
        this.mHasNormals = true;
    }

    public int getNumTriangles() {
        final Buffer vertBuffer = mBuffers.get(VERTEX_BUFFER_KEY).buffer;
        return vertBuffer != null ? vertBuffer.limit() / 9 : 0;
    }

    public void setBuffersCreated(boolean created) {
        mHaveCreatedBuffers = created;
    }

    void setBoundingBox(BoundingBox boundingBox){
        this.mBoundingBox = boundingBox;
    }
}
