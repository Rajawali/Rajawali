package org.rajawali3d.textures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.opengl.GLES20;
import org.junit.Test;
import org.rajawali3d.materials.Material;
import org.rajawali3d.textures.annotation.Filter;
import org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.textures.annotation.Wrap;

import java.util.ArrayList;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ATextureTest {

    private static final class TestableATexture extends ATexture {

        @Override public ATexture clone() {
            return null;
        }

        @Override void add() throws TextureException {

        }

        @Override void remove() throws TextureException {

        }

        @Override void replace() throws TextureException {

        }

        @Override void reset() throws TextureException {

        }
    }

    @Test
    public void setFrom() throws Exception {
        final ATexture from = mock(ATexture.class);
        final ATexture to = new TestableATexture();
        when(from.getTextureId()).thenReturn(1);
        when(from.getWidth()).thenReturn(256);
        when(from.getHeight()).thenReturn(512);
        when(from.getTexelFormat()).thenReturn(GLES20.GL_RGBA);
        when(from.isMipmap()).thenReturn(false);
        when(from.willRecycle()).thenReturn(true);
        when(from.getTextureName()).thenReturn("TEST");
        when(from.getTextureType()).thenReturn(Type.LOOKUP);
        when(from.getWrapType()).thenReturn(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R);
        when(from.getFilterType()).thenReturn(Filter.NEAREST);
        when(from.getCompressedTexture()).thenReturn(null);
        when(from.getTextureTarget()).thenReturn(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        when(from.getRegisteredMaterials()).thenReturn(new ArrayList<Material>());

        to.setFrom(from);
        assertEquals(1, to.getTextureId());
        assertEquals(256, to.getWidth());
        assertEquals(512, to.getHeight());
        assertEquals(GLES20.GL_RGBA, to.getTexelFormat());
        assertEquals(false, to.isMipmap());
        assertEquals(true, to.willRecycle());
        assertEquals("TEST", to.getTextureName());
        assertEquals(Type.LOOKUP, to.getTextureType());
        assertEquals((Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R), to.getWrapType());
        assertEquals(Filter.NEAREST, to.getFilterType());
        assertEquals(null, to.getCompressedTexture());
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, to.getTextureTarget());
        assertNotNull(to.getRegisteredMaterials());
        assertEquals(0, to.getRegisteredMaterials().size());
    }

    @Test
    public void getTextureId() throws Exception {

    }

    @Test
    public void setTextureId() throws Exception {

    }

    @Test
    public void getWidth() throws Exception {

    }

    @Test
    public void setWidth() throws Exception {

    }

    @Test
    public void getHeight() throws Exception {

    }

    @Test
    public void setHeight() throws Exception {

    }

    @Test
    public void getTexelFormat() throws Exception {

    }

    @Test
    public void setTexelFormat() throws Exception {

    }

    @Test
    public void isMipmap() throws Exception {

    }

    @Test
    public void setMipmap() throws Exception {

    }

    @Test
    public void willRecycle() throws Exception {

    }

    @Test
    public void shouldRecycle() throws Exception {

    }

    @Test
    public void getTextureName() throws Exception {

    }

    @Test
    public void setTextureName() throws Exception {

    }

    @Test
    public void getTextureType() throws Exception {

    }

    @Test
    public void getWrapType() throws Exception {

    }

    @Test
    public void setWrapType() throws Exception {

    }

    @Test
    public void getFilterType() throws Exception {

    }

    @Test
    public void setFilterType() throws Exception {

    }

    @Test
    public void getTextureTarget() throws Exception {

    }

    @Test
    public void setTextureTarget() throws Exception {

    }

    @Test
    public void registerMaterial() throws Exception {

    }

    @Test
    public void unregisterMaterial() throws Exception {

    }

    @Test
    public void setInfluence() throws Exception {

    }

    @Test
    public void getInfluence() throws Exception {

    }

}