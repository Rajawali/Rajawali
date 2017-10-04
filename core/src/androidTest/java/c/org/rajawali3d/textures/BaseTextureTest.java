package c.org.rajawali3d.textures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.opengl.GLES20;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.textures.annotation.Wrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.materials.Material;

import java.util.ArrayList;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class BaseTextureTest {

    private static final class TestableBaseTexture extends BaseTexture {

        @Override public BaseTexture clone() {
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
        final BaseTexture from = mock(BaseTexture.class);
        final BaseTexture to = new TestableBaseTexture();
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
    }

    @Test
    public void setTextureId() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setTextureId(10);
        assertEquals(10, texture.getTextureId());
    }

    @Test
    public void setWidth() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setWidth(10);
        assertEquals(10, texture.getWidth());
    }

    @Test
    public void setHeight() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setHeight(10);
        assertEquals(10, texture.getHeight());
    }

    @Test
    public void setTexelFormat() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setTexelFormat(GLES20.GL_RGB565);
        assertEquals(GLES20.GL_RGB565, texture.getTexelFormat());
    }

    @Test
    public void setMipmap() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setMipmaped(true);
        assertEquals(true, texture.isMipmaped());
    }

    @Test
    public void shouldRecycle() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.willRecycle(true);
        assertEquals(true, texture.willRecycle());
    }

    @Test
    public void setTextureName() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setTextureName("TEST");
        assertEquals("TEST", texture.getTextureName());
    }

    @Test
    public void setTextureType() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setTextureType(Type.NORMAL);
        assertEquals(Type.NORMAL, texture.getTextureType());
    }

    @Test
    public void setWrapType() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setWrapType(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R);
        assertEquals(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T | Wrap.REPEAT_R, texture.getWrapType());
    }

    @Test
    public void setFilterType() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setFilterType(Filter.TRILINEAR);
        assertEquals(Filter.TRILINEAR, texture.getFilterType());
    }

    @Test
    public void setTextureTarget() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setTextureTarget(GLES20.GL_TEXTURE_CUBE_MAP);
        assertEquals(GLES20.GL_TEXTURE_CUBE_MAP, texture.getTextureTarget());
    }

    @Test
    public void registerMaterial() throws Exception {
        final Material material = mock(Material.class);
        final TestableBaseTexture texture = new TestableBaseTexture();
        boolean retval = texture.registerMaterial(material);
        assertTrue(retval);
        assertTrue(texture.getRegisteredMaterials().contains(material));
        retval = texture.registerMaterial(material);
        assertFalse(retval);
    }

    @Test
    public void clearRegisteredMaterials() {
        final Material material = mock(Material.class);
        final Material material2 = mock(Material.class);
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.registerMaterial(material);
        texture.registerMaterial(material2);
        texture.clearRegisteredMaterials();
        assertFalse(texture.getRegisteredMaterials().contains(material));
        assertFalse(texture.getRegisteredMaterials().contains(material2));
    }

    @Test
    public void unregisterMaterial() throws Exception {
        final Material material = mock(Material.class);
        final Material material2 = mock(Material.class);
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.registerMaterial(material);
        boolean retval = texture.unregisterMaterial(material);
        assertTrue(retval);
        assertFalse(texture.getRegisteredMaterials().contains(material));
        retval = texture.unregisterMaterial(material2);
        assertFalse(retval);
    }

    @Test
    public void setInfluence() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setInfluence(0.5f);
        assertEquals(Float.floatToIntBits(0.5f), Float.floatToIntBits(texture.getInfluence()));
    }

    @SuppressWarnings("WrongConstant")
    @Test(expected = TextureException.class)
    public void applyMinificationFilterFailFilterMipmapped() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setFilterType(-1);
        texture.setMipmaped(true);
        texture.applyMinificationFilter();
    }

    @SuppressWarnings("WrongConstant")
    @Test(expected = TextureException.class)
    public void applyMinificationFilterFailFilterNotMipmapped() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        texture.setFilterType(-1);
        texture.setMipmaped(false);
        texture.applyMinificationFilter();
    }
}