package c.org.rajawali3d.textures;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.opengl.GLES20;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.textures.annotation.Wrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;

import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class CubeMapTextureTest {

    @Test
    public void constructorOther() throws Exception {
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        final CubeMapTexture other = new CubeMapTexture("TEST", references);
        final CubeMapTexture texture = new CubeMapTexture(other);
        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        assertEquals(Wrap.CLAMP_S | Wrap.CLAMP_T, texture.getWrapType());
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP, texture.getTextureTarget());
        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i], times(2)).holdReference();
        }
    }

    @Test(expected = TextureException.class)
    public void constructorOtherAndThrow() throws Exception {
        final CubeMapTexture other = new CubeMapTexture("TEST");
        final CubeMapTexture texture = new CubeMapTexture(other);
    }

    @Test
    public void constructorName() throws Exception {
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals(Wrap.CLAMP_S | Wrap.CLAMP_T, texture.getWrapType());
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP, texture.getTextureTarget());
    }

    @Test
    public void constructorResourceIds() throws Exception {
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        assertEquals(Wrap.CLAMP_S | Wrap.CLAMP_T, texture.getWrapType());
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP, texture.getTextureTarget());
        final TextureDataReference[] data = texture.getTextureData();
        assertNotNull(data);
        assertEquals(6, data.length);
        for (int i = 0; i < 6; ++i) {
            assertNotNull(data[i]);
            assertTrue(data[i].hasBitmap());
        }
    }

    @Test
    public void constructorDataReferences() throws Exception {
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST", references);

        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        assertEquals(Wrap.CLAMP_S | Wrap.CLAMP_T, texture.getWrapType());
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP, texture.getTextureTarget());
        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i]).holdReference();
        }
    }

    @Test
    public void setIsSkyTexture() throws Exception {
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        texture.isSkyTexture(true);
        assertTrue(texture.isSkyTexture());
        texture.isSkyTexture(false);
        assertFalse(texture.isSkyTexture());
    }

    @Test
    public void testClone() throws Exception {
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        final CubeMapTexture other = new CubeMapTexture("TEST", references);
        final CubeMapTexture texture = other.clone();
        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        assertEquals(Wrap.CLAMP_S | Wrap.CLAMP_T, texture.getWrapType());
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP, texture.getTextureTarget());
        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i], times(2)).holdReference();
        }
    }

    @Test
    public void testCloneFail() throws Exception {
        final CubeMapTexture other = new CubeMapTexture("TEST");
        final CubeMapTexture texture = other.clone();
        assertNull(texture);
    }

    @Test
    public void textureAddFailNullData() throws Exception {
        final CubeMapTexture texture = new CubeMapTexture("TEST");
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
    public void textureAddFailBadLengthData() throws Exception {
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        final TextureDataReference reference = mock(TextureDataReference.class);
        texture.setTextureData(new TextureDataReference[]{reference});
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
    public void textureRemoveNotAdded() throws Exception {
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        texture.remove();
    }

    @Test
    public void replaceNoData() throws Exception {
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        boolean thrown = false;
        try {
            texture.replace();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void textureReplaceDestroyed() throws Exception {
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST");
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
    public void replaceBufferZeroLimit() throws Exception {
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        final ByteBuffer buffer = ByteBuffer.allocateDirect(0);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
                                                                        GLES20.GL_UNSIGNED_BYTE, 256, 0);
        texture.setTextureData(new TextureDataReference[]{reference, reference, reference,
                                                          reference, reference, reference});
        boolean thrown = false;
        try {
            texture.replace();
        } catch (TextureException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}