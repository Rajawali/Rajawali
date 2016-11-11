package c.org.rajawali3d.textures;

import android.opengl.GLES20;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;

import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.textures.annotation.Wrap;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    public void pushTextureData() throws Exception {

    }

    @Test
    public void add() throws Exception {

    }

    @Test
    public void remove() throws Exception {

    }

    @Test
    public void replace() throws Exception {

    }
}