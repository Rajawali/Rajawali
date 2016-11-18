package c.org.rajawali3d.textures;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.opengl.GLES20;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;

import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CompressedTexture2DTest {

    private static class Testable extends CompressedTexture2D {

        public Testable() {
            super();
        }

        public Testable(int type, String name) throws TextureException {
            super(type, name);
        }

        public Testable(int type, String name, TextureDataReference data) throws TextureException {
            super(type, name, data);
        }

        public Testable(int type, String name, TextureDataReference[] data) throws TextureException {
            super(type, name, data);
        }

        public Testable(CompressedTexture2D other) throws TextureException {
            super(other);
        }

        @Override public BaseTexture clone() {
            return null;
        }
    }

    @Test
    public void constructorDefault() throws Exception {
        final CompressedTexture2D texture = new Testable();
        assertNotNull(texture);
    }

    @Test
    public void constructorTypeName() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        assertEquals(Type.DIFFUSE, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
    }

    @Test
    public void constructorTextureReference() throws Exception {
        final TextureDataReference reference = mock(TextureDataReference.class);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", reference);

        assertEquals(Type.DIFFUSE, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        assertNotNull(texture.getTextureData());
        assertEquals(reference, texture.getTextureData()[0]);
        verify(reference).holdReference();
    }

    @Test
    public void constructorTextureReferences() throws Exception {
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);

        assertEquals(Type.DIFFUSE, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i]).holdReference();
        }
    }

    @Test
    public void constructorOtherTexture() throws Exception {
        final CompressedTexture2D other = new Testable();

        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        other.setTextureData(references);

        final CompressedTexture2D texture = new Testable(other);

        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i], times(2)).holdReference();
        }
    }

    @Test
    public void setFrom() throws Exception {
        final CompressedTexture2D other = new Testable();

        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        // We have to set some data or the base class will throw an exception
        other.setTextureData(references);
        other.setCompressionType(Compression2D.ATC);

        final CompressedTexture2D texture = new Testable();
        texture.setFrom(other);

        assertNotNull(texture.getTextureData());
        assertEquals(Compression2D.ATC, texture.getCompressionType());
    }

    @Test
    public void setCompressionType() throws Exception {
        final CompressedTexture2D texture = new Testable();
        texture.setCompressionType(Compression2D.ATC);
        assertEquals(Compression2D.ATC, texture.getCompressionType());
    }

    @Test
    public void addFailNullData() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        boolean thrown = false;
        try {
            texture.add();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertTrue(texture.getTextureId() == -1);
    }

    @SuppressWarnings("Range")
    @Test
    public void addFailBadLengthData() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        final TextureDataReference reference = mock(TextureDataReference.class);
        texture.setTextureData(new TextureDataReference[]{ reference });
        boolean thrown = false;
        try {
            texture.add();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertTrue(texture.getTextureId() == -1);
    }

    @Test
    public void addFailZeroLimitBufferWithoutBitmap() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        final TextureDataReference reference = mock(TextureDataReference.class);
        doReturn(ByteBuffer.allocateDirect(0)).when(reference).getByteBuffer();
        doReturn(true).when(reference).hasBuffer();
        doReturn(false).when(reference).hasBitmap();
        texture.setTextureData(new TextureDataReference[]{ reference, reference, reference,
                                                           reference, reference, reference
        });
        boolean thrown = false;
        try {
            texture.add();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertTrue(texture.getTextureId() == -1);
    }

    @Test
    public void addFailZeroLimitBufferWithBitmap() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        final TextureDataReference reference = mock(TextureDataReference.class);
        doReturn(ByteBuffer.allocateDirect(0)).when(reference).getByteBuffer();
        doReturn(true).when(reference).hasBuffer();
        doReturn(true).when(reference).hasBitmap();
        texture.setTextureData(new TextureDataReference[]{ reference, reference, reference,
                                                           reference, reference, reference
        });
        boolean thrown = false;
        try {
            texture.add();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertTrue(texture.getTextureId() == -1);
    }

    @Test
    public void addFailNullReferences() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        texture.setTextureData(new TextureDataReference[]{ null, null, null, null, null, null });
        boolean thrown = false;
        try {
            texture.add();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertTrue(texture.getTextureId() == -1);
    }

    @Test
    public void addFailDestroyedData() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        final TextureDataReference reference = mock(TextureDataReference.class);
        doReturn(true).when(reference).isDestroyed();
        texture.setTextureData(new TextureDataReference[]{ reference, reference, reference,
                                                           reference, reference, reference
        });
        boolean thrown = false;
        try {
            texture.add();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
        assertTrue(texture.getTextureId() == -1);
    }

    @Test
    public void replaceNoData() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        boolean thrown = false;
        try {
            texture.replace();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void replaceDestroyed() throws Exception {
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        final TextureDataReference[] references = texture.setTextureDataFromResourceIds(getContext(), ids);
        for (TextureDataReference reference : references) {
            reference.recycle();
        }
        boolean thrown = false;
        try {
            texture.replace();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void replaceBufferFailZeroLimit() throws Exception {
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        final ByteBuffer buffer = ByteBuffer.allocateDirect(0);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
                                                                        GLES20.GL_UNSIGNED_BYTE, 256, 0);
        texture.setTextureData(new TextureDataReference[]{ reference, reference, reference,
                                                           reference, reference, reference
        });
        boolean thrown = false;
        try {
            texture.replace();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void replaceBufferFailZeroLimitWithBitmap() throws Exception {
        final TextureDataReference reference = mock(TextureDataReference.class);
        doReturn(ByteBuffer.allocateDirect(0)).when(reference).getByteBuffer();
        doReturn(true).when(reference).hasBuffer();
        doReturn(true).when(reference).hasBitmap();
        final TextureDataReference[] badReferences = new TextureDataReference[]{ reference, reference, reference,
                                                                                 reference, reference, reference
        };
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST");
        texture.setTextureData(badReferences);
        boolean thrown = false;
        try {
            texture.replace();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}