package c.org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.opengl.GLES20;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.geometry.Geometry.BufferType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class GeometryTest extends GlTestCase {

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

    public GeometryTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testToString() throws Exception {
        final Geometry bufferObject = new Geometry();
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

        final Geometry bufferObject2 = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(vertices, normals, textures, colors, indices, true);
            }
        });
        assertNotNull(bufferObject2.toString());
    }

    @Test
    public void testBufferInfoNoArgsConstructor() {
        final BufferInfo floatInfo = new BufferInfo();
        assertEquals("Buffer info usage set to wrong type.", GLES20.GL_STATIC_DRAW, floatInfo.usage);
    }

    @Test
    public void testBufferInfoConstructor() {
        final FloatBuffer floatBuffer = FloatBuffer.allocate(4);
        final BufferInfo floatInfo = new BufferInfo(BufferType.FLOAT_BUFFER, floatBuffer);
        assertEquals("Float buffer info pointing to wrong buffer.", floatBuffer, floatInfo.buffer);
        assertEquals("Float buffer info set to wrong type.", BufferType.FLOAT_BUFFER, floatInfo.bufferType);

        final IntBuffer intBuffer = IntBuffer.allocate(4);
        final BufferInfo intInfo = new BufferInfo(BufferType.INT_BUFFER, intBuffer);
        assertEquals("Int buffer info pointing to wrong buffer.", intBuffer, intInfo.buffer);
        assertEquals("Int buffer info set to wrong type.", BufferType.INT_BUFFER, intInfo.bufferType);

        final ShortBuffer shortBuffer = ShortBuffer.allocate(4);
        final BufferInfo shortInfo = new BufferInfo(BufferType.SHORT_BUFFER, shortBuffer);
        assertEquals("Short buffer info pointing to wrong buffer.", shortBuffer, shortInfo.buffer);
        assertEquals("Short buffer info set to wrong type.", BufferType.SHORT_BUFFER, shortInfo.bufferType);

        final ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        final BufferInfo byteInfo = new BufferInfo(BufferType.BYTE_BUFFER, byteBuffer);
        assertEquals("Byte buffer info pointing to wrong buffer.", byteBuffer, byteInfo.buffer);
        assertEquals("Byte buffer info set to wrong type.", BufferType.BYTE_BUFFER, byteInfo.bufferType);
    }

    @Test
    public void testBufferInfoToString() {
        final BufferInfo floatInfo = new BufferInfo();
        assertNotNull(floatInfo.toString());
    }

    @Test
    public void testBufferTypeValues() {
        final BufferType[] values = BufferType.values();
        assertNotNull(values);
        assertTrue(values.length == 4);
    }

    @Test
    public void testBufferTypeValueOf() {
        final BufferType floatType = BufferType.valueOf("FLOAT_BUFFER");
        final BufferType intType = BufferType.valueOf("INT_BUFFER");
        final BufferType shortType = BufferType.valueOf("SHORT_BUFFER");
        final BufferType byteType = BufferType.valueOf("BYTE_BUFFER");
        assertNotNull(floatType);
        assertNotNull(intType);
        assertNotNull(shortType);
        assertNotNull(byteType);
        assertEquals(BufferType.FLOAT_BUFFER, floatType);
        assertEquals(BufferType.INT_BUFFER, intType);
        assertEquals(BufferType.SHORT_BUFFER, shortType);
        assertEquals(BufferType.BYTE_BUFFER, byteType);
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

        final Geometry bufferObject = new Geometry();

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
        assertEquals("Vertex buffer info set to wrong type.", BufferType.FLOAT_BUFFER, vertexInfo.bufferType);
        assertEquals("Normal buffer info set to wrong type.", BufferType.FLOAT_BUFFER, normalInfo.bufferType);
        assertEquals("Texture buffer info set to wrong type.", BufferType.FLOAT_BUFFER, textureInfo.bufferType);
        assertEquals("Color buffer info set to wrong type.", BufferType.FLOAT_BUFFER, colorInfo.bufferType);
        assertEquals("Index buffer info set to wrong type.", BufferType.INT_BUFFER, indexInfo.bufferType);
        assertEquals("Vertex buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, vertexInfo.usage);
        assertEquals("Normal buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, normalInfo.usage);
        assertEquals("Texture buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, textureInfo.usage);
        assertEquals("Color buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, colorInfo.usage);
        assertEquals("Index buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, indexInfo.usage);

        assertEquals("Number of vertices invalid.", 1, bufferObject.getNumVertices());
        int i = 0;
        vertexBuffer.rewind();
        while (vertexBuffer.hasRemaining()) {
            assertEquals("Vertex buffer contents invalid.", vertexBuffer.get(), vertices[i++], 0);
        }
        i = 0;
        normalBuffer.rewind();
        while (normalBuffer.hasRemaining()) {
            assertEquals("Normal buffer contents invalid.", normalBuffer.get(), normals[i++], 0);
        }
        i = 0;
        textureBuffer.rewind();
        while (textureBuffer.hasRemaining()) {
            assertEquals("Texture buffer contents invalid.", textureBuffer.get(), textures[i++], 0);
        }
        i = 0;
        colorBuffer.rewind();
        while (colorBuffer.hasRemaining()) {
            assertEquals("Color buffer contents invalid.", colorBuffer.get(), colors[i++], 0);
        }
        i = 0;
        indexBuffer.rewind();
        while (indexBuffer.hasRemaining()) {
            assertEquals("Index buffer contents invalid.", ((IntBuffer) indexBuffer).get(), indices[i++], 0);
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

        final Geometry bufferObject = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(vertices, GLES20.GL_STATIC_DRAW, normals, GLES20.GL_STREAM_DRAW,
                                     textures, GLES20.GL_DYNAMIC_DRAW, colors, GLES20.GL_STATIC_DRAW,
                                     indices, GLES20.GL_STREAM_DRAW, true);
            }
        });

        final BufferInfo vertexInfo = bufferObject.getVertexBufferInfo();
        final BufferInfo normalInfo = bufferObject.getNormalBufferInfo();
        final BufferInfo colorInfo = bufferObject.getColorBufferInfo();
        final BufferInfo textureInfo = bufferObject.getTexCoordBufferInfo();
        final BufferInfo indexInfo = bufferObject.getIndexBufferInfo();
        final FloatBuffer vertexBuffer = (FloatBuffer) vertexInfo.buffer;
        final FloatBuffer normalBuffer = (FloatBuffer) normalInfo.buffer;
        final FloatBuffer colorBuffer = (FloatBuffer) colorInfo.buffer;
        final FloatBuffer textureBuffer = (FloatBuffer) textureInfo.buffer;
        final Buffer indexBuffer = indexInfo.buffer;
        assertEquals("Vertex buffer info set to wrong type.", BufferType.FLOAT_BUFFER, vertexInfo.bufferType);
        assertEquals("Normal buffer info set to wrong type.", BufferType.FLOAT_BUFFER, normalInfo.bufferType);
        assertEquals("Color buffer info set to wrong type.", BufferType.FLOAT_BUFFER, colorInfo.bufferType);
        assertEquals("Texture buffer info set to wrong type.", BufferType.FLOAT_BUFFER, textureInfo.bufferType);
        assertEquals("Index buffer info set to wrong type.", BufferType.INT_BUFFER, indexInfo.bufferType);
        assertEquals("Vertex buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, vertexInfo.usage);
        assertEquals("Normal buffer info set to wrong usage.", GLES20.GL_STREAM_DRAW, normalInfo.usage);
        assertEquals("Color buffer info set to wrong usage.", GLES20.GL_STATIC_DRAW, colorInfo.usage);
        assertEquals("Texture buffer info set to wrong usage.", GLES20.GL_DYNAMIC_DRAW, textureInfo.usage);
        assertEquals("Index buffer info set to wrong usage.", GLES20.GL_STREAM_DRAW, indexInfo.usage);

        assertEquals("Number of vertices invalid.", 1, bufferObject.getNumVertices());
        int i = 0;
        vertexBuffer.rewind();
        while (vertexBuffer.hasRemaining()) {
            assertEquals("Vertex buffer contents invalid.", vertexBuffer.get(), vertices[i++], 0);
        }
        i = 0;
        normalBuffer.rewind();
        while (normalBuffer.hasRemaining()) {
            assertEquals("Normal buffer contents invalid.", normalBuffer.get(), normals[i++], 0);
        }
        i = 0;
        colorBuffer.rewind();
        while (colorBuffer.hasRemaining()) {
            assertEquals("Color buffer contents invalid.", colorBuffer.get(), colors[i++], 0);
        }
        i = 0;
        textureBuffer.rewind();
        while (textureBuffer.hasRemaining()) {
            assertEquals("Texture buffer contents invalid.", textureBuffer.get(), textures[i++], 0);
        }
        i = 0;
        indexBuffer.rewind();
        while (indexBuffer.hasRemaining()) {
            assertEquals("Index buffer contents invalid.",  ((IntBuffer) indexBuffer).get(), indices[i++], 0);
        }
    }

    @Test
    public void testSetVertices() throws Exception {
        // Create the dummy arrays
        final float[] vertices = createVertexArray();
        final float[] vertices2 = createNormalArray();
        final int[] indices = new int[4];

        // Fill arrays
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 3;
        indices[3] = 4;

        final Geometry bufferObject = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(vertices, null, null, null, indices, true);
            }
        });
        bufferObject.setVertices(vertices2);
        final FloatBuffer buffer = bufferObject.getVertices();
        int i = 0;
        buffer.rewind();
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer.get(), vertices2[i++], 0);
        }

        final Geometry bufferObject2 = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(vertices, null, null, null, indices, false);
            }
        });
        bufferObject2.validateBuffers();
        bufferObject2.setVertices(vertices2);
        final FloatBuffer buffer2 = bufferObject2.getVertices();
        int i2 = 0;
        buffer2.rewind();
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer2.get(), vertices2[i2++], 0);
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

        final Geometry bufferObject = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(normals, normals, null, null, indices, true);
            }
        });
        bufferObject.setNormals(normals2);
        final FloatBuffer buffer = bufferObject.getNormals();
        int i = 0;
        buffer.rewind();
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer.get(), normals2[i++], 0);
        }

        final Geometry bufferObject2 = new Geometry();

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
        buffer2.rewind();
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer2.get(), normals2[i2++], 0);
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

        final Geometry bufferObject = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(textures, null, textures, null, indices, true);
            }
        });
        bufferObject.setTextureCoords(textures2);
        final FloatBuffer buffer = bufferObject.getTextureCoords();
        int i = 0;
        buffer.rewind();
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer.get(), textures2[i++], 0);
        }

        final Geometry bufferObject2 = new Geometry();

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
        buffer2.rewind();
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

        final Geometry bufferObject = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(colors, null, null, colors, indices, true);
            }
        });
        bufferObject.setColors(colors2);
        final FloatBuffer buffer = bufferObject.getColors();
        int i = 0;
        buffer.rewind();
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", buffer.get(), colors2[i++], 0);
        }

        final Geometry bufferObject2 = new Geometry();

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
        buffer2.rewind();
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

        final Geometry bufferObject = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(vertices, null, null, null, indices, true);
            }
        });
        bufferObject.setIndices(indices2);
        final Buffer buffer = bufferObject.getIndices();
        int i = 0;
        buffer.rewind();
        while (buffer.hasRemaining()) {
            assertEquals("Buffer contents invalid.", ((IntBuffer) buffer).get(), indices2[i++], 0);
        }

        final Geometry bufferObject2 = new Geometry();

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
        buffer2.rewind();
        while (buffer2.hasRemaining()) {
            assertEquals("Buffer contents invalid.", ((IntBuffer) buffer2).get(), indices2[i++], 0);
        }
    }

    @Test
    public void testValidateBuffers() throws Exception {
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

        final Geometry bufferObject = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject.setData(vertices, normals, textures, colors, indices, false);
            }
        });

        bufferObject.validateBuffers();

        final Geometry bufferObject2 = new Geometry();

        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                bufferObject2.setData(vertices, normals, textures, colors, indices, true);
            }
        });

        bufferObject2.validateBuffers();
    }
}
