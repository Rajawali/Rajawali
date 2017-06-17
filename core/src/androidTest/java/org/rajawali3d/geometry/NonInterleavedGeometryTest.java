package org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.opengl.GLES20;
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

        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(vertices, normals, textures, colors, indices, true);
            }
        });

        final BufferInfo vertexInfo = bufferObject.getVertexBufferInfo();
        final BufferInfo normalInfo = bufferObject.getNormalBufferInfo();
        final BufferInfo textureInfo = bufferObject.getTexCoordBufferInfo();
        final BufferInfo colorInfo = bufferObject.getColorBufferInfo();
        final BufferInfo indexInfo = bufferObject.getIndexBufferInfo();
        final FloatBuffer vertexBuffer = (FloatBuffer) vertexInfo.buffer;
        final FloatBuffer normalBuffer = (FloatBuffer) normalInfo.buffer;
        final FloatBuffer colorBuffer = (FloatBuffer) colorInfo.buffer;
        final FloatBuffer textureBuffer = (FloatBuffer) textureInfo.buffer;
        final Buffer indexBuffer = indexInfo.buffer;
        assertEquals("Vertex buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, vertexInfo.bufferType);
        assertEquals("Normal buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, normalInfo.bufferType);
        assertEquals("Texture2D buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, textureInfo.bufferType);
        assertEquals("Color buffer info set to wrong type.", BufferInfo.FLOAT_BUFFER, colorInfo.bufferType);
        assertEquals("Index buffer info set to wrong type.", BufferInfo.BYTE_BUFFER, indexInfo.bufferType);
        assertEquals("VERTEX buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, vertexInfo.usage);
        assertEquals("Normal buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, normalInfo.usage);
        assertEquals("Texture2D buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, textureInfo.usage);
        assertEquals("Color buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, colorInfo.usage);
        assertEquals("Index buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, indexInfo.usage);

        assertEquals("Number of vertices invalid.", 1, bufferObject.getVertexCount());
        int i = 0;
        while (vertexBuffer.hasRemaining()) {
            assertEquals("VERTEX buffer contents invalid.", vertexBuffer.get(), vertices[i++], 0);
        }
        i = 0;
        while (normalBuffer.hasRemaining()) {
            assertEquals("Normal buffer contents invalid.", normalBuffer.get(), normals[i++], 0);
        }
        i = 0;
        while (textureBuffer.hasRemaining()) {
            assertEquals("Texture2D buffer contents invalid.", textureBuffer.get(), textures[i++], 0);
        }
        i = 0;
        while (colorBuffer.hasRemaining()) {
            assertEquals("Color buffer contents invalid.", colorBuffer.get(), colors[i++], 0);
        }
        i = 0;
        while (indexBuffer.hasRemaining()) {
            assertEquals("Index buffer contents invalid.", ((ByteBuffer) indexBuffer).get(), indices[i++], 0);
        }
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
    public void testSetVertices() throws Exception {

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

        final NonInterleavedGeometry geometry = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                geometry.setData(vertices, null, null, null, indices, true);
            }
        });
        geometry.setVertices(vertices2);
        final FloatBuffer buffer = geometry.getVertices();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer.get(), vertices2[i++], 0);
        }

        final NonInterleavedGeometry geometry1 = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                geometry1.setData(vertices, null, null, null, indices, false);
            }
        });
        geometry1.validateBuffers();
        geometry1.setVertices(vertices2);
        final FloatBuffer buffer2 = geometry1.getVertices();
        int i2 = 0;
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer2.get(), vertices2[i2++], 0);
        }
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
        final float[] normals = createNormalArray();
        final float[] normals2 = createVertexArray();
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
        bufferObject.setNormals(normals2);
        final FloatBuffer buffer = bufferObject.getNormals();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", normals2[i++], buffer.get(), 0);
        }

        final NonInterleavedGeometry bufferObject2 = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(normals, normals, null, null, indices, false);
            }
        });
        bufferObject2.validateBuffers();
        bufferObject2.setNormals(normals2);
        final FloatBuffer buffer2 = bufferObject2.getNormals();
        int i2 = 0;
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", normals2[i2++], buffer2.get(), 0);
        }
    }

    @Test
    public void testSetTextureCoords() throws Exception {
        // Create the dummy arrays
        final float[] textures = createTextureArray();
        final float[] textures2 = createVertexArray();
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
                bufferObject.setData(textures, null, textures, null, indices, true);
            }
        });
        bufferObject.setTextureCoords(textures2);
        final FloatBuffer buffer = bufferObject.getTextureCoords();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer.get(), textures2[i++], 0);
        }

        final NonInterleavedGeometry bufferObject2 = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(textures, null, textures, null, indices, false);
            }
        });
        bufferObject2.validateBuffers();
        bufferObject2.setTextureCoords(textures2);
        final FloatBuffer buffer2 = bufferObject2.getTextureCoords();
        int i2 = 0;
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer2.get(), textures2[i2++], 0);
        }
    }

    @Test
    public void testSetColors() throws Exception {
        // Create the dummy arrays
        final float[] colors = createColorArray();
        final float[] colors2 = createVertexArray();
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
        bufferObject.setColors(colors2);
        final FloatBuffer buffer = bufferObject.getColors();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer.get(), colors2[i++], 0);
        }

        final NonInterleavedGeometry bufferObject2 = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(colors, null, null, colors, indices, false);
            }
        });
        bufferObject2.validateBuffers();
        bufferObject2.setColors(colors2);
        final FloatBuffer buffer2 = bufferObject2.getColors();
        int i2 = 0;
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer2.get(), colors2[i2++], 0);
        }
    }

    @Test
    public void testSetIndices() throws Exception {
        // Create the dummy arrays
        final float[] vertices = createVertexArray();
        final int[] indices = new int[4];
        final int[] indices2 = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        // Fill arrays
        indices2[0] = 4;
        indices2[1] = 3;
        indices2[2] = 2;
        indices2[3] = 1;

        final NonInterleavedGeometry bufferObject = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(vertices, null, null, null, indices, true);
            }
        });
        bufferObject.setIndices(indices2);
        final Buffer buffer = bufferObject.getIndices();
        int i = 0;
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", ((ByteBuffer) buffer).get(), indices2[i++], 0);
        }

        final NonInterleavedGeometry bufferObject2 = new NonInterleavedGeometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(vertices, null, null, null, indices, false);
            }
        });
        bufferObject2.validateBuffers();
        bufferObject2.setIndices(indices2);
        final Buffer buffer2 = bufferObject.getIndices();
        i = 0;
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", ((ByteBuffer) buffer2).get(), indices2[i++], 0);
        }
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
