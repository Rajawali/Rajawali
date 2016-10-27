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
        when(from.isMipmaped()).thenReturn(false);
        when(from.willRecycle()).thenReturn(true);
        when(from.getTextureName()).thenReturn("TEST");
        when(from.getTextureType()).thenReturn(Type.LOOKUP);
        when(from.getWrapType()).thenReturn(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R);
        when(from.getFilterType()).thenReturn(Filter.NEAREST);
        when(from.getCompressedTexture()).thenReturn(null);
        when(from.getTextureTarget()).thenReturn(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        when(from.getInfluence()).thenReturn(0.654321f);
        when(from.getRegisteredMaterials()).thenReturn(new ArrayList<Material>());

        to.setFrom(from);
        assertEquals(1, to.getTextureId());
        assertEquals(256, to.getWidth());
        assertEquals(512, to.getHeight());
        assertEquals(GLES20.GL_RGBA, to.getTexelFormat());
        assertEquals(false, to.isMipmaped());
        assertEquals(true, to.willRecycle());
        assertEquals("TEST", to.getTextureName());
        assertEquals(Type.LOOKUP, to.getTextureType());
        assertEquals((Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R), to.getWrapType());
        assertEquals(Filter.NEAREST, to.getFilterType());
        assertEquals(null, to.getCompressedTexture());
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, to.getTextureTarget());
        assertEquals(0.654321f, to.getInfluence());
        assertNotNull(to.getRegisteredMaterials());
        assertEquals(0, to.getRegisteredMaterials().size());
    }

    @Test
    public void setTextureId() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setTextureId(10);
        assertEquals(10, texture.getTextureId());
    }

    @Test
    public void setWidth() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setWidth(10);
        assertEquals(10, texture.getWidth());
    }

    @Test
    public void setHeight() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setHeight(10);
        assertEquals(10, texture.getHeight());
    }

    @Test
    public void setTexelFormat() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setTexelFormat(GLES20.GL_RGB565);
        assertEquals(GLES20.GL_RGB565, texture.getTexelFormat());
    }

    @Test
    public void setMipmap() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setMipmaped(true);
        assertEquals(true, texture.isMipmaped());
    }

    @Test
    public void shouldRecycle() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.shouldRecycle(true);
        assertEquals(true, texture.willRecycle());
    }

    @Test
    public void setTextureName() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setTextureName("TEST");
        assertEquals("TEST", texture.getTextureName());
    }

    @Test
    public void setWrapType() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setWrapType(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R);
        assertEquals(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R, texture.getWrapType());
    }

    @Test
    public void setFilterType() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setFilterType(Filter.ANISOTROPIC);
        assertEquals(Filter.ANISOTROPIC, texture.getFilterType());
    }

    @Test
    public void setTextureTarget() throws Exception {
        final TestableATexture texture = new TestableATexture();
        texture.setTextureTarget(GLES20.GL_TEXTURE_CUBE_MAP);
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP, texture.getTextureTarget());
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