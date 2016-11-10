package c.org.rajawali3d.textures;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.rajawali3d.R;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class MultiTexture2DTest {

    private final static class TestableMultiTexture2D extends MultiTexture2D {

        @Override public BaseTexture clone() {
            return null;
        }

        @Override void add() throws TextureException {

        }

        @Override void remove() throws TextureException {

        }

        @Override void replace() throws TextureException {

        }
    }

    @Test
    public void setFrom() throws Exception {

    }

    @Test
    public void setTextureDataFromResourceIds() throws Exception {
        final MultiTexture2D texture = new TestableMultiTexture2D();
        final int[] ids = new int[] {
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final TextureDataReference[] data = texture.setTextureDataFromResourceIds(getContext(), ids);
        assertNotNull(data);
        assertEquals(6, data.length);
        for (int i = 0; i < 6; ++i) {
            assertNotNull(data[i]);
            assertTrue(data[i].hasBitmap());
        }
    }

    @Test
    public void setTextureData() throws Exception {
        final MultiTexture2D texture = new TestableMultiTexture2D();

        // Test set with no old data
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        texture.setTextureData(references);

        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i]).holdReference();
        }

        // Test second set with old data present
        final TextureDataReference[] newReferences = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            newReferences[i] = mock(TextureDataReference.class);
        }
        texture.setTextureData(newReferences);

        assertNotNull(texture.getTextureData());
        assertArrayEquals(newReferences, texture.getTextureData());

        for (int i = 0; i < 6; ++i) {
            verify(references[i]).recycle();
            verify(newReferences[i]).holdReference();
        }

        // Test setting null data
        texture.setTextureData(null);
        assertNull(texture.getTextureData());

        // Test setting a non-null array with null references
        final TextureDataReference[] nullReferences = new TextureDataReference[6];
        texture.setTextureData(nullReferences);
        assertNotNull(texture.getTextureData());
        assertArrayEquals(nullReferences, texture.getTextureData());

        // Test setting data with an old non-null array with null references
        texture.setTextureData(null);
        assertNull(texture.getTextureData());
    }

    @Test
    public void reset() throws Exception {

    }
}