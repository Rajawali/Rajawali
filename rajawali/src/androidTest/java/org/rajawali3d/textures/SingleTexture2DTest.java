package org.rajawali3d.textures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.opengl.GLES20;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.materials.Material;
import org.rajawali3d.textures.annotation.Filter;
import org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.textures.annotation.Wrap;

import java.util.ArrayList;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class SingleTexture2DTest extends GlTestCase {

    private static class TestableSingleTexture2D extends SingleTexture2D {

        @Override public SingleTexture2D clone() {
            return null;
        }
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
        
    }

    @Test
    public void setTextureData() throws Exception {

    }

    @Test
    public void getTextureData() throws Exception {

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

    @Test
    public void reset() throws Exception {

    }

}