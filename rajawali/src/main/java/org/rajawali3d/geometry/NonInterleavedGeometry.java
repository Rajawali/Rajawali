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
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import c.org.rajawali3d.gl.buffers.BufferUsage;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.animation.mesh.VertexAnimationObject3D;
import org.rajawali3d.math.vector.Vector3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
public class NonInterleavedGeometry extends IndexedGeometry {

    public static final int VERTEX_BUFFER_KEY = 0;
    public static final int NORMAL_BUFFER_KEY = 1;
    public static final int TEXTURE_BUFFER_KEY = 2;
    public static final int COLOR_BUFFER_KEY = 3;

    /**
     * The number of vertices currently stored in the vertex buffer.
     */
    protected int                    numVertices;
    /**
     * A pointer to the original geometry. This is not null when the object has been cloned.
     * When cloning a BaseObject3D the data isn't copied over, only the handle to the OpenGL
     * buffers are used.
     */
    protected NonInterleavedGeometry sourceGeometry;

    /**
     * Indicates whether this geometry contains normals or not.
     */
    protected boolean hasNormals;
    /**
     * Indicates whether this geometry contains texture coordinates or not.
     */
    protected boolean hasTextureCoordinates;

    public NonInterleavedGeometry() {
    }

    @RenderThread
    @Override
    public void validateBuffers() {
        if (sourceGeometry != null) {
            sourceGeometry.validateBuffers();
        } else {
            super.validateBuffers();
        }
    }

    @RenderThread
    @Override
    public void reload() {
        if (sourceGeometry != null) {
            if (!sourceGeometry.isValid()) {
                sourceGeometry.reload();
            }
            copyFrom(sourceGeometry);
        } else {
            super.reload();
        }
    }

    @RenderThread
    @Override
    public void destroy() {
        if (sourceGeometry != null) {
            sourceGeometry.destroy();
            sourceGeometry = null;
        } else {
            // TODO: Reference counting for source geometries
            super.destroy();
        }
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
     * Copies another {@link NonInterleavedGeometry}'s BufferInfo objects. This means that it doesn't copy or clone the
     * actual data, but rather will just use the pointers to the other {@link NonInterleavedGeometry}'s buffers.
     *
     * @param geometry The {@link NonInterleavedGeometry} to copy from.
     *
     * @see BufferInfo
     */
    public void copyFrom(@NonNull NonInterleavedGeometry geometry) {
        numVertices = geometry.getNumVertices();

        BufferInfo info = geometry.getVertexBufferInfo();
        if (info != null) {
            setBufferInfo(VERTEX_BUFFER_KEY, info);
        }
        info = geometry.getNormalBufferInfo();
        if (info != null) {
            setBufferInfo(NORMAL_BUFFER_KEY, info);
        }
        info = geometry.getTexCoordBufferInfo();
        if (info != null) {
            setBufferInfo(TEXTURE_BUFFER_KEY, info);
        }
        info = geometry.getColorBufferInfo();
        if (info != null) {
            setBufferInfo(COLOR_BUFFER_KEY, info);
        }
        super.copyFrom(geometry);
        sourceGeometry = geometry;
        hasNormals = geometry.hasNormals();
        hasTextureCoordinates = geometry.hasTextureCoordinates();
    }

    /**
     * Sets the geometry data. This methods takes two {@link BufferInfo} objects which means it will use another
     * NonInterleavedGeometry instance's data (vertices and normals). The remaining parameters are arrays which will
     * be used
     * to create buffers that are unique to this instance.
     * <p>
     * This is typically used with {@link VertexAnimationObject3D} instances.
     *
     * @param vertexBufferInfo {@link BufferInfo} providing the vertex data.
     * @param normalBufferInfo {@link BufferInfo} providing the normal data.
     * @param textureCoords    {@code float} array containing the texture coordinate data.
     * @param colors           {@code float} array containing the vertex color data.
     * @param indices          {@code int} array containing the vertex index data.
     * @param createVBOs       {@code true} if the VBOs should be constructed immediately. This requires calling on
     *                                     the GL
     *                         thread.
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

        setBufferInfo(VERTEX_BUFFER_KEY, vertexBufferInfo);
        setBufferInfo(NORMAL_BUFFER_KEY, normalBufferInfo);

        sourceGeometry = null;

        if (createVBOs) {
            createBuffers();
        }
    }

    /**
     * Sets the geometry data. Assumes that the data will never be changed and passes @link GLES20.GL_STATIC_DRAW} to
     * the OpenGL context when the buffers are created.
     *
     * @param vertices      {@code float} array containing the vertex position data.
     * @param normals       {@code float} array containing the vertex normal data.
     * @param textureCoords {@code float} array containing the the vertex texture coordinate data.
     * @param colors        {@code float} array containing the vertex color data.
     * @param indices       {@code int} array containing the geometry index data.
     * @param createVBOs    {@code true} if the VBOs should be constructed immediately. This requires calling on the GL
     *                      thread.
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
     * @param vertices           {@code float} array containing the vertex position data.
     * @param verticesUsage      {@code int} VERTEX buffer usage hint.
     * @param normals            {@code float} array containing the vertex normal data.
     * @param normalsUsage       {@code int} Normal buffer usage hint.
     * @param textureCoords      {@code float} array containing the the vertex texture coordinate data.
     * @param textureCoordsUsage {@code int} Texture coordinate buffer usage hint.
     * @param colors             {@code float} array containing the vertex color data.
     * @param colorsUsage        {@code int} Color buffer usage hint.
     * @param indices            {@code int} array containing the geometry index data.
     * @param indicesUsage       {@code int} Index buffer usage hint.
     * @param createVBOs         {@code true} if the VBOs should be constructed immediately. This requires calling on
     *                                       the GL
     *                           thread.
     */
    @SuppressWarnings("ConstantConditions")
    public void setData(@NonNull float[] vertices, @BufferUsage int verticesUsage, @Nullable float[] normals,
                        @BufferUsage int normalsUsage, @Nullable float[] textureCoords,
                        @BufferUsage int textureCoordsUsage, @Nullable float[] colors, @BufferUsage int colorsUsage,
                        @NonNull int[] indices, @BufferUsage int indicesUsage, boolean createVBOs) {
        // Set the vertices
        setVertices(vertices, true);

        // Set the normals if provided
        if (normals != null && normals.length > 0) {
            setNormals(normals, true);
        }

        // Set the texture coordinates, if provided
        if (textureCoords != null && textureCoords.length > 0) {
            setTextureCoords(textureCoords, true);
        }

        // Set the vertex colors if provided
        if (colors != null && colors.length > 0) {
            setColors(colors, true);
        }

        // Set the indices
        setIndices(indices, true);

        // We can skip null safety check for buffers because hasBuffer enforces it
        if (hasBuffer(VERTEX_BUFFER_KEY)) {
            getBufferInfo(VERTEX_BUFFER_KEY).usage = verticesUsage;
        }
        if (hasBuffer(NORMAL_BUFFER_KEY)) {
            getBufferInfo(NORMAL_BUFFER_KEY).usage = normalsUsage;
        }
        if (hasBuffer(TEXTURE_BUFFER_KEY)) {
            getBufferInfo(TEXTURE_BUFFER_KEY).usage = textureCoordsUsage;
        }
        if (hasBuffer(COLOR_BUFFER_KEY)) {
            getBufferInfo(COLOR_BUFFER_KEY).usage = colorsUsage;
        }
        final BufferInfo indexInfo = getIndexBufferInfo();
        if (indexInfo != null) {
            indexInfo.usage = indicesUsage;
        }

        if (createVBOs) {
            createBuffers();
        }
    }

    /**
     * Creates the vertex and normal buffers only. This is typically used for a
     * VertexAnimationObject3D's frames.
     *
     * @throws IllegalStateException if no data has been set for the vertices or normals.
     * @see VertexAnimationObject3D
     */
    public void createVertexAndNormalBuffersOnly() throws IllegalStateException {
        if (!hasBuffer(VERTEX_BUFFER_KEY) || !hasBuffer(NORMAL_BUFFER_KEY)) {
            throw new IllegalStateException("Cannot create vertex and normal buffers when no data has been provided.");
        }
        final BufferInfo vertex = getBufferInfo(VERTEX_BUFFER_KEY);
        final BufferInfo normal = getBufferInfo(NORMAL_BUFFER_KEY);
        ((FloatBuffer) vertex.buffer).compact().position(0);
        ((FloatBuffer) normal.buffer).compact().position(0);

        createBufferObject(vertex, BufferInfo.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);
        createBufferObject(normal, BufferInfo.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);

        // Ensure we clear the current buffer binding
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void setVertices(@NonNull float[] vertices) {
        setVertices(vertices, false);
    }

    public void setVertices(@NonNull float[] vertices, boolean override) {
        BufferInfo vertexInfo = getBufferInfo(VERTEX_BUFFER_KEY);
        if (vertexInfo == null) {
            vertexInfo = new BufferInfo();
            vertexInfo.rajawaliHandle = VERTEX_BUFFER_KEY;
            vertexInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            vertexInfo.target = GLES20.GL_ARRAY_BUFFER;
            final BufferInfo old = setBufferInfo(VERTEX_BUFFER_KEY, vertexInfo);
            //TODO: Cleanup old buffer info
        }
        if (override || vertexInfo.buffer == null) {
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

    public void setVertices(@NonNull FloatBuffer vertices) {
        vertices.position(0);
        float[] v = new float[vertices.capacity()];
        vertices.get(v);
        setVertices(v);
    }

    @Nullable
    public FloatBuffer getVertices() {
        final BufferInfo info = getBufferInfo(VERTEX_BUFFER_KEY);
        if (info == null) {
            return null;
        } else {
            return (FloatBuffer) info.buffer;
        }
    }

    public void setNormals(@NonNull float[] normals) {
        setNormals(normals, false);
    }

    public void setNormals(@NonNull float[] normals, boolean override) {
        BufferInfo normalInfo = getBufferInfo(NORMAL_BUFFER_KEY);
        if (normalInfo == null) {
            normalInfo = new BufferInfo();
            normalInfo.rajawaliHandle = NORMAL_BUFFER_KEY;
            normalInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            normalInfo.target = GLES20.GL_ARRAY_BUFFER;
            final BufferInfo old = setBufferInfo(NORMAL_BUFFER_KEY, normalInfo);
            //TODO: Cleanup old buffer info
        }
        if (override || normalInfo.buffer == null) {
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

    public void setNormals(@NonNull FloatBuffer normals) {
        normals.position(0);
        float[] n = new float[normals.capacity()];
        normals.get(n);
        setNormals(n);
    }


    @Nullable
    public FloatBuffer getNormals() {
        final BufferInfo info = getBufferInfo(NORMAL_BUFFER_KEY);
        if (info == null) {
            return null;
        } else {
            return (FloatBuffer) info.buffer;
        }
    }

    public boolean hasNormals() {
        return hasNormals;
    }

    public void setTextureCoords(@NonNull float[] textureCoords) {
        setTextureCoords(textureCoords, false);
    }

    public void setTextureCoords(@NonNull float[] textureCoords, boolean override) {
        BufferInfo textureInfo = getBufferInfo(TEXTURE_BUFFER_KEY);
        if (textureInfo == null) {
            textureInfo = new BufferInfo();
            textureInfo.rajawaliHandle = TEXTURE_BUFFER_KEY;
            textureInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            textureInfo.target = GLES20.GL_ARRAY_BUFFER;
            final BufferInfo old = setBufferInfo(TEXTURE_BUFFER_KEY, textureInfo);
            //TODO: Cleanup old buffer info
        }
        if (override || textureInfo.buffer == null) {
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

    public void setTextureCoords(@NonNull FloatBuffer colors) {
        colors.position(0);
        float[] n = new float[colors.capacity()];
        colors.get(n);
        setColors(n);
    }

    @Nullable
    public FloatBuffer getTextureCoords() {
        final BufferInfo info = getBufferInfo(TEXTURE_BUFFER_KEY);
        if (info == null) {
            return null;
        } else {
            return (FloatBuffer) info.buffer;
        }
    }

    public boolean hasTextureCoordinates() {
        return hasTextureCoordinates;
    }

    public void setColor(float r, float g, float b, float a) {
        setColor(r, g, b, a, false);
    }

    public void setColor(float r, float g, float b, float a, boolean override) {
        BufferInfo colorInfo = getBufferInfo(COLOR_BUFFER_KEY);
        if (colorInfo == null || colorInfo.buffer == null || colorInfo.buffer.limit() == 0) {
            colorInfo = new BufferInfo();
            colorInfo.rajawaliHandle = COLOR_BUFFER_KEY;
            colorInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            colorInfo.target = GLES20.GL_ARRAY_BUFFER;
            colorInfo.buffer = ByteBuffer.allocateDirect(numVertices * 4 * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            final BufferInfo old = setBufferInfo(COLOR_BUFFER_KEY, colorInfo);
            //TODO: Cleanup old buffer info
        }
        if (override || colorInfo.buffer == null) {
            colorInfo.buffer = ByteBuffer
                    .allocateDirect(numVertices * 4 * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        colorInfo.buffer.position(0);

        while (colorInfo.buffer.remaining() > 3) {
            ((FloatBuffer) colorInfo.buffer).put(r);
            ((FloatBuffer) colorInfo.buffer).put(g);
            ((FloatBuffer) colorInfo.buffer).put(b);
            ((FloatBuffer) colorInfo.buffer).put(a);
        }
        colorInfo.buffer.position(0);
    }

    public void setColors(int color) {
        setColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
    }

    public void setColors(@NonNull float[] colors) {
        setColors(colors, false);
    }

    public void setColors(@NonNull float[] colors, boolean override) {
        BufferInfo colorInfo = getBufferInfo(COLOR_BUFFER_KEY);
        if (colorInfo == null) {
            colorInfo = new BufferInfo();
            colorInfo.rajawaliHandle = COLOR_BUFFER_KEY;
            colorInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            colorInfo.target = GLES20.GL_ARRAY_BUFFER;
            final BufferInfo old = setBufferInfo(COLOR_BUFFER_KEY, colorInfo);
            //TODO: Cleanup old buffer info
        }
        if (override || colorInfo.buffer == null) {
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

    @Nullable
    public FloatBuffer getColors() {
        final BufferInfo info = getBufferInfo(COLOR_BUFFER_KEY);
        if (info == null) {
            return null;
        } else {
            return (FloatBuffer) info.buffer;
        }
    }

    public void setBuffersCreated(boolean created) {
        haveCreatedBuffers = created;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public void setNumVertices(int numVertices) {
        this.numVertices = numVertices;
    }

    @Nullable
    @Override
    public BufferInfo getVertexBufferInfo() {
        return getBufferInfo(VERTEX_BUFFER_KEY);
    }

    public void setVertexBufferInfo(BufferInfo vertexBufferInfo) {
        final BufferInfo info = setBufferInfo(VERTEX_BUFFER_KEY, vertexBufferInfo);
        //TODO: Cleanup old buffer info
    }

    @Nullable
    public BufferInfo getNormalBufferInfo() {
        return getBufferInfo(NORMAL_BUFFER_KEY);
    }

    public void setNormalBufferInfo(BufferInfo normalBufferInfo) {
        final BufferInfo info = setBufferInfo(NORMAL_BUFFER_KEY, normalBufferInfo);
        //TODO: Cleanup old buffer info
        this.hasNormals = true;
    }

    @Nullable
    public BufferInfo getTexCoordBufferInfo() {
        return getBufferInfo(TEXTURE_BUFFER_KEY);
    }

    public void setTexCoordBufferInfo(BufferInfo texCoordBufferInfo) {
        final BufferInfo info = setBufferInfo(TEXTURE_BUFFER_KEY, texCoordBufferInfo);
        //TODO: Cleanup old buffer info
        this.hasTextureCoordinates = true;
    }

    @Nullable
    public BufferInfo getColorBufferInfo() {
        return getBufferInfo(COLOR_BUFFER_KEY);
    }

    public void setColorBufferInfo(BufferInfo colorBufferInfo) {
        final BufferInfo info = setBufferInfo(COLOR_BUFFER_KEY, colorBufferInfo);
        //TODO: Cleanup old buffer info
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (hasBuffer(VERTEX_BUFFER_KEY) && getBufferInfo(VERTEX_BUFFER_KEY).buffer != null) {
            builder.append(", vertices: ").append(getBufferInfo(VERTEX_BUFFER_KEY).buffer.capacity());
        }
        if (hasBuffer(NORMAL_BUFFER_KEY) && getBufferInfo(NORMAL_BUFFER_KEY).buffer != null) {
            builder.append(", normals: ").append(getBufferInfo(NORMAL_BUFFER_KEY).buffer.capacity());
        }
        if (hasBuffer(TEXTURE_BUFFER_KEY) && getBufferInfo(TEXTURE_BUFFER_KEY).buffer != null) {
            builder.append(", uvs: ").append(getBufferInfo(TEXTURE_BUFFER_KEY).buffer.capacity()).append("\n");
        }
        if (hasBuffer(COLOR_BUFFER_KEY) && getBufferInfo(COLOR_BUFFER_KEY).buffer != null) {
            builder.append(", colors: ").append(getBufferInfo(COLOR_BUFFER_KEY).buffer.capacity()).append("\n");
        }

        addBufferHandles(builder);

        return builder.toString();
    }
}
