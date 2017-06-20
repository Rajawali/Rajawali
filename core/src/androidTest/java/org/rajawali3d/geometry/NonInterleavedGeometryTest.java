package org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;
import android.opengl.GLES20;
import android.support.annotation.Nullable;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.buffers.BufferInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.rajawali3d.math.vector.Vector3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class NonInterleavedGeometryTest extends GlTestCase {

    static float[] createVertexArray() {
        final float[] vertices = new float[4];
        vertices[0] = 1.0f;
        vertices[1] = 2.0f;
        vertices[2] = 3.0f;
        vertices[3] = 4.0f;
        return vertices;
    }

    static float[] createNormalArray() {
        final float[] normals = new float[4];
        normals[0] = 5.0f;
        normals[1] = 6.0f;
        normals[2] = 7.0f;
        normals[3] = 8.0f;
        return normals;
    }

    static float[] createColorArray() {
        final float[] colors = new float[4];
        colors[0] = 9.0f;
        colors[1] = 10.0f;
        colors[2] = 11.0f;
        colors[3] = 12.0f;
        return colors;
    }

    static float[] createTextureArray() {
        final float[] textures = new float[4];
        textures[0] = 13.0f;
        textures[1] = 14.0f;
        textures[2] = 15.0f;
        textures[3] = 16.0f;
        return textures;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp(getClass().getSimpleName());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testValidateBuffers() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final NonInterleavedGeometry source = Mockito.spy(new NonInterleavedGeometry());
        Mockito.doNothing().when(geometry).createBuffers();
        Mockito.doNothing().when(source).createBuffers();
        Mockito.doNothing().when(geometry).createBufferObject(Mockito.any(BufferInfo.class));
        Mockito.doNothing().when(source).createBufferObject(Mockito.any(BufferInfo.class));

        // Test super call first
        geometry.validateBuffers();

        // Add a source geometry and call it
        geometry.copyFrom(source);
        geometry.validateBuffers();
        Mockito.verify(source).validateBuffers();
    }

    @Test
    public void testReload() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final NonInterleavedGeometry source = Mockito.spy(new NonInterleavedGeometry());
        // Test super call first
        geometry.reload();

        // Add a source geometry and call it
        geometry.copyFrom(source);
        Mockito.doReturn(false).when(source).isValid();
        geometry.reload();

        Mockito.doReturn(true).when(source).isValid();
        geometry.reload();
        Mockito.verify(source, Mockito.times(1)).reload();
    }

    @Test
    public void testDestroy() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final NonInterleavedGeometry source = Mockito.spy(new NonInterleavedGeometry());
        // Test super call first
        geometry.destroy();

        // Add a source geometry and call it
        geometry.copyFrom(source);
        geometry.destroy();
        Mockito.verify(source).destroy();
    }

    @Test
    public void testCalculateAABounds() throws Exception {
        final NonInterleavedGeometry geometry = new NonInterleavedGeometry();
        final float[] pointVertices = new float[] { 0, 0, 0 };
        final Vector3 expectedZero = new Vector3(0);
        geometry.setVertices(pointVertices);
        final Vector3 min = new Vector3();
        final Vector3 max = new Vector3();
        geometry.calculateAABounds(min, max);
        assertTrue(expectedZero.equals(min, 1e-14));
        assertTrue(expectedZero.equals(max, 1e-14));

        final float[] originCenteredCubeVertices = new float[] { -1, -1, -1,
                                                                 -1, -1, 1,
                                                                 1, -1, 1,
                                                                 1, -1, -1,
                                                                 -1, 1, -1,
                                                                 -1, 1, 1,
                                                                 1, 1, 1,
                                                                 1, 1, -1 };
        final Vector3 expectedMin = new Vector3(-1);
        final Vector3 expectedMax = new Vector3(1);
        final NonInterleavedGeometry geometry1 = new NonInterleavedGeometry();
        geometry1.setVertices(originCenteredCubeVertices);
        geometry1.calculateAABounds(min, max);
        assertTrue(expectedMin.equals(min, 1e-14));
        assertTrue(expectedMax.equals(max, 1e-14));
    }

    @Test
    public void testIssueDrawCalls() throws Exception {

    }

    @Test
    public void testCopyFrom() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final NonInterleavedGeometry source = Mockito.spy(new NonInterleavedGeometry());

        // Test with nulls
        Mockito.doReturn(0).when(source).getVertexCount();
        Mockito.doReturn(false).when(source).hasNormals();
        Mockito.doReturn(false).when(source).hasNormals();
        geometry.copyFrom(source);
        assertEquals(0, geometry.getVertexCount());
        assertEquals(source, geometry.sourceGeometry);
        assertFalse(geometry.hasNormals());
        assertFalse(geometry.hasTextureCoordinates());

        // Test with buffers, no keys
        BufferInfo vInfo = Mockito.mock(BufferInfo.class);
        BufferInfo nInfo = Mockito.mock(BufferInfo.class);
        BufferInfo tInfo = Mockito.mock(BufferInfo.class);
        BufferInfo cInfo = Mockito.mock(BufferInfo.class);
        Mockito.doReturn(vInfo).when(source).getVertexBufferInfo();
        Mockito.doReturn(nInfo).when(source).getNormalBufferInfo();
        Mockito.doReturn(tInfo).when(source).getTexCoordBufferInfo();
        Mockito.doReturn(cInfo).when(source).getColorBufferInfo();
        geometry.copyFrom(source);
        assertEquals(vInfo, geometry.getVertexBufferInfo());
        assertEquals(nInfo, geometry.getNormalBufferInfo());
        assertEquals(tInfo, geometry.getTexCoordBufferInfo());
        assertEquals(cInfo, geometry.getColorBufferInfo());

        // Test with buffers, keys
        vInfo = Mockito.mock(BufferInfo.class);
        nInfo = Mockito.mock(BufferInfo.class);
        tInfo = Mockito.mock(BufferInfo.class);
        cInfo = Mockito.mock(BufferInfo.class);
        Mockito.doReturn(vInfo).when(source).getVertexBufferInfo();
        Mockito.doReturn(nInfo).when(source).getNormalBufferInfo();
        Mockito.doReturn(tInfo).when(source).getTexCoordBufferInfo();
        Mockito.doReturn(cInfo).when(source).getColorBufferInfo();
        geometry.copyFrom(source);
        assertEquals(vInfo, geometry.getVertexBufferInfo());
        assertEquals(nInfo, geometry.getNormalBufferInfo());
        assertEquals(tInfo, geometry.getTexCoordBufferInfo());
        assertEquals(cInfo, geometry.getColorBufferInfo());
    }

    @Test
    public void testSetDataVertexNormalBuffers() throws Exception {

    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSetDataStaticDrawCreateBuffers() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final float[] array = new float[1];
        final int[] intArray = new int[1];
        final int usage = GLES20.GL_STATIC_DRAW;
        final boolean create = false;
        Mockito.doNothing().when(geometry).setData(array, usage, array, usage, array, usage, array, usage, intArray,
                                                   usage, create);
        geometry.setData(array, array, array, array, intArray, create);
        Mockito.verify(geometry).setData(array, usage, array, usage, array, usage, array, usage, intArray, usage,
                                         create);
    }

    @Test
    public void testSetDataAllParametersCreateBuffers() throws Exception {
        // Create the dummy arrays
        final float[] vertices = createVertexArray();
        final float[] normals = createNormalArray();
        final float[] colors = createColorArray();
        final float[] textures = createTextureArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        Mockito.doNothing().when(geometry).createBuffers();

        // Call with zero lengths
        geometry.setData(new float[0], GLES20.GL_STATIC_DRAW, new float[0], GLES20.GL_STATIC_DRAW,
                         new float[0], GLES20.GL_STATIC_DRAW, new float[0], GLES20.GL_STATIC_DRAW,
                         new int[0], GLES20.GL_STATIC_DRAW, false);
        Mockito.verify(geometry, Mockito.times(0)).createBuffers();

        // Call with only vertices and indices
        geometry.setData(vertices, GLES20.GL_STATIC_DRAW, null, GLES20.GL_STATIC_DRAW, null, GLES20.GL_STATIC_DRAW,
                         null, GLES20.GL_STATIC_DRAW, indices, GLES20.GL_STATIC_DRAW, false);

        Mockito.verify(geometry, Mockito.times(0)).createBuffers();

        BufferInfo vertexInfo = geometry.getVertexBufferInfo();
        BufferInfo normalInfo = geometry.getNormalBufferInfo();
        BufferInfo colorInfo = geometry.getColorBufferInfo();
        BufferInfo textureInfo = geometry.getTexCoordBufferInfo();
        BufferInfo indexInfo = geometry.getIndexBufferInfo();

        assertNotNull(vertexInfo);
        assertNull(normalInfo);
        assertNull(colorInfo);
        assertNull(textureInfo);
        assertNotNull(indexInfo);

        FloatBuffer vertexBuffer = (FloatBuffer) vertexInfo.buffer;
        Buffer indexBuffer = indexInfo.buffer;

        assertEquals("Vertex buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, vertexInfo.bufferType);
        assertEquals("Vertex buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, vertexInfo.usage);
        assertEquals("Index buffer info set to wrong type.", BufferInfo.BYTE_BUFFER, indexInfo.bufferType);
        assertEquals("Index buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, indexInfo.usage);
        assertEquals("Number of vertices invalid.", 1, geometry.getVertexCount());
        int i = 0;
        while (vertexBuffer.hasRemaining()) {
            assertEquals("Vertex buffer contents invalid.", vertexBuffer.get(), vertices[i++], 0);
        }
        i = 0;
        while (indexBuffer.hasRemaining()) {
            assertEquals("Index buffer contents invalid.", ((ByteBuffer) indexBuffer).get(), indices[i++], 0);
        }

        // Call with everything
        geometry.setData(vertices, GLES20.GL_STATIC_DRAW, normals, GLES20.GL_STREAM_DRAW,
                         textures, GLES20.GL_DYNAMIC_DRAW, colors, GLES20.GL_STATIC_DRAW,
                         indices, GLES20.GL_STREAM_DRAW, true);

        Mockito.verify(geometry).createBuffers();

        vertexInfo = geometry.getVertexBufferInfo();
        normalInfo = geometry.getNormalBufferInfo();
        colorInfo = geometry.getColorBufferInfo();
        textureInfo = geometry.getTexCoordBufferInfo();
        indexInfo = geometry.getIndexBufferInfo();
        vertexBuffer = (FloatBuffer) vertexInfo.buffer;
        indexBuffer = indexInfo.buffer;
        FloatBuffer normalBuffer = (FloatBuffer) normalInfo.buffer;
        FloatBuffer colorBuffer = (FloatBuffer) colorInfo.buffer;
        FloatBuffer textureBuffer = (FloatBuffer) textureInfo.buffer;
        assertEquals("Vertex buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, vertexInfo.bufferType);
        assertEquals("Normal buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, normalInfo.bufferType);
        assertEquals("Color buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, colorInfo.bufferType);
        assertEquals("Texture2D buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, textureInfo.bufferType);
        assertEquals("Index buffer info set to wrong type.", BufferInfo.BYTE_BUFFER, indexInfo.bufferType);
        assertEquals("Vertex buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, vertexInfo.usage);
        assertEquals("Normal buffer info set to wrong usage.", GLES20.GL_STREAM_DRAW, normalInfo.usage);
        assertEquals("Color buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, colorInfo.usage);
        assertEquals("Texture2D buffer info set to wrong usage.", GLES20.GL_DYNAMIC_DRAW, textureInfo.usage);
        assertEquals("Index buffer info set to wrong usage.", GLES20.GL_STREAM_DRAW, indexInfo.usage);

        assertEquals("Number of vertices invalid.", 1, geometry.getVertexCount());
        i = 0;
        while (vertexBuffer.hasRemaining()) {
            assertEquals("Vertex buffer contents invalid.", vertexBuffer.get(), vertices[i++], 0);
        }
        i = 0;
        while (normalBuffer.hasRemaining()) {
            assertEquals("Normal buffer contents invalid.", normalBuffer.get(), normals[i++], 0);
        }
        i = 0;
        while (colorBuffer.hasRemaining()) {
            assertEquals("Color buffer contents invalid.", colorBuffer.get(), colors[i++], 0);
        }
        i = 0;
        while (textureBuffer.hasRemaining()) {
            assertEquals("Texture2D buffer contents invalid.", textureBuffer.get(), textures[i++], 0);
        }
        i = 0;
        while (indexBuffer.hasRemaining()) {
            assertEquals("Index buffer contents invalid.", ((ByteBuffer) indexBuffer).get(), indices[i++], 0);
        }
    }

    @Test
    public void testCreateVertexAndNormalBuffersOnly() {

    }

    @Test
    public void testHasVertexData() throws Exception {
        final NonInterleavedGeometry geometry = new NonInterleavedGeometry();
        assertFalse(geometry.hasVertexData());
        geometry.setVertices(new float[3]);
        assertTrue(geometry.hasVertexData());
    }

    @Test
    public void testSetVertices() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        Mockito.doNothing().when(geometry).setVertices(Mockito.any(float[].class), Mockito.anyBoolean());
        final float[] array = new float[1];
        geometry.setVertices(array);
        Mockito.verify(geometry).setVertices(array, false);
    }

    @Test
    public void testSetVerticesOverride() throws Exception {
        // Create the dummy arrays
        final float[] vertices = createVertexArray();
        final float[] vertices2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        // Test when no buffer exists, no override (valid buffer key, null buffer)
        geometry.setVertices(vertices, false);
        BufferInfo vertexInfo = geometry.getVertexBufferInfo();
        assertNotNull(vertexInfo);
        assertTrue(vertexInfo.rajawaliHandle >= 0);
        assertEquals(BufferInfo.FLOAT_BUFFER, vertexInfo.bufferType);
        assertEquals(GLES20.GL_ARRAY_BUFFER, vertexInfo.target);
        assertEquals(1, geometry.getVertexCount());
        Mockito.verify(geometry).addBuffer(vertexInfo);
        FloatBuffer buffer = geometry.getVertices();
        assertNotNull(buffer);
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", vertices[i++], buffer.get(), 0);
        }

        // Test when buffer exists, no override (valid buffer key, non-null buffer)
        geometry.setVertices(vertices2, false);
        vertexInfo = geometry.getVertexBufferInfo();
        assertNotNull(vertexInfo);
        buffer = geometry.getVertices();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", vertices2[i++], buffer.get(), 0);
        }

        // Test override, valid buffer key
        geometry.setVertices(vertices, true);
        vertexInfo = geometry.getVertexBufferInfo();
        assertNotNull(vertexInfo);
        buffer = geometry.getVertices();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", vertices[i++], buffer.get(), 0);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testSetVerticesOverrideFailNullInfo() throws Exception {
        final NonInterleavedGeometry geometry = new NonInterleavedGeometry() {

            @Nullable
            @Override
            protected BufferInfo getBufferInfo(int bufferKey) {
                return null;
            }
        };

        geometry.setVertices(new float[1], true);
        geometry.setVertices(new float[1], true);
    }

    @Test
    public void testSetVerticesFloatBuffer() throws Exception {
        // Create the dummy arrays
        final float[] vertices = createVertexArray();
        final float[] vertices2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(vertices, null, null, null, indices, true);
            }
        });
        bufferObject.setVertices(FloatBuffer.wrap(vertices2));
        final FloatBuffer buffer = bufferObject.getVertices();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", vertices2[i++], buffer.get(), 0);
        }
    }

    @Test
    public void testGetVertices() throws Exception {
        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();
        assertNull(bufferObject.getVertices());

        final float[] vertices = createVertexArray();
        bufferObject.setVertices(vertices);
        final FloatBuffer buffer = bufferObject.getVertices();
        assertNotNull(buffer);
        for (int i = 0; i < vertices.length; ++i) {
            assertEquals(vertices[i], buffer.get(i), 1e-6);
        }
    }

    @Test
    public void testSetNormals() throws Exception {
        // Create the dummy arrays
        final float[] normals = createVertexArray();
        final float[] normals2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        // Test when no buffer exists, no override (valid buffer key, null buffer)
        geometry.setNormals(normals, false);
        BufferInfo normalInfo = geometry.getNormalBufferInfo();
        assertNotNull(normalInfo);
        assertTrue(normalInfo.rajawaliHandle >= 0);
        assertEquals(BufferInfo.FLOAT_BUFFER, normalInfo.bufferType);
        assertEquals(GLES20.GL_ARRAY_BUFFER, normalInfo.target);
        assertTrue(geometry.hasNormals());
        Mockito.verify(geometry).addBuffer(normalInfo);
        FloatBuffer buffer = geometry.getNormals();
        assertNotNull(buffer);
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", normals[i++], buffer.get(), 0);
        }

        // Test when buffer exists, no override (valid buffer key, non-null buffer)
        geometry.setNormals(normals2, false);
        normalInfo = geometry.getNormalBufferInfo();
        assertNotNull(normalInfo);
        buffer = geometry.getNormals();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", normals2[i++], buffer.get(), 0);
        }

        // Test override, valid buffer key
        geometry.setNormals(normals, true);
        normalInfo = geometry.getNormalBufferInfo();
        assertNotNull(normalInfo);
        buffer = geometry.getNormals();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", normals[i++], buffer.get(), 0);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testSetNormalsOverrideFailNullInfo() throws Exception {
        final NonInterleavedGeometry geometry = new NonInterleavedGeometry() {

            @Nullable
            @Override
            protected BufferInfo getBufferInfo(int bufferKey) {
                return null;
            }
        };

        geometry.setNormals(new float[1], true);
        geometry.setNormals(new float[1], true);
    }

    @Test
    public void testSetNormalsFloatBuffer() throws Exception {
        // Create the dummy arrays
        final float[] normals = createVertexArray();
        final float[] normals2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(normals, normals, null, null, indices, true);
            }
        });
        bufferObject.setNormals(FloatBuffer.wrap(normals2));
        final FloatBuffer buffer = bufferObject.getNormals();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", normals2[i++], buffer.get(), 0);
        }
    }

    @Test
    public void testGetNormals() throws Exception {
        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();
        assertNull(bufferObject.getNormals());

        final float[] normals = createNormalArray();
        bufferObject.setNormals(normals);
        final FloatBuffer buffer = bufferObject.getNormals();
        assertNotNull(buffer);
        for (int i = 0; i < normals.length; ++i) {
            assertEquals(normals[i], buffer.get(i), 1e-6);
        }
    }

    @Test
    public void testSetTextureCoords() throws Exception {
        // Create the dummy arrays
        final float[] texCoords = createVertexArray();
        final float[] texCoords2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        // Test when no buffer exists, no override (valid buffer key, null buffer)
        geometry.setTextureCoords(texCoords, false);
        BufferInfo texInfo = geometry.getTexCoordBufferInfo();
        assertNotNull(texInfo);
        assertTrue(texInfo.rajawaliHandle >= 0);
        assertEquals(BufferInfo.FLOAT_BUFFER, texInfo.bufferType);
        assertEquals(GLES20.GL_ARRAY_BUFFER, texInfo.target);
        assertTrue(geometry.hasTextureCoordinates());
        Mockito.verify(geometry).addBuffer(texInfo);
        FloatBuffer buffer = geometry.getTextureCoords();
        assertNotNull(buffer);
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", texCoords[i++], buffer.get(), 0);
        }

        // Test when buffer exists, no override (valid buffer key, non-null buffer)
        geometry.setTextureCoords(texCoords2, false);
        texInfo = geometry.getTexCoordBufferInfo();
        assertNotNull(texInfo);
        buffer = geometry.getTextureCoords();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", texCoords2[i++], buffer.get(), 0);
        }

        // Test override, valid buffer key
        geometry.setTextureCoords(texCoords, true);
        texInfo = geometry.getTexCoordBufferInfo();
        assertNotNull(texInfo);
        buffer = geometry.getTextureCoords();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", texCoords[i++], buffer.get(), 0);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testSetTexCoordOverrideFailNullInfo() throws Exception {
        final NonInterleavedGeometry geometry = new NonInterleavedGeometry() {

            @Nullable
            @Override
            protected BufferInfo getBufferInfo(int bufferKey) {
                return null;
            }
        };

        geometry.setTextureCoords(new float[1], true);
        geometry.setTextureCoords(new float[1], true);
    }

    @Test
    public void testSetTexCoordsFloatBuffer() throws Exception {
        // Create the dummy arrays
        final float[] texCoords = createVertexArray();
        final float[] texCoords2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(texCoords, null, texCoords, null, indices, true);
            }
        });
        bufferObject.setTextureCoords(FloatBuffer.wrap(texCoords2));
        final FloatBuffer buffer = bufferObject.getTextureCoords();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", texCoords2[i++], buffer.get(), 0);
        }
    }

    @Test
    public void testGetTexCoords() throws Exception {
        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();
        assertNull(bufferObject.getTextureCoords());

        final float[] textures = createTextureArray();
        bufferObject.setTextureCoords(textures);
        final FloatBuffer buffer = bufferObject.getTextureCoords();
        assertNotNull(buffer);
        for (int i = 0; i < textures.length; ++i) {
            assertEquals(textures[i], buffer.get(i), 1e-6);
        }
    }

    @Test
    public void testSetColorComponentsNoOverride() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        geometry.setColor(0.1f, 0.2f, 0.3f, 0.4f);
        Mockito.verify(geometry).setColor(0.1f, 0.2f, 0.3f, 0.4f, false);
    }

    @Test
    public void testColorComponentsWithOverride() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final float[] colors = new float[] { 0.1f, 0.2f, 0.3f, 0.4f };
        final float[] colors2 = new float[] { 0.4f, 0.3f, 0.2f, 0.1f };

        // Test when no buffer exists, no override (valid buffer key, null buffer)
        geometry.setVertices(new float[3], true);
        geometry.setColor(0.1f, 0.2f, 0.3f, 0.4f, true);
        BufferInfo colorInfo = geometry.getColorBufferInfo();
        assertNotNull(colorInfo);
        assertTrue(colorInfo.rajawaliHandle >= 0);
        assertEquals(BufferInfo.FLOAT_BUFFER, colorInfo.bufferType);
        assertEquals(GLES20.GL_ARRAY_BUFFER, colorInfo.target);
        assertTrue(geometry.hasVertexColors());
        Mockito.verify(geometry).addBuffer(colorInfo);
        FloatBuffer buffer = geometry.getColors();
        assertNotNull(buffer);
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors[i++], buffer.get(), 0);
        }

        // Test when buffer exists, no override (valid buffer key, non-null buffer)
        geometry.setColor(0.4f, 0.3f, 0.2f, 0.1f, false);
        colorInfo = geometry.getColorBufferInfo();
        assertNotNull(colorInfo);
        buffer = geometry.getColors();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors2[i++], buffer.get(), 0);
        }

        // Test override, valid buffer key
        geometry.setColor(0.1f, 0.2f, 0.3f, 0.4f, true);
        colorInfo = geometry.getColorBufferInfo();
        assertNotNull(colorInfo);
        buffer = geometry.getColors();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors[i++], buffer.get(), 0);
        }

        final NonInterleavedGeometry geometry1 = new NonInterleavedGeometry();
        geometry1.setColor(0.1f, 0.2f, 0.3f, 0.4f, true);
        colorInfo = geometry1.getColorBufferInfo();
        assertNotNull(colorInfo);
        buffer = geometry1.getColors();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors[i++], buffer.get(), 0);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testSetColorComponentsOverrideFailNullInfo() throws Exception {
        final NonInterleavedGeometry geometry = new NonInterleavedGeometry() {

            @Nullable
            @Override
            protected BufferInfo getBufferInfo(int bufferKey) {
                return null;
            }
        };

        geometry.setColor(0.1f, 0.2f, 0.3f, 0.4f, true);
        geometry.setColor(0.1f, 0.2f, 0.3f, 0.4f, true);
    }

    @Test
    public void testSetColorsAllSame() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        geometry.setColors(150);
        Mockito.verify(geometry).setColor(Color.red(150), Color.green(150), Color.blue(150), Color.alpha(150), false);
    }

    @Test
    public void testSetColors() throws Exception {
        // Create the dummy arrays
        final float[] colors = createVertexArray();
        final float[] colors2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        // Test when no buffer exists, no override (valid buffer key, null buffer)
        geometry.setVertices(new float[3], true);
        geometry.setColors(colors, false);
        BufferInfo colorInfo = geometry.getColorBufferInfo();
        assertNotNull(colorInfo);
        assertTrue(colorInfo.rajawaliHandle >= 0);
        assertEquals(BufferInfo.FLOAT_BUFFER, colorInfo.bufferType);
        assertEquals(GLES20.GL_ARRAY_BUFFER, colorInfo.target);
        assertTrue(geometry.hasVertexColors());
        Mockito.verify(geometry).addBuffer(colorInfo);
        FloatBuffer buffer = geometry.getColors();
        assertNotNull(buffer);
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors[i++], buffer.get(), 0);
        }

        // Test when buffer exists, no override (valid buffer key, non-null buffer)
        geometry.setColors(colors2, false);
        colorInfo = geometry.getColorBufferInfo();
        assertNotNull(colorInfo);
        buffer = geometry.getColors();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors2[i++], buffer.get(), 0);
        }

        // Test override, valid buffer key
        geometry.setColors(colors, true);
        colorInfo = geometry.getColorBufferInfo();
        assertNotNull(colorInfo);
        buffer = geometry.getColors();
        assertNotNull(buffer);
        i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors[i++], buffer.get(), 0);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testSetColorsOverrideFailNullInfo() throws Exception {
        final NonInterleavedGeometry geometry = new NonInterleavedGeometry() {

            @Nullable
            @Override
            protected BufferInfo getBufferInfo(int bufferKey) {
                return null;
            }
        };

        geometry.setColors(new float[1], true);
        geometry.setColors(new float[1], true);
    }

    @Test
    public void testSetColorsFloatBuffer() throws Exception {
        // Create the dummy arrays
        final float[] colors = createVertexArray();
        final float[] colors2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(colors, null, null, colors, indices, true);
            }
        });
        bufferObject.setColors(FloatBuffer.wrap(colors2));
        final FloatBuffer buffer = bufferObject.getColors();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", colors2[i++], buffer.get(), 0);
        }
    }

    @Test
    public void testGetColors() throws Exception {
        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();
        assertNull(bufferObject.getColors());

        final float[] colors = createColorArray();
        bufferObject.setColors(colors);
        final FloatBuffer buffer = bufferObject.getColors();
        assertNotNull(buffer);
        for (int i = 0; i < colors.length; ++i) {
            assertEquals(colors[i], buffer.get(i), 1e-6);
        }
    }

    @Test
    public void getVertexBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final float[] indices = new float[] { 0, 1, 2 };
        geometry.setVertices(indices);

        final BufferInfo info = geometry.getVertexBufferInfo();
        assertNotNull(info);
        assertEquals(GLES20.GL_ARRAY_BUFFER, info.target);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.FLOAT_BUFFER, info.bufferType);
        assertTrue(info.rajawaliHandle >= 0);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((FloatBuffer) info.buffer).get(i), 1e-14);
        }
    }

    @Test
    public void setVertexBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        info.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setVertexBufferInfo(info);
        Mockito.verify(geometry).addBuffer(info);

        final BufferInfo infoNew = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        infoNew.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setVertexBufferInfo(infoNew);
        Mockito.verify(geometry).setBufferInfo(Mockito.anyInt(), Mockito.any(BufferInfo.class));
    }

    @Test
    public void getNormalBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final float[] indices = new float[] { 0, 1, 2 };
        geometry.setNormals(indices);

        final BufferInfo info = geometry.getNormalBufferInfo();
        assertNotNull(info);
        assertEquals(GLES20.GL_ARRAY_BUFFER, info.target);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.FLOAT_BUFFER, info.bufferType);
        assertTrue(info.rajawaliHandle >= 0);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((FloatBuffer) info.buffer).get(i), 1e-14);
        }
    }

    @Test
    public void setNormalBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        info.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setNormalBufferInfo(info);
        Mockito.verify(geometry).addBuffer(info);

        final BufferInfo infoNew = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        infoNew.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setNormalBufferInfo(infoNew);
        Mockito.verify(geometry).setBufferInfo(Mockito.anyInt(), Mockito.any(BufferInfo.class));
    }

    @Test
    public void getTexCoordBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final float[] indices = new float[] { 0, 1, 2 };
        geometry.setTextureCoords(indices);

        final BufferInfo info = geometry.getTexCoordBufferInfo();
        assertNotNull(info);
        assertEquals(GLES20.GL_ARRAY_BUFFER, info.target);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.FLOAT_BUFFER, info.bufferType);
        assertTrue(info.rajawaliHandle >= 0);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((FloatBuffer) info.buffer).get(i), 1e-14);
        }
    }

    @Test
    public void setTexCoordBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        info.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setTexCoordBufferInfo(info);
        Mockito.verify(geometry).addBuffer(info);

        final BufferInfo infoNew = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        infoNew.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setTexCoordBufferInfo(infoNew);
        Mockito.verify(geometry).setBufferInfo(Mockito.anyInt(), Mockito.any(BufferInfo.class));
    }

    @Test
    public void getColorBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());
        final float[] indices = new float[] { 0, 1, 2 };
        geometry.setColors(indices);

        final BufferInfo info = geometry.getColorBufferInfo();
        assertNotNull(info);
        assertEquals(GLES20.GL_ARRAY_BUFFER, info.target);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.FLOAT_BUFFER, info.bufferType);
        assertTrue(info.rajawaliHandle >= 0);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((FloatBuffer) info.buffer).get(i), 1e-14);
        }
    }

    @Test
    public void setColorBufferInfo() throws Exception {
        final NonInterleavedGeometry geometry = Mockito.spy(new NonInterleavedGeometry());

        final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        info.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setColorBufferInfo(info);
        Mockito.verify(geometry).addBuffer(info);

        final BufferInfo infoNew = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3).asFloatBuffer());
        infoNew.target = GLES20.GL_ARRAY_BUFFER;

        geometry.setColorBufferInfo(infoNew);
        Mockito.verify(geometry).setBufferInfo(Mockito.anyInt(), Mockito.any(BufferInfo.class));
    }


    @Test
    public void testToString() throws Exception {
        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();
        assertNotNull(bufferObject.toString());

        // Create the dummy arrays
        final float[] vertices = createVertexArray();
        final float[] normals = createNormalArray();
        final float[] colors = createColorArray();
        final float[] textures = createTextureArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final NonInterleavedGeometry bufferObject2 = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(vertices, normals, textures, colors, indices, true);
            }
        });
        assertNotNull(bufferObject2.toString());
    }
}
