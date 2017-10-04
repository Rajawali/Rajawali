package c.org.rajawali3d.textures;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.opengl.GLES20;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;

import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class Texture2DTest {

    @Test
    public void testConstructFromOtherTexture2D() throws Exception {
        final Texture2D from = new Texture2D("FROM");
        from.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        final Texture2D to = new Texture2D(from);
        assertNotNull(to);
        assertEquals("FROM", to.getTextureName());
    }

    @Test
    public void testCosntructWithName() throws Exception {
        final Texture2D texture = new Texture2D("NAME");
        assertNotNull(texture);
        assertEquals("NAME", texture.getTextureName());
    }

    @Test
    public void testConstructWithResource() throws Exception {
        final Texture2D texture = new Texture2D("NAME", getContext(), R.drawable.earth_diffuse);
        assertNotNull(texture);
        assertNotNull(texture.getTextureData());
        assertEquals("NAME", texture.getTextureName());
        assertTrue(texture.getTextureData().hasBitmap());
    }

    @Test
    public void testConstructWithData() throws Exception {
        final TextureDataReference reference = new TextureDataReference(null, ByteBuffer.allocateDirect(4 * 256 * 256),
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 256);
        final Texture2D texture = new Texture2D("NAME", reference);
        assertNotNull(texture);
        assertNotNull(texture.getTextureData());
        assertEquals(reference, texture.getTextureData());
        assertEquals("NAME", texture.getTextureName());
    }

    @Test
    public void testClone() throws Exception {
        final TextureDataReference reference = new TextureDataReference(null, ByteBuffer.allocateDirect(4 * 256 * 256),
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 256);
        final Texture2D from = new Texture2D("FROM", reference);
        final Texture2D texture = from.clone();
        assertNotNull(texture);
        assertEquals("FROM", texture.getTextureName());
        assertEquals(reference, texture.getTextureData());
    }

    @Test
    public void testCloneFail() throws Exception {
        final Texture2D from = new Texture2D("FROM");
        final Texture2D texture = from.clone();
        assertNull(texture);
    }
}