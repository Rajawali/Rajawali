package org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
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
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class IndexedGeometryTest extends GlTestCase {

    public class TestableIndexedGeometry extends IndexedGeometry {

        @Override protected int getVertexCount() {
            return 0;
        }

        @Override protected boolean hasVertexData() {
            return false;
        }

        @Nullable @Override protected BufferInfo getVertexBufferInfo() {
            return null;
        }

        @Override public void calculateAABounds(@NonNull Vector3 min, @NonNull Vector3 max) {

        }

        @Override public void issueDrawCalls() {

        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp(IndexedGeometryTest.class.getSimpleName());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @SuppressWarnings("Range")
    @Test
    public void copyFrom() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        final IndexedGeometry newGeometry1 = Mockito.spy(new TestableIndexedGeometry());
        newGeometry1.copyFrom(geometry);
        assertEquals(0, newGeometry1.getNumberIndices());

        final BufferInfo info = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(3));
        info.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
        geometry.setIndexBufferInfo(info);
        Mockito.doReturn(3).when(geometry).getNumberIndices();
        newGeometry1.copyFrom(geometry);
        Mockito.verify(newGeometry1).addBuffer(info);
        assertEquals(3, newGeometry1.getNumberIndices());

        newGeometry1.addBuffer(info);
        final BufferInfo info2 = new BufferInfo(BufferInfo.FLOAT_BUFFER, ByteBuffer.allocate(6));
        info2.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
        geometry.setIndexBufferInfo(info2);
        Mockito.doReturn(6).when(geometry).getNumberIndices();
        newGeometry1.copyFrom(geometry);
        Mockito.verify(newGeometry1).setBufferInfo(Mockito.anyInt(), Mockito.any(BufferInfo.class));
        assertEquals(6, newGeometry1.getNumberIndices());
    }

    @Test
    public void isValid() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());

        assertFalse(geometry.isValid());

        Mockito.when(geometry.hasVertexData()).thenReturn(true);
        assertFalse(geometry.isValid());
        Mockito.when(geometry.hasVertexData()).thenReturn(false);

        final BufferInfo vertexInfo = new BufferInfo(BufferInfo.FLOAT_BUFFER, FloatBuffer.allocate(9));
        vertexInfo.target = GLES20.GL_ARRAY_BUFFER;
        final BufferInfo indexInfo = new BufferInfo(BufferInfo.BYTE_BUFFER, ByteBuffer.allocate(3));
        indexInfo.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
        geometry.addBuffer(vertexInfo);
        geometry.setIndexBufferInfo(indexInfo);
        assertFalse(geometry.isValid());

        Mockito.when(geometry.getVertexBufferInfo()).thenReturn(vertexInfo);
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        assertFalse(geometry.isValid());

        final boolean result[] = new boolean[] { false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                geometry.createBuffers();
                result[0] = geometry.isValid();
            }
        });
        assertTrue(result[0]);

        result[0] = true;
        final int oldIndexHandle = indexInfo.glHandle;
        indexInfo.glHandle *= Integer.MAX_VALUE;
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                result[0] = geometry.isValid();
            }
        });
        assertFalse(result[0]);

        result[0] = true;
        indexInfo.glHandle = oldIndexHandle;
        vertexInfo.glHandle *= Integer.MAX_VALUE;
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                result[0] = geometry.isValid();
            }
        });
        assertFalse(result[0]);
    }

    @Test
    public void getTriangleCount() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());

        Mockito.when(geometry.hasVertexData()).thenReturn(true);
        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIndices(indices);

        assertEquals(1, geometry.getTriangleCount());
    }

    @Test
    public void setIndices() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.doNothing().when(geometry).setIndices(Mockito.any(int[].class), Mockito.anyBoolean());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        // Test byte indices
        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIndices(indices);
        Mockito.verify(geometry).setIndices(indices, false);
    }

    @Test(expected = IllegalStateException.class)
    public void setIndicesNoVertexData() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIndices(indices, false);
    }

    @Test
    public void setIndicesReplace() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        // Test byte indices
        final int[] byteIndices = new int[] { 0, 1, 2 };
        Mockito.when(geometry.getVertexCount()).thenReturn(3);
        geometry.setIndices(byteIndices, true);
        final BufferInfo byteInfo = geometry.getIndexBufferInfo();
        assertEquals(3, geometry.getNumberIndices());
        assertNotNull(byteInfo);
        assertNotNull(byteInfo.buffer);
        assertEquals(BufferInfo.BYTE_BUFFER, byteInfo.bufferType);

        // Test short indices
        final int[] shortIndices = new int[257];
        for (int i = 0; i < shortIndices.length; ++i) {
            shortIndices[i] = i;
        }
        Mockito.when(geometry.getVertexCount()).thenReturn(257);
        geometry.setIndices(shortIndices, true);
        final BufferInfo shortInfo = geometry.getIndexBufferInfo();
        assertEquals(257, geometry.getNumberIndices());
        assertNotNull(shortInfo);
        assertNotNull(shortInfo.buffer);
        assertEquals(BufferInfo.SHORT_BUFFER, shortInfo.bufferType);

        // Test short indices
        final int[] intIndices = new int[65537];
        for (int i = 0; i < intIndices.length; ++i) {
            intIndices[i] = i;
        }
        Mockito.when(geometry.getVertexCount()).thenReturn(65537);
        geometry.setIndices(intIndices, true);
        final BufferInfo intInfo = geometry.getIndexBufferInfo();
        assertEquals(65537, geometry.getNumberIndices());
        assertNotNull(intInfo);
        assertNotNull(intInfo.buffer);
        assertEquals(BufferInfo.INT_BUFFER, intInfo.bufferType);
    }

    @Test
    public void getIndices() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());

        // After construction, indices should return null
        assertNull(geometry.getIndices());

        Mockito.when(geometry.hasVertexData()).thenReturn(true);
        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIndices(indices);

        final Buffer buffer = geometry.getIndices();
        assertNotNull(buffer);
        assertEquals(3, buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((ByteBuffer) buffer).get(i));
        }
    }

    @Test
    public void getNumberIndices() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);
        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIndices(indices);

        assertEquals(3, geometry.getNumberIndices());
    }

    @Test
    public void getIndexBufferInfo() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);
        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIndices(indices);

        final BufferInfo info = geometry.getIndexBufferInfo();
        assertNotNull(info);
        assertEquals(GLES20.GL_ELEMENT_ARRAY_BUFFER, info.target);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.BYTE_BUFFER, info.bufferType);
        assertTrue(info.rajawaliHandle >= 0);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((ByteBuffer) info.buffer).get(i));
        }
    }

    @Test
    public void setIndexBufferInfo() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final BufferInfo info = new BufferInfo(BufferInfo.BYTE_BUFFER, ByteBuffer.allocate(3));
        info.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;

        geometry.setIndexBufferInfo(info);
        Mockito.verify(geometry).addBuffer(info);

        final BufferInfo infoNew = new BufferInfo(BufferInfo.BYTE_BUFFER, ByteBuffer.allocate(3));
        infoNew.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;

        geometry.setIndexBufferInfo(infoNew);
        Mockito.verify(geometry).setBufferInfo(Mockito.anyInt(), Mockito.any(BufferInfo.class));
    }

    @Test
    public void setByteIndicesNoInfo() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        geometry.setByteIndices(new int[3], true);
        Mockito.verify(geometry).addBuffer(Mockito.any(BufferInfo.class));
    }

    @Test
    public void setByteIndicesReplaceExisting() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setByteIndices(indices, true);
        final BufferInfo info = geometry.getIndexBufferInfo();
        assertNotNull(info);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.BYTE_BUFFER, info.bufferType);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((ByteBuffer) info.buffer).get(i));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void setByteIndicesReplaceNullBuffer() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final BufferInfo info = new BufferInfo(BufferInfo.BYTE_BUFFER, null);
        info.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
        geometry.setIndexBufferInfo(info);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setByteIndices(indices, false);
        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.BYTE_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((ByteBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setByteIndicesReplaceExistingData() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setByteIndices(indices, false);

        final int[] newIndices = new int[] { 2, 1, 0 };
        geometry.setByteIndices(newIndices, false);

        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.BYTE_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(newIndices[i], ((ByteBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setByteIndicesDifferentType() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIntIndices(indices, false);

        final int[] newIndices = new int[] { 2, 1, 0 };
        geometry.setByteIndices(newIndices, false);

        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.BYTE_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(newIndices[i], ((ByteBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setShortIndicesNoInfo() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        geometry.setShortIndices(new int[3], true);
        Mockito.verify(geometry).addBuffer(Mockito.any(BufferInfo.class));
    }

    @Test
    public void setShortIndicesReplaceExisting() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setShortIndices(indices, true);
        final BufferInfo info = geometry.getIndexBufferInfo();
        assertNotNull(info);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.SHORT_BUFFER, info.bufferType);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((ShortBuffer) info.buffer).get(i));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void setShortIndicesReplaceNullBuffer() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final BufferInfo info = new BufferInfo(BufferInfo.SHORT_BUFFER, null);
        info.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
        geometry.setIndexBufferInfo(info);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setShortIndices(indices, false);
        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.SHORT_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((ShortBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setShortIndicesReplaceExistingData() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setShortIndices(indices, false);

        final int[] newIndices = new int[] { 2, 1, 0 };
        geometry.setShortIndices(newIndices, false);

        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.SHORT_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(newIndices[i], ((ShortBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setShortIndicesDifferentType() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIntIndices(indices, false);

        final int[] newIndices = new int[] { 2, 1, 0 };
        geometry.setShortIndices(newIndices, false);

        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.SHORT_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(newIndices[i], ((ShortBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setIntIndicesNoInfo() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        geometry.setIntIndices(new int[3], true);
        Mockito.verify(geometry).addBuffer(Mockito.any(BufferInfo.class));
    }

    @Test
    public void setIntIndicesReplaceExisting() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIntIndices(indices, true);
        final BufferInfo info = geometry.getIndexBufferInfo();
        assertNotNull(info);
        assertNotNull(info.buffer);
        assertEquals(BufferInfo.INT_BUFFER, info.bufferType);
        assertEquals(3, info.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((IntBuffer) info.buffer).get(i));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void setIntIndicesReplaceNullBuffer() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final BufferInfo info = new BufferInfo(BufferInfo.INT_BUFFER, null);
        info.target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
        geometry.setIndexBufferInfo(info);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIntIndices(indices, false);
        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.INT_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(indices[i], ((IntBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setIntIndicesReplaceExistingData() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setIntIndices(indices, false);

        final int[] newIndices = new int[] { 2, 1, 0 };
        geometry.setIntIndices(newIndices, false);

        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.INT_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(newIndices[i], ((IntBuffer) result.buffer).get(i));
        }
    }

    @Test
    public void setIntIndicesDifferentType() throws Exception {
        final IndexedGeometry geometry = Mockito.spy(new TestableIndexedGeometry());
        Mockito.when(geometry.hasVertexData()).thenReturn(true);

        final int[] indices = new int[] { 0, 1, 2 };
        geometry.setShortIndices(indices, false);

        final int[] newIndices = new int[] { 2, 1, 0 };
        geometry.setIntIndices(newIndices, false);

        final BufferInfo result = geometry.getIndexBufferInfo();
        assertNotNull(result);
        assertNotNull(result.buffer);
        assertEquals(BufferInfo.INT_BUFFER, result.bufferType);
        assertEquals(3, result.buffer.capacity());
        for (int i = 0; i < 3; ++i) {
            assertEquals(newIndices[i], ((IntBuffer) result.buffer).get(i));
        }
    }
}