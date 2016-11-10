package c.org.rajawali3d.textures;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import c.org.rajawali3d.textures.annotation.Type;
import org.junit.Test;
import org.rajawali3d.R;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class MultiTexture2DTest {

    private static class TestableMultiTexture2D extends MultiTexture2D {

        public TestableMultiTexture2D() {
            super();
        }

        public TestableMultiTexture2D(int type, String name) {
            super(type, name);
        }

        public TestableMultiTexture2D(int type, String name, Context context, int[] resourceIds) {
            super(type, name, context, resourceIds);
        }

        public TestableMultiTexture2D(int type, String name, TextureDataReference[] data) {
            super(type, name, data);
        }

        public TestableMultiTexture2D(MultiTexture2D other) throws TextureException {
            super(other);
        }

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
    public void constructorTypeName() throws Exception {
        final MultiTexture2D texture = new TestableMultiTexture2D(Type.CUBE_MAP, "TEST");
        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
    }

    @Test
    public void constructorResourceIds() throws Exception {
        final int[] ids = new int[] {
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final Context context = getContext();
        final MultiTexture2D texture = new TestableMultiTexture2D(Type.CUBE_MAP, "TEST", context, ids);
        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        final TextureDataReference[] data = texture.getTextureData();
        assertNotNull(data);
        assertEquals(6, data.length);
        for (int i = 0; i < 6; ++i) {
            assertNotNull(data[i]);
            assertTrue(data[i].hasBitmap());
        }
    }

    @Test
    public void constructorTextureReferences() throws Exception {
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        final MultiTexture2D texture = new TestableMultiTexture2D(Type.CUBE_MAP, "TEST", references);

        assertEquals(Type.CUBE_MAP, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i]).holdReference();
        }
    }

    @Test
    public void constructorOtherTexture() throws Exception {
        final MultiTexture2D other = new TestableMultiTexture2D();

        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        other.setTextureData(references);

        final MultiTexture2D texture = new TestableMultiTexture2D(other);

        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i], times(2)).holdReference();
        }
    }

    @Test
    public void setFrom() throws Exception {
        final MultiTexture2D other = new TestableMultiTexture2D();

        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        other.setTextureData(references);

        final MultiTexture2D texture = new TestableMultiTexture2D();
        texture.setFrom(other);

        assertNotNull(texture.getTextureData());
        assertArrayEquals(references, texture.getTextureData());
        for (int i = 0; i < 6; ++i) {
            verify(references[i], times(2)).holdReference();
        }
    }

    @Test(expected = TextureException.class)
    public void setFromAndFail() throws Exception {
        final MultiTexture2D other = new TestableMultiTexture2D();

        final MultiTexture2D texture = new TestableMultiTexture2D();
        texture.setFrom(other);
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
        final MultiTexture2D texture = new TestableMultiTexture2D();

        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = mock(TextureDataReference.class);
        }
        texture.setTextureData(references);
        texture.reset();

        assertNull(texture.getTextureData());

        for (int i = 0; i < 6; ++i) {
            verify(references[i]).recycle();
        }

        // Try with no data
        texture.reset();
    }
}