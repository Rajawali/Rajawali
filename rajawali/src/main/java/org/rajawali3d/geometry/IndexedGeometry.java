/**
 * Copyright 2013 Dennis Ippel
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.geometry;

import android.graphics.Color;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import android.support.annotation.Nullable;

import net.jcip.annotations.NotThreadSafe;

import org.rajawali3d.animation.mesh.VertexAnimationObject3D;
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

import static org.rajawali3d.util.ArrayUtils.concatAllFloat;
import static org.rajawali3d.util.ArrayUtils.concatAllInt;
import static org.rajawali3d.util.ArrayUtils.getFloatArrayFromBuffer;
import static org.rajawali3d.util.ArrayUtils.getIntArrayFromBuffer;

/**
 * This is where the vertex, normal, texture coordinate, color and index data is stored. The data is stored in
 * FloatBuffers, IntBuffers and ShortBuffers. The data is uploaded to the graphics card using VERTEX Buffer Objects
 * (VBOs). The data in the FloatBuffers is kept in memory in order to restore the VBOs when the OpenGL context needs
 * to be restored (typically when the application regains focus).
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SuppressWarnings("WeakerAccess")
@NotThreadSafe
public class IndexedGeometry implements Geometry {

    public static final int FLOAT_SIZE_BYTES = 4;
    public static final int INT_SIZE_BYTES = 4;
    public static final int SHORT_SIZE_BYTES = 2;
    public static final int BYTE_SIZE_BYTES = 1;

    public static final int VERTEX_BUFFER_KEY = 0;
    public static final int NORMAL_BUFFER_KEY = 1;
    public static final int TEXTURE_BUFFER_KEY = 2;
    public static final int COLOR_BUFFER_KEY = 3;
    public static final int INDEX_BUFFER_KEY = 4;

    // TODO: We probably want this to be a sparse array
    protected final ArrayList<BufferInfo> buffers;

    /**
     * The number of indices currently stored in the index buffer.
     */
    protected int numIndices;
    /**
     * The number of vertices currently stored in the vertex buffer.
     */
    protected int numVertices;
    /**
     * A pointer to the original geometry. This is not null when the object has been cloned.
     * When cloning a BaseObject3D the data isn't copied over, only the handle to the OpenGL
     * buffers are used.
     */
    protected IndexedGeometry sourceGeometry;

    /**
     * Boolean to keep track of if the buffers for this geometry have been through their initial creation.
     */
    protected boolean haveCreatedBuffers;

    /**
     * Indicates whether this geometry contains normals or not.
     */
    protected boolean hasNormals;
    /**
     * Indicates whether this geometry contains texture coordinates or not.
     */
    protected boolean hasTextureCoordinates;

    public IndexedGeometry() {
        haveCreatedBuffers = false;
        buffers = new ArrayList<>(8);
        buffers.add(new BufferInfo());
        buffers.add(new BufferInfo());
        buffers.add(new BufferInfo());
        buffers.add(new BufferInfo());
        buffers.add(new BufferInfo());

        buffers.get(VERTEX_BUFFER_KEY).rajawaliHandle = VERTEX_BUFFER_KEY;
        buffers.get(VERTEX_BUFFER_KEY).bufferType = BufferInfo.FLOAT_BUFFER;
        buffers.get(VERTEX_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        buffers.get(NORMAL_BUFFER_KEY).rajawaliHandle = NORMAL_BUFFER_KEY;
        buffers.get(NORMAL_BUFFER_KEY).bufferType = BufferInfo.FLOAT_BUFFER;
        buffers.get(NORMAL_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        buffers.get(TEXTURE_BUFFER_KEY).rajawaliHandle = TEXTURE_BUFFER_KEY;
        buffers.get(TEXTURE_BUFFER_KEY).bufferType = BufferInfo.FLOAT_BUFFER;
        buffers.get(TEXTURE_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        buffers.get(COLOR_BUFFER_KEY).rajawaliHandle = COLOR_BUFFER_KEY;
        buffers.get(COLOR_BUFFER_KEY).bufferType = BufferInfo.FLOAT_BUFFER;
        buffers.get(COLOR_BUFFER_KEY).target = GLES20.GL_ARRAY_BUFFER;

        buffers.get(INDEX_BUFFER_KEY).rajawaliHandle = INDEX_BUFFER_KEY;
        buffers.get(INDEX_BUFFER_KEY).bufferType = BufferInfo.INT_BUFFER;
        buffers.get(INDEX_BUFFER_KEY).target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
    }

    @RenderThread
    @Override
    public void createBuffers() {
        for (BufferInfo info : buffers) {
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

        haveCreatedBuffers = true;
    }

    @RenderThread
    @Override
    public void validateBuffers() {
        if (!haveCreatedBuffers) {
            createBuffers();
        }
        if (sourceGeometry != null) {
            sourceGeometry.validateBuffers();
            return;
        }

        for (int i = 0, j = buffers.size(); i < j; ++i) {
            final BufferInfo info = buffers.get(i);
            if (info != null && info.glHandle == 0) {
                createBuffer(info);
            }
        }
    }

    @RenderThread
    @Override
    public boolean isValid() {
        return GLES20.glIsBuffer(buffers.get(VERTEX_BUFFER_KEY).glHandle);
    }

    @RenderThread
    @Override
    public void reload() {
        if (sourceGeometry != null) {
            if (!sourceGeometry.isValid()) {
                sourceGeometry.reload();
            }
            copyFromGeometry3D(sourceGeometry);
        }
        createBuffers();
    }

    @RenderThread
    @Override
    public void destroy() {
        int[] glHandles = new int[buffers.size()];
        int index = 0;
        for (BufferInfo info : buffers) {
            glHandles[index++] = info.glHandle;
            if (info.buffer != null) {
                info.buffer.clear();
                info.buffer = null;
            }
        }
        GLES20.glDeleteBuffers(glHandles.length, glHandles, 0);
        sourceGeometry = null;
        buffers.clear();
    }

    @RequiresReadLock
    @Override
    public void calculateAABounds(@NonNull Vector3 min, @NonNull Vector3 max) {
        final FloatBuffer vertices = getVertices();
        vertices.rewind();

        min.setAll(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        max.setAll(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        final Vector3 vertex = new Vector3();

        while (vertices.hasRemaining()) {
            vertex.x = vertices.get();
            vertex.y = vertices.get();
            vertex.z = vertices.get();

            if (vertex.x < min.x) {
                min.x = vertex.x;
            }
            if (vertex.y < min.y) {
                min.y = vertex.y;
            }
            if (vertex.z < min.z) {
                min.z = vertex.z;
            }
            if (vertex.x > max.x) {
                max.x = vertex.x;
            }
            if (vertex.y > max.y) {
                max.y = vertex.y;
            }
            if (vertex.z > max.z) {
                max.z = vertex.z;
            }
        }
    }

    @RequiresReadLock
    @RenderThread
    @Override
    public void issueDrawCalls() {
        // TODO: Issue draw calls
    }

    /**
     * Copies another IndexedGeometry's BufferInfo objects. This means that it doesn't copy or clone the actual data.
     * It will just use the pointers to the other IndexedGeometry's buffers.
     *
     * @param geometry The {@link IndexedGeometry} to copy from.
     *
     * @see BufferInfo
     */
    public void copyFromGeometry3D(@NonNull IndexedGeometry geometry) {
        numIndices = geometry.getNumIndices();
        numVertices = geometry.getNumVertices();

        buffers.add(VERTEX_BUFFER_KEY, geometry.getVertexBufferInfo());
        buffers.add(NORMAL_BUFFER_KEY, geometry.getNormalBufferInfo());
        buffers.add(TEXTURE_BUFFER_KEY, geometry.getTexCoordBufferInfo());
        if (buffers.get(COLOR_BUFFER_KEY).buffer == null) {
            buffers.add(COLOR_BUFFER_KEY, geometry.getColorBufferInfo());
        }
        buffers.add(INDEX_BUFFER_KEY, geometry.getIndexBufferInfo());
        sourceGeometry = geometry;
        hasNormals = geometry.hasNormals();
        hasTextureCoordinates = geometry.hasTextureCoordinates();
    }

    /**
     * Adds the geometry from the incoming geometry with the specified offset. Note that the offset is only applied to
     * the vertex positions. Subsequent changes to the original data being added will not affect the data of this
     * instance.
     *
     * @param offset     {@link Vector3} containing the offset in each direction. Can be null.
     * @param geometry   {@link IndexedGeometry} to be added.
     * @param createVBOs {@code true} if the VBOs should be constructed immediately. This requires calling on the GL
     *                               thread.
     */
    public void addFromOther(@NonNull Vector3 offset, @NonNull IndexedGeometry geometry, boolean createVBOs) {
        float[] newVertices;
        float[] newNormals;
        float[] newColors;
        float[] newTextureCoords;
        int[] newIntIndices;
        float[] verticesArray;
        float[] normalsArray;
        float[] colorsArray;
        float[] textureCoordsArray;
        int[] indicesArray;

        //Get the old data
        verticesArray = getFloatArrayFromBuffer((FloatBuffer) buffers.get(VERTEX_BUFFER_KEY).buffer);
        normalsArray = getFloatArrayFromBuffer((FloatBuffer) buffers.get(NORMAL_BUFFER_KEY).buffer);
        colorsArray = getFloatArrayFromBuffer((FloatBuffer) buffers.get(COLOR_BUFFER_KEY).buffer);
        textureCoordsArray = getFloatArrayFromBuffer((FloatBuffer) buffers.get(TEXTURE_BUFFER_KEY).buffer);
        indicesArray = getIntArrayFromBuffer(buffers.get(INDEX_BUFFER_KEY).buffer);

        // Get the new data, offset the vertices
        final float[] addVertices = getFloatArrayFromBuffer(geometry.getVertices());
        for (int i = 0, j = addVertices.length; i < j; i += 3) {
            addVertices[i] += offset.x;
            addVertices[i + 1] += offset.y;
            addVertices[i + 2] += offset.z;
        }
        final float[] addNormals = getFloatArrayFromBuffer(geometry.getNormals());
        final float[] addColors = getFloatArrayFromBuffer(geometry.getColors());
        final float[] addTextureCoords = getFloatArrayFromBuffer(geometry.getTextureCoords());
        final int[] addIndices = getIntArrayFromBuffer(geometry.getIndices());
        int index_offset = verticesArray.length / 3;
        for (int i = 0, j = addIndices.length; i < j; ++i) {
            addIndices[i] += index_offset;
        }

        // Concatenate the old and new data
        newVertices = concatAllFloat(verticesArray, addVertices);
        newNormals = concatAllFloat(normalsArray, addNormals);
        newColors = concatAllFloat(colorsArray, addColors);
        newTextureCoords = concatAllFloat(textureCoordsArray, addTextureCoords);
        newIntIndices = concatAllInt(indicesArray, addIndices);

        // Set the new data
        setVertices(newVertices, true);
        setNormals(newNormals, true);
        setTextureCoords(newTextureCoords, true);
        setColors(newColors, true);
        setIndices(newIntIndices, true);

        if (createVBOs) {
            // Create the new buffers
            createBuffers();
        }
    }

    /**
     * Sets the geometry data. This methods takes two {@link BufferInfo} objects which means it will use another
     * IndexedGeometry instance's data (vertices and normals). The remaining parameters are arrays which will be used
     * to create buffers that are unique to this instance.
     * <p>
     * This is typically used with {@link VertexAnimationObject3D} instances.
     *
     * @param vertexBufferInfo {@link BufferInfo} providing the vertex data.
     * @param normalBufferInfo {@link BufferInfo} providing the normal data.
     * @param textureCoords {@code float} array containing the texture coordinate data.
     * @param colors {@code float} array containing the vertex color data.
     * @param indices {@code int} array containing the vertex index data.
     * @param createVBOs {@code true} if the VBOs should be constructed immediately. This requires calling on the GL
     *                               thread.
     *
     * @see VertexAnimationObject3D
     */
    public void setData(@NonNull BufferInfo vertexBufferInfo, @Nullable BufferInfo normalBufferInfo,
                        @Nullable float[] textureCoords, @Nullable float[] colors, @NonNull int[] indices,
                        boolean createVBOs) {
        // TODO: Why are we synthesizing texture and color data?
        if (textureCoords == null || textureCoords.length == 0) {
            textureCoords = new float[(numVertices / 3) * 2];
        }
        setTextureCoords(textureCoords);
        if (colors == null || colors.length == 0) {
            setColors(0xff000000 + (int) (Math.random() * 0xffffff));
        } else {
            setColors(colors);
        }
        setIndices(indices);

        buffers.add(VERTEX_BUFFER_KEY, vertexBufferInfo);
        buffers.add(NORMAL_BUFFER_KEY, normalBufferInfo);

        sourceGeometry = null;

        if (createVBOs) {
            createBuffers();
        }
    }

    /**
     * Sets the geometry data. Assumes that the data will never be changed and passes @link GLES20.GL_STATIC_DRAW} to
     * the OpenGL context when the buffers are created.
     *
     * @param vertices {@code float} array containing the vertex position data.
     * @param normals {@code float} array containing the vertex normal data.
     * @param textureCoords {@code float} array containing the the vertex texture coordinate data.
     * @param colors {@code float} array containing the vertex color data.
     * @param indices {@code int} array containing the geometry index data.
     * @param createVBOs {@code true} if the VBOs should be constructed immediately. This requires calling on the GL
     *                               thread.
     *
     * @see {@link GLES20#GL_STATIC_DRAW}.
     */
    public void setData(@NonNull float[] vertices, @Nullable float[] normals, @Nullable float[] textureCoords,
                        @Nullable float[] colors, @NonNull int[] indices, boolean createVBOs) {
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
     * @param vertices {@code float} array containing the vertex position data.
     * @param verticesUsage {@code int} VERTEX buffer usage hint.
     * @param normals {@code float} array containing the vertex normal data.
     * @param normalsUsage {@code int} Normal buffer usage hint.
     * @param textureCoords {@code float} array containing the the vertex texture coordinate data.
     * @param textureCoordsUsage {@code int} Texture coordinate buffer usage hint.
     * @param colors {@code float} array containing the vertex color data.
     * @param colorsUsage {@code int} Color buffer usage hint.
     * @param indices {@code int} array containing the geometry index data.
     * @param indicesUsage {@code int} Index buffer usage hint.
     * @param createVBOs {@code true} if the VBOs should be constructed immediately. This requires calling on the GL
     *                               thread.
     */
    public void setData(@NonNull float[] vertices, @VBOUsage int verticesUsage, @Nullable float[] normals,
                        @VBOUsage int normalsUsage, @Nullable float[] textureCoords, @VBOUsage int textureCoordsUsage,
                        @Nullable float[] colors, @VBOUsage int colorsUsage, @NonNull int[] indices,
                        @VBOUsage int indicesUsage, boolean createVBOs) {
        buffers.get(VERTEX_BUFFER_KEY).usage = verticesUsage;
        buffers.get(NORMAL_BUFFER_KEY).usage = normalsUsage;
        buffers.get(TEXTURE_BUFFER_KEY).usage = textureCoordsUsage;
        buffers.get(COLOR_BUFFER_KEY).usage = colorsUsage;
        buffers.get(INDEX_BUFFER_KEY).usage = indicesUsage;
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
     * Creates the vertex and normal buffers only. This is typically used for a
     * VertexAnimationObject3D's frames.
     *
     * @see VertexAnimationObject3D
     */
    public void createVertexAndNormalBuffersOnly() {
        ((FloatBuffer) buffers.get(VERTEX_BUFFER_KEY).buffer).compact().position(0);
        ((FloatBuffer) buffers.get(NORMAL_BUFFER_KEY).buffer).compact().position(0);

        createBuffer(buffers.get(VERTEX_BUFFER_KEY), BufferInfo.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);
        createBuffer(buffers.get(NORMAL_BUFFER_KEY), BufferInfo.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);

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
    public void createBuffer(BufferInfo bufferInfo, @BufferInfo.BufferType int type, int target) {
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
    public void createBuffer(BufferInfo bufferInfo, @BufferInfo.BufferType int type, int target, int usage) {
        int byteSize = FLOAT_SIZE_BYTES;
        if (type == BufferInfo.SHORT_BUFFER) {
            byteSize = SHORT_SIZE_BYTES;
        } else if (type == BufferInfo.BYTE_BUFFER) {
            byteSize = BYTE_SIZE_BYTES;
        } else if (type == BufferInfo.INT_BUFFER) {
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

        bufferInfo.glHandle = handle;
        bufferInfo.bufferType = type;
        bufferInfo.target = target;
        bufferInfo.usage = usage;
    }

    public void createBuffer(BufferInfo bufferInfo) {
        createBuffer(bufferInfo, bufferInfo.bufferType, bufferInfo.target, bufferInfo.usage);
    }

    public int addBuffer(BufferInfo bufferInfo, @BufferInfo.BufferType int type, int target, int usage) {
        createBuffer(bufferInfo, type, target, usage);
        final int key = buffers.size();
        bufferInfo.rajawaliHandle = key;
        buffers.add(bufferInfo);
        return key;
    }

    public int addBuffer(BufferInfo bufferInfo, @BufferInfo.BufferType int type, int target) {
        return addBuffer(bufferInfo, type, target, bufferInfo.usage);
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
     * @param bufferInfo
     * @param usage
     */
    @RenderThread
    public void changeBufferUsage(BufferInfo bufferInfo, final int usage) {
        GLES20.glDeleteBuffers(1, new int[]{bufferInfo.glHandle}, 0);
        createBuffer(bufferInfo, bufferInfo.bufferType, bufferInfo.target, usage);
    }

    /**
     * Change a specific subset of the buffer's data at the given offset to the given length.
     *
     * @param bufferInfo
     * @param newData
     * @param index
     */
    @RenderThread
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
    @RenderThread
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
    @RenderThread
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
    @RenderThread
    public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index, int size, boolean resizeBuffer) {
        newData.rewind();

        GLES20.glBindBuffer(bufferInfo.target, bufferInfo.glHandle);
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
        final BufferInfo vertexInfo = buffers.get(VERTEX_BUFFER_KEY);
        if (vertexInfo.buffer == null || override) {
            if (vertexInfo.buffer != null) {
                vertexInfo.buffer.clear();
            }
            vertexInfo.buffer = ByteBuffer
                .allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

            ((FloatBuffer) vertexInfo.buffer).put(vertices);
            vertexInfo.buffer.position(0);
            numVertices = vertices.length / 3;
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
        if (sourceGeometry != null) {
            return sourceGeometry.getVertices();
        }
        return (FloatBuffer) buffers.get(VERTEX_BUFFER_KEY).buffer;
    }

    public void setNormals(float[] normals) {
        setNormals(normals, false);
    }

    public void setNormals(float[] normals, boolean override) {
        if (normals == null) {
            return;
        }
        final BufferInfo normalInfo = buffers.get(NORMAL_BUFFER_KEY);
        if (normalInfo.buffer == null || override) {
            normalInfo.buffer = ByteBuffer.allocateDirect(normals.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) normalInfo.buffer).put(normals);
            normalInfo.buffer.position(0);
        } else {
            normalInfo.buffer.position(0);
            ((FloatBuffer) normalInfo.buffer).put(normals);
            normalInfo.buffer.position(0);
        }

        hasNormals = true;
    }

    public void setNormals(FloatBuffer normals) {
        normals.position(0);
        float[] n = new float[normals.capacity()];
        normals.get(n);
        setNormals(n);
    }


    public FloatBuffer getNormals() {
        if (sourceGeometry != null) {
            return sourceGeometry.getNormals();
        }
        return (FloatBuffer) buffers.get(NORMAL_BUFFER_KEY).buffer;
    }

    public boolean hasNormals() {
        return hasNormals;
    }

    public void setIndices(int[] indices) {
        setIndices(indices, false);
    }

    public void setIndices(int[] indices, boolean override) {
        final BufferInfo indexInfo = buffers.get(INDEX_BUFFER_KEY);
        if (indexInfo.buffer == null || override) {
            indexInfo.buffer = ByteBuffer.allocateDirect(indices.length * INT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
            ((IntBuffer) indexInfo.buffer).put(indices).position(0);

            numIndices = indices.length;
        } else {
            ((IntBuffer) indexInfo.buffer).put(indices);
        }
    }

    public IntBuffer getIndices() {
        if (buffers.get(INDEX_BUFFER_KEY).buffer == null && sourceGeometry != null) {
            return sourceGeometry.getIndices();
        }
        return (IntBuffer) buffers.get(INDEX_BUFFER_KEY).buffer;
    }

    public void setTextureCoords(float[] textureCoords) {
        setTextureCoords(textureCoords, false);
    }

    public void setTextureCoords(float[] textureCoords, boolean override) {
        if (textureCoords == null) {
            return;
        }
        final BufferInfo textureInfo = buffers.get(TEXTURE_BUFFER_KEY);
        if (textureInfo.buffer == null || override) {
            textureInfo.buffer = ByteBuffer
                .allocateDirect(textureCoords.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) textureInfo.buffer).put(textureCoords);
            textureInfo.buffer.position(0);
        } else {
            ((FloatBuffer) textureInfo.buffer).put(textureCoords);
        }
        hasTextureCoordinates = true;
    }

    public FloatBuffer getTextureCoords() {
        if (buffers.get(TEXTURE_BUFFER_KEY).buffer == null && sourceGeometry != null) {
            return sourceGeometry.getTextureCoords();
        }
        return (FloatBuffer) buffers.get(TEXTURE_BUFFER_KEY).buffer;
    }

    public boolean hasTextureCoordinates() {
        return hasTextureCoordinates;
    }

    public void setColors(int color) {
        setColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
    }

    public void setColors(float[] colors) {
        setColors(colors, false);
    }

    public void setColors(float[] colors, boolean override) {
        final BufferInfo colorInfo = buffers.get(COLOR_BUFFER_KEY);
        if (colorInfo.buffer == null || override) {
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
        if (buffers.get(COLOR_BUFFER_KEY).buffer == null && sourceGeometry != null) {
            return sourceGeometry.getColors();
        }
        return (FloatBuffer) buffers.get(COLOR_BUFFER_KEY).buffer;
    }

    public int getNumIndices() {
        return numIndices;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public void setNumVertices(int numVertices) {
        this.numVertices = numVertices;
    }

    public void setColor(float r, float g, float b, float a) {
        setColor(r, g, b, a, false);
    }

    public void setColor(float r, float g, float b, float a, boolean createNewBuffer) {
        BufferInfo colorInfo = buffers.get(COLOR_BUFFER_KEY);
        if (colorInfo.buffer == null || colorInfo.buffer.limit() == 0) {
            colorInfo = new BufferInfo();
            colorInfo.buffer = ByteBuffer.allocateDirect(numVertices * 4 * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
            createNewBuffer = true;
            buffers.add(COLOR_BUFFER_KEY, colorInfo);
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
            createBuffer(colorInfo, BufferInfo.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);
        } else {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorInfo.glHandle);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorInfo.buffer.limit() * FLOAT_SIZE_BYTES, colorInfo.buffer,
                GLES20.GL_STATIC_DRAW);
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public String toString() {
        final StringBuilder buff = new StringBuilder();
        if (buffers.get(INDEX_BUFFER_KEY).buffer != null) {
            buff.append("IndexedGeometry indices: ").append(buffers.get(INDEX_BUFFER_KEY).buffer.capacity());
        }
        if (buffers.get(VERTEX_BUFFER_KEY).buffer != null) {
            buff.append(", vertices: ").append(buffers.get(VERTEX_BUFFER_KEY).buffer.capacity());
        }
        if (buffers.get(NORMAL_BUFFER_KEY).buffer != null) {
            buff.append(", normals: ").append(buffers.get(NORMAL_BUFFER_KEY).buffer.capacity());
        }
        if (buffers.get(TEXTURE_BUFFER_KEY).buffer != null) {
            buff.append(", uvs: ").append(buffers.get(TEXTURE_BUFFER_KEY).buffer.capacity()).append("\n");
        }
        if (buffers.get(COLOR_BUFFER_KEY).buffer != null) {
            buff.append(", colors: ").append(buffers.get(COLOR_BUFFER_KEY).buffer.capacity()).append("\n");
        }

        if (buffers.get(VERTEX_BUFFER_KEY) != null) {
            buff.append("vertex buffer handle: ").append(buffers.get(VERTEX_BUFFER_KEY).glHandle).append("\n");
        }
        if (buffers.get(INDEX_BUFFER_KEY) != null) {
            buff.append("index buffer handle: ").append(buffers.get(INDEX_BUFFER_KEY).glHandle).append("\n");
        }
        if (buffers.get(NORMAL_BUFFER_KEY) != null) {
            buff.append("normal buffer handle: ").append(buffers.get(NORMAL_BUFFER_KEY).glHandle).append("\n");
        }
        if (buffers.get(TEXTURE_BUFFER_KEY) != null) {
            buff.append("texcoord buffer handle: ").append(buffers.get(TEXTURE_BUFFER_KEY).glHandle).append("\n");
        }
        if (buffers.get(COLOR_BUFFER_KEY) != null) {
            buff.append("color buffer handle: ").append(buffers.get(COLOR_BUFFER_KEY).glHandle).append("\n");
        }

        return buff.toString();
    }

    public BufferInfo getVertexBufferInfo() {
        return buffers.get(VERTEX_BUFFER_KEY);
    }

    public void setVertexBufferInfo(BufferInfo vertexBufferInfo) {
        buffers.add(VERTEX_BUFFER_KEY, vertexBufferInfo);
    }

    public BufferInfo getIndexBufferInfo() {
        return buffers.get(INDEX_BUFFER_KEY);
    }

    public void setIndexBufferInfo(BufferInfo indexBufferInfo) {
        buffers.add(INDEX_BUFFER_KEY, indexBufferInfo);
    }

    public BufferInfo getTexCoordBufferInfo() {
        return buffers.get(TEXTURE_BUFFER_KEY);
    }

    public void setTexCoordBufferInfo(BufferInfo texCoordBufferInfo) {
        buffers.add(TEXTURE_BUFFER_KEY, texCoordBufferInfo);
        this.hasTextureCoordinates = true;
    }

    public BufferInfo getColorBufferInfo() {
        return buffers.get(COLOR_BUFFER_KEY);
    }

    public void setColorBufferInfo(BufferInfo colorBufferInfo) {
        buffers.add(COLOR_BUFFER_KEY, colorBufferInfo);
    }

    public BufferInfo getNormalBufferInfo() {
        return buffers.get(NORMAL_BUFFER_KEY);
    }

    public void setNormalBufferInfo(BufferInfo normalBufferInfo) {
        buffers.add(NORMAL_BUFFER_KEY, normalBufferInfo);
        this.hasNormals = true;
    }

    public int getNumTriangles() {
        final Buffer vertBuffer = buffers.get(VERTEX_BUFFER_KEY).buffer;
        return vertBuffer != null ? vertBuffer.limit() / 9 : 0;
    }

    public void setBuffersCreated(boolean created) {
        haveCreatedBuffers = created;
    }
}
