package org.rajawali3d.textures;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.opengl.GLES20;
import android.support.test.filters.SmallTest;
import org.junit.Test;
import org.rajawali3d.R;
import org.rajawali3d.materials.Material;
import org.rajawali3d.textures.annotation.Filter;
import org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.textures.annotation.Wrap;

import java.util.ArrayList;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class SingleTexture2DTest {

    private static class TestableSingleTexture2D extends SingleTexture2D {

        @Override public SingleTexture2D clone() {
            return null;
        }
    }

    @Test
    public void setFrom() throws Exception {
        final SingleTexture2D from = mock(SingleTexture2D.class);
        final SingleTexture2D to = new TestableSingleTexture2D();
        final TextureDataReference data = mock(TextureDataReference.class);
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
        when(from.getTextureTarget()).thenReturn(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        when(from.getInfluence()).thenReturn(0.654321f);
        when(from.getRegisteredMaterials()).thenReturn(new ArrayList<Material>());
        when(from.getTextureData()).thenReturn(data);

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
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, to.getTextureTarget());
        assertEquals(Double.doubleToLongBits(0.654321f), Double.doubleToLongBits(to.getInfluence()));
        assertNotNull(to.getRegisteredMaterials());
        assertEquals(0, to.getRegisteredMaterials().size());
        verify(data).holdReference();
    }

    @Test(expected = TextureException.class)
    public void setFromWithNoData() throws Exception {
        final SingleTexture2D from = mock(SingleTexture2D.class);
        final SingleTexture2D to = new TestableSingleTexture2D();
        when(from.getTextureData()).thenReturn(null);

        to.setFrom(from);
    }

    @Test
    public void setTextureDataFromResourceId() throws Exception {
        final SingleTexture2D texture = new TestableSingleTexture2D();
        final TextureDataReference reference = texture.setTextureDataFromResourceId(getContext(), R.drawable
                .earth_diffuse);
        assertNotNull(reference);
        assertEquals(reference, texture.getTextureData());
        assertTrue(reference.hasBitmap());
        assertFalse(reference.hasBuffer());
        assertEquals(GLES20.GL_RGBA, reference.getPixelFormat());
        assertEquals(GLES20.GL_UNSIGNED_BYTE, reference.getDataType());
        reference.recycle();
    }

    @Test
    public void setTextureData() throws Exception {
        final TextureDataReference reference = mock(TextureDataReference.class);
        final TextureDataReference newReference = mock(TextureDataReference.class);

        final SingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureData(reference);
        verify(reference).holdReference();
        texture.setTextureData(newReference);
        verify(reference).recycle();
        verify(newReference).holdReference();
    }
}
