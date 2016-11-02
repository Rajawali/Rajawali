package org.rajawali3d.textures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;
import org.rajawali3d.textures.annotation.Filter;
import org.rajawali3d.textures.annotation.Wrap;

import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
// TODO: Should we verify the GL state for these tests?
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class SingleTexture2DGLTest extends GlTestCase {

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
    public void remove() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
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
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        texture.willRecycle(false);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
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
    public void replaceWithBitmap() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void replaceWithMipmappedBitmap() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setMipmaped(true);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void replaceWithBuffer() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, 256, 512);
        texture.setTextureData(reference);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void replaceWithBitmapFailWidth() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final TextureDataReference reference = texture.setTextureDataFromResourceId(getContext(),
            R.drawable.earth_diffuse);
        final Bitmap bitmap = Bitmap.createBitmap(reference.getWidth() / 2, reference.getHeight(),
                                                  Bitmap.Config.ARGB_8888);
        final TextureDataReference reference2 = new TextureDataReference(bitmap, null, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, bitmap.getWidth(), bitmap.getHeight());

        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(reference2);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void replaceWithBitmapFailHeight() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final TextureDataReference reference = texture.setTextureDataFromResourceId(getContext(),
            R.drawable.earth_diffuse);
        final Bitmap bitmap = Bitmap.createBitmap(reference.getWidth(), reference.getHeight() / 2,
                                                  Bitmap.Config.ARGB_8888);
        final TextureDataReference reference2 = new TextureDataReference(bitmap, null, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, bitmap.getWidth(), bitmap.getHeight());
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(reference2);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void replaceWithBufferFailWidth() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final TextureDataReference reference2 = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, 512, 512);
        texture.setTextureData(reference);
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(reference2);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void replaceWithBufferFailHeight() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final TextureDataReference reference2 = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, 256, 256);
        texture.setTextureData(reference);
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(reference2);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void textureAdd1() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.CLAMP_S | Wrap.MIRRORED_REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
        assertNull(texture.getTextureData());
    }

    @Test
    public void textureAdd2() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.MIRRORED_REPEAT_S | Wrap.CLAMP_T);
        texture.setFilterType(Filter.BILINEAR);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void textureAdd3() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.TRILINEAR);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void textureAdd4() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void textureAdd5() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.BILINEAR);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void textureAdd6() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.TRILINEAR);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void textureAdd7() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.TRILINEAR);
        texture.setMaxAnisotropy(2.0f);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (Exception e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void textureAdd8() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.TRILINEAR);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void textureAddFailFilter1() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(-1);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
        assertTrue(texture.getTextureId() == -1);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void textureAddFailFilter2() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        texture.setTextureDataFromResourceId(getContext(), R.drawable.earth_diffuse);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(-1);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
        assertTrue(texture.getTextureId() == -1);
    }

    @Test
    public void textureAddBufferWithoutRecycle() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
                                                                        GLES20.GL_UNSIGNED_BYTE, 256, 512);
        texture.setTextureData(reference);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.TRILINEAR);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
        assertNotNull(texture.getTextureData());
    }

    @Test
    public void textureAddBufferFailWidth() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
                                                                        GLES20.GL_UNSIGNED_BYTE, 0, 512);
        texture.setTextureData(reference);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.TRILINEAR);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
        assertTrue(texture.getTextureId() == -1);
    }

    @Test
    public void textureAddBufferFailHeight() throws Exception {
        final TestableSingleTexture2D texture = new TestableSingleTexture2D();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference reference = new TextureDataReference(null, buffer, GLES20.GL_RGBA,
                                                                        GLES20.GL_UNSIGNED_BYTE, 256, 0);
        texture.setTextureData(reference);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setWrapType(Wrap.REPEAT_S | Wrap.REPEAT_T);
        texture.setFilterType(Filter.TRILINEAR);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
        assertTrue(texture.getTextureId() == -1);
    }
}