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

import net.jcip.annotations.NotThreadSafe;

import org.rajawali3d.animation.mesh.VertexAnimationObject3D;
import org.rajawali3d.math.vector.Vector3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import c.org.rajawali3d.gl.buffers.BufferUsage;
import c.org.rajawali3d.util.FloatBufferWrapper;

import static c.org.rajawali3d.gl.buffers.BufferInfo.BYTE_BUFFER;
import static c.org.rajawali3d.gl.buffers.BufferInfo.INT_BUFFER;
import static c.org.rajawali3d.gl.buffers.BufferInfo.SHORT_BUFFER;

/**
 * This is where the vertex, normal, texture coordinate, color and index data is stored. The data is stored in
 * various {@link Buffer}s. The data is uploaded to the graphics card using VERTEX Buffer Objects (VBOs). The data in
 * the {@link Buffer}s is kept in memory in order to restore the VBOs when the OpenGL context needs to be restored
 * (typically when the application regains focus).
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SuppressWarnings("WeakerAccess")
@NotThreadSafe
public class NonInterleavedGeometry extends IndexedGeometry {

    private static final String TAG = "NonInterleavedGeomtry";

    /**
     * Keys for the commonly used vertex attributes. If -1, no buffer has been stored for that key.
     */
    private int vertexBufferKey  = -1;
    private int normalBufferKey  = -1;
    private int textureBufferKey = -1;
    private int colorBufferKey   = -1;

    /**
     * Indicates whether this geometry contains normals or not.
     */
    private boolean hasNormals;

    /**
     * Indicates whether this geometry contains texture coordinates or not.
     */
    private boolean hasTextureCoordinates;

    /**
     * Indicates whether this geometry contains vertex colors or not.
     */
    private boolean hasVertexColors;

    /**
     * The number of vertices currently stored in the vertex buffer.
     */
    private int numVertices;

    /**
     * A pointer to the original geometry. This is not null when the object has been cloned. When cloning a
     * {@link NonInterleavedGeometry} the data isn't copied over, only the handle to the OpenGL buffers are used. A
     * reference to the original is kept to ensure the data does not go out of scope prematurely.
     */
    protected NonInterleavedGeometry sourceGeometry;

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
        final FloatBufferWrapper vertices = getVertices();
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
    public void issueDrawCalls(@DrawingMode int drawingMode) {
        int bufferType = getIndexBufferInfo().bufferType;
        int bindType = GLES20.GL_UNSIGNED_BYTE;
        switch (bufferType) {
            case BYTE_BUFFER:
                break;
            case SHORT_BUFFER:
                bindType = GLES20.GL_UNSIGNED_SHORT;
                break;
            case INT_BUFFER:
                bindType = GLES20.GL_UNSIGNED_INT;
                break;
        }
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, getIndexBufferInfo().glHandle);
        GLES20.glDrawElements(drawingMode, getNumberIndices(), bindType, 0);
        // GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0); We are currently not forcefully unbinding as it is deemed unnecessary.
    }

    @Override
    @Nullable
    public FloatBufferWrapper getVertices() {
        final BufferInfo info = getBufferInfo(vertexBufferKey);
        if (info == null) {
            return null;
        } else {
            return new FloatBufferWrapper(info);
        }
    }

    @Override
    @Nullable
    public FloatBufferWrapper getNormals() {
        final BufferInfo info = getBufferInfo(normalBufferKey);
        if (info == null) {
            return null;
        } else {
            return new FloatBufferWrapper(info);
        }
    }

    @Override
    @Nullable
    public FloatBufferWrapper getTextureCoords() {
        final BufferInfo info = getBufferInfo(textureBufferKey);
        if (info == null) {
            return null;
        } else {
            return new FloatBufferWrapper(info);
        }
    }

    @Override
    @Nullable
    public FloatBufferWrapper getColors() {
        final BufferInfo info = getBufferInfo(colorBufferKey);
        if (info == null) {
            return null;
        } else {
            return new FloatBufferWrapper(info);
        }
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
        // TODO: Cleanup of replaced buffers?
        numVertices = geometry.getVertexCount();

        BufferInfo info = geometry.getVertexBufferInfo();
        if (info != null) {
            if (vertexBufferKey < 0) {
                vertexBufferKey = addBuffer(info);
            } else {
                setBufferInfo(vertexBufferKey, info);
            }
        }
        info = geometry.getNormalBufferInfo();
        if (info != null) {
            if (normalBufferKey < 0) {
                normalBufferKey = addBuffer(info);
            } else {
                setBufferInfo(normalBufferKey, info);
            }
        }
        info = geometry.getTexCoordBufferInfo();
        if (info != null) {
            if (textureBufferKey < 0) {
                textureBufferKey = addBuffer(info);
            } else {
                setBufferInfo(textureBufferKey, info);
            }
        }
        info = geometry.getColorBufferInfo();
        if (info != null) {
            if (colorBufferKey < 0) {
                colorBufferKey = addBuffer(info);
            } else {
                setBufferInfo(colorBufferKey, info);
            }
        }
        super.copyFrom(geometry);
        sourceGeometry = geometry;
        hasNormals = geometry.hasNormals();
        hasTextureCoordinates = geometry.hasTextureCoordinates();
    }

    /**
     * Sets the geometry data. This methods takes two {@link BufferInfo} objects which means it will use another
     * NonInterleavedGeometry instance's data (vertices and normals). The remaining parameters are arrays which will
     * be used to create buffers that are unique to this instance.
     *
     * This is typically used with {@link VertexAnimationObject3D} instances.
     *
     * @param vertexBufferInfo {@link BufferInfo} providing the vertex data.
     * @param normalBufferInfo {@link BufferInfo} providing the normal data.
     * @param textureCoords    {@code float} array containing the texture coordinate data.
     * @param colors           {@code float} array containing the vertex color data.
     * @param indices          {@code int} array containing the vertex index data.
     * @param createVBOs       {@code true} if the VBOs should be constructed immediately. This requires calling on
     *                         the GL thread.
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

        vertexBufferKey = addBuffer(vertexBufferInfo);
        normalBufferKey = addBuffer(normalBufferInfo);

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
     *                           the GL
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

        // We can skip null safety check for buffers because hasBuffer enforces it. Vertices and indices must always
        // be present based on the preceding code as well
        getBufferInfo(vertexBufferKey).usage = verticesUsage;
        if (hasBuffer(normalBufferKey)) {
            getBufferInfo(normalBufferKey).usage = normalsUsage;
        }
        if (hasBuffer(textureBufferKey)) {
            getBufferInfo(textureBufferKey).usage = textureCoordsUsage;
        }
        if (hasBuffer(colorBufferKey)) {
            getBufferInfo(colorBufferKey).usage = colorsUsage;
        }
        final BufferInfo indexInfo = getIndexBufferInfo();
        indexInfo.usage = indicesUsage;

        if (createVBOs) {
            createBuffers();
        }
    }

    /**
     * Creates the vertex and normal buffers only. This is typically used for a VertexAnimationObject3D's frames.
     *
     * @throws IllegalStateException if no data has been set for the vertices or normals.
     * @see VertexAnimationObject3D
     */
    public void createVertexAndNormalBuffersOnly() throws IllegalStateException {
        if (!hasBuffer(vertexBufferKey) || !hasBuffer(normalBufferKey)) {
            throw new IllegalStateException("Cannot create vertex and normal buffers when no data has been provided.");
        }
        final BufferInfo vertex = getBufferInfo(vertexBufferKey);
        final BufferInfo normal = getBufferInfo(normalBufferKey);
        ((FloatBuffer) vertex.buffer).compact().position(0);
        ((FloatBuffer) normal.buffer).compact().position(0);

        createBufferObject(vertex, BufferInfo.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);
        createBufferObject(normal, BufferInfo.FLOAT_BUFFER, GLES20.GL_ARRAY_BUFFER);

        // Ensure we clear the current buffer binding
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void setVertices(@NonNull float[] vertices) {
        setVertices(vertices, false);
    }

    @Override
    public void setVertices(@NonNull float[] vertices, boolean override) {
        BufferInfo vertexInfo;
        if (vertexBufferKey < 0) {
            vertexInfo = new BufferInfo();
            vertexInfo.rajawaliHandle = vertexBufferKey;
            vertexInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            vertexInfo.target = GLES20.GL_ARRAY_BUFFER;
            vertexBufferKey = addBuffer(vertexInfo);
        } else {
            vertexInfo = getBufferInfo(vertexBufferKey);
        }
        if (vertexInfo == null) {
            throw new IllegalStateException("Expected to find vertex buffer info, but was null.");
        }
        if (override || vertexInfo.buffer == null) {
            vertexInfo.buffer = ByteBuffer
                    .allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();

            ((FloatBuffer) vertexInfo.buffer).put(vertices);
            vertexInfo.buffer.rewind();
        } else {
            vertexInfo.buffer.rewind();
            ((FloatBuffer) vertexInfo.buffer).put(vertices);
            vertexInfo.buffer.rewind();
        }
        numVertices = vertexInfo.buffer.capacity() / 3;
    }

    public void setVertices(@NonNull FloatBuffer vertices) {
        vertices.rewind();
        float[] v = new float[vertices.capacity()];
        vertices.get(v);
        setVertices(v);
    }

    @Override
    public void setNormals(@NonNull float[] normals) {
        setNormals(normals, false);
    }

    @Override
    public void setNormals(@NonNull float[] normals, boolean override) {
        BufferInfo normalInfo;
        if (normalBufferKey < 0) {
            normalInfo = new BufferInfo();
            normalInfo.rajawaliHandle = normalBufferKey;
            normalInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            normalInfo.target = GLES20.GL_ARRAY_BUFFER;
            normalBufferKey = addBuffer(normalInfo);
        } else {
            normalInfo = getBufferInfo(normalBufferKey);
        }
        if (normalInfo == null) {
            throw new IllegalStateException("Expected to find normal buffer info, but was null.");
        }
        if (override || normalInfo.buffer == null) {
            normalInfo.buffer = ByteBuffer.allocateDirect(normals.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) normalInfo.buffer).put(normals);
            normalInfo.buffer.rewind();
        } else {
            final FloatBuffer buffer = ((FloatBuffer) normalInfo.buffer);
            buffer.rewind();
            buffer.put(normals);
            buffer.rewind();
        }

        hasNormals = true;
    }

    public void setNormals(@NonNull FloatBuffer normals) {
        normals.rewind();
        float[] n = new float[normals.capacity()];
        normals.get(n);
        setNormals(n);
    }

    public boolean hasNormals() {
        return hasNormals;
    }

    @Override
    public void setTextureCoords(@NonNull float[] textureCoords) {
        setTextureCoords(textureCoords, false);
    }

    @Override
    public void setTextureCoords(@NonNull float[] textureCoords, boolean override) {
        BufferInfo textureInfo;
        if (textureBufferKey < 0) {
            textureInfo = new BufferInfo();
            textureInfo.rajawaliHandle = textureBufferKey;
            textureInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            textureInfo.target = GLES20.GL_ARRAY_BUFFER;
            textureBufferKey = addBuffer(textureInfo);
        } else {
            textureInfo = getBufferInfo(textureBufferKey);
        }
        if (textureInfo == null) {
            throw new IllegalStateException("Expected to find texture buffer info, but was null.");
        }
        if (override || textureInfo.buffer == null) {
            textureInfo.buffer = ByteBuffer
                    .allocateDirect(textureCoords.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) textureInfo.buffer).put(textureCoords);
            textureInfo.buffer.rewind();
        } else {
            textureInfo.buffer.rewind();
            ((FloatBuffer) textureInfo.buffer).put(textureCoords);
            textureInfo.buffer.rewind();
        }

        hasTextureCoordinates = true;
    }

    public void setTextureCoords(@NonNull FloatBuffer textureCoords) {
        textureCoords.rewind();
        float[] n = new float[textureCoords.capacity()];
        textureCoords.get(n);
        setTextureCoords(n);
    }

    public boolean hasTextureCoordinates() {
        return hasTextureCoordinates;
    }

    public void setColor(float r, float g, float b, float a) {
        setColor(r, g, b, a, false);
    }

    public void setColor(float r, float g, float b, float a, boolean override) {
        BufferInfo colorInfo;
        if (colorBufferKey < 0) {
            colorInfo = new BufferInfo();
            colorInfo.rajawaliHandle = colorBufferKey;
            colorInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            colorInfo.target = GLES20.GL_ARRAY_BUFFER;
            colorInfo.buffer = ByteBuffer.allocateDirect(numVertices * 4 * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            colorBufferKey = addBuffer(colorInfo);
        } else {
            colorInfo = getBufferInfo(colorBufferKey);
        }
        if (colorInfo == null) {
            throw new IllegalStateException("Expected to find color buffer info, but was null.");
        }
        if (override || colorInfo.buffer == null) {
            colorInfo.buffer = ByteBuffer
                    .allocateDirect(numVertices * 4 * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        colorInfo.buffer.rewind();

        while (colorInfo.buffer.remaining() > 3) {
            ((FloatBuffer) colorInfo.buffer).put(r);
            ((FloatBuffer) colorInfo.buffer).put(g);
            ((FloatBuffer) colorInfo.buffer).put(b);
            ((FloatBuffer) colorInfo.buffer).put(a);
        }
        colorInfo.buffer.rewind();
        hasVertexColors = true;
    }

    public void setColors(int color) {
        setColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
    }

    @Override
    public void setColors(@NonNull float[] colors) {
        setColors(colors, false);
    }

    @Override
    public void setColors(@NonNull float[] colors, boolean override) {
        BufferInfo colorInfo;
        if (colorBufferKey < 0) {
            colorInfo = new BufferInfo();
            colorInfo.rajawaliHandle = colorBufferKey;
            colorInfo.bufferType = BufferInfo.FLOAT_BUFFER;
            colorInfo.target = GLES20.GL_ARRAY_BUFFER;
            colorBufferKey = addBuffer(colorInfo);
        } else {
            colorInfo = getBufferInfo(colorBufferKey);
        }
        if (colorInfo == null) {
            throw new IllegalStateException("Expected to find color buffer info, but was null.");
        }
        if (override || colorInfo.buffer == null) {
            colorInfo.buffer = ByteBuffer
                    .allocateDirect(colors.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            ((FloatBuffer) colorInfo.buffer).put(colors);
            colorInfo.buffer.rewind();
        } else {
            colorInfo.buffer.rewind();
            ((FloatBuffer) colorInfo.buffer).put(colors);
            colorInfo.buffer.rewind();
        }
        hasVertexColors = true;
    }

    public void setColors(@NonNull FloatBuffer colors) {
        colors.rewind();
        float[] n = new float[colors.capacity()];
        colors.get(n);
        setColors(n);
    }

    public boolean hasVertexColors() {
        return hasVertexColors;
    }

    @Nullable
    @Override
    public BufferInfo getVertexBufferInfo() {
        return getBufferInfo(vertexBufferKey);
    }

    public void setVertexBufferInfo(BufferInfo bufferInfo) {
        if (vertexBufferKey < 0) {
            vertexBufferKey = addBuffer(bufferInfo);
        } else {
            final BufferInfo info = setBufferInfo(vertexBufferKey, bufferInfo);
            //TODO: Cleanup old buffer info
        }
    }

    @Nullable
    public BufferInfo getNormalBufferInfo() {
        return getBufferInfo(normalBufferKey);
    }

    public void setNormalBufferInfo(BufferInfo bufferInfo) {
        if (normalBufferKey < 0) {
            normalBufferKey = addBuffer(bufferInfo);
        } else {
            final BufferInfo info = setBufferInfo(normalBufferKey, bufferInfo);
            //TODO: Cleanup old buffer info
        }
        hasNormals = true;
    }

    @Nullable
    public BufferInfo getTexCoordBufferInfo() {
        return getBufferInfo(textureBufferKey);
    }

    public void setTexCoordBufferInfo(BufferInfo bufferInfo) {
        if (textureBufferKey < 0) {
            textureBufferKey = addBuffer(bufferInfo);
        } else {
            final BufferInfo info = setBufferInfo(textureBufferKey, bufferInfo);
            //TODO: Cleanup old buffer info
            hasTextureCoordinates = true;
        }
    }

    @Nullable
    public BufferInfo getColorBufferInfo() {
        return getBufferInfo(colorBufferKey);
    }

    public void setColorBufferInfo(BufferInfo bufferInfo) {
        if (colorBufferKey < 0) {
            colorBufferKey = addBuffer(bufferInfo);
        } else {
            final BufferInfo info = setBufferInfo(colorBufferKey, bufferInfo);
            //TODO: Cleanup old buffer info
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (hasBuffer(vertexBufferKey) && getBufferInfo(vertexBufferKey).buffer != null) {
            builder.append(", vertices: ").append(getBufferInfo(vertexBufferKey).buffer.capacity());
        }
        if (hasBuffer(normalBufferKey) && getBufferInfo(normalBufferKey).buffer != null) {
            builder.append(", normals: ").append(getBufferInfo(normalBufferKey).buffer.capacity());
        }
        if (hasBuffer(textureBufferKey) && getBufferInfo(textureBufferKey).buffer != null) {
            builder.append(", uvs: ").append(getBufferInfo(textureBufferKey).buffer.capacity()).append("\n");
        }
        if (hasBuffer(colorBufferKey) && getBufferInfo(colorBufferKey).buffer != null) {
            builder.append(", colors: ").append(getBufferInfo(colorBufferKey).buffer.capacity()).append("\n");
        }

        addBufferHandles(builder);

        return builder.toString();
    }

    @Override
    protected int getVertexCount() {
        return numVertices;
    }

    @Override
    protected boolean hasVertexData() {
        return getVertexBufferInfo() != null;
    }
}
