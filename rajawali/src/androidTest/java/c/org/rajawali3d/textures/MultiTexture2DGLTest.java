package c.org.rajawali3d.textures;

import static c.org.rajawali3d.textures.annotation.Wrap.MIRRORED_REPEAT_T;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.textures.annotation.Wrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
// TODO: Should we verify the GL state for these tests?
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class MultiTexture2DGLTest extends GlTestCase {

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
            // Generate a texture id
            int[] genTextureNames = new int[1];
            GLES20.glGenTextures(1, genTextureNames, 0);
            int textureId = genTextureNames[0];

            if (textureId > 0) {
                setTextureId(textureId);
            }

            if (willRecycle()) {
                setTextureData(null);
            }
        }

        @Override void replace() throws TextureException {

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
    public void remove() throws Exception {
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final TestableMultiTexture2D texture = new TestableMultiTexture2D(Type.CUBE_MAP, "TEST", getContext(), ids);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.remove();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void removeDontRecycle() throws Exception {
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final TestableMultiTexture2D texture = new TestableMultiTexture2D(Type.CUBE_MAP, "TEST", getContext(), ids);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        texture.willRecycle(false);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.remove();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void removeNoData() throws Exception {
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final TestableMultiTexture2D texture = new TestableMultiTexture2D(Type.CUBE_MAP, "TEST");
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            final Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), ids[i]);
            references[i] = spy(new TextureDataReference(bitmap, null, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                                                         bitmap.getWidth(), bitmap.getHeight()));
        }
        texture.setTextureData(references);
        assertNotNull(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.CLAMP_S | MIRRORED_REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        texture.willRecycle(true);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.remove();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
        assertNull(texture.getTextureData());
        for (TextureDataReference reference : references) {
            assertTrue(reference.isDestroyed());
            verify(reference).recycle();
        }
    }

    @Test
    public void removeNullData() throws Exception {
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final TestableMultiTexture2D texture = new TestableMultiTexture2D(Type.CUBE_MAP, "TEST");
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            final Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), ids[i]);
            references[i] = spy(new TextureDataReference(bitmap, null, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                                                         bitmap.getWidth(), bitmap.getHeight()));
        }
        texture.setTextureData(references);
        assertNotNull(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.CLAMP_S | MIRRORED_REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        texture.willRecycle(true);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(new TextureDataReference[]{null, null, null, null, null, null});
                    texture.remove();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
        assertNotNull(texture.getTextureData());
        for (TextureDataReference reference : references) {
            assertTrue(reference.isDestroyed());
            verify(reference).recycle();
        }
    }
}