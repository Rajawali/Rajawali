package c.org.rajawali3d.textures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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

import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
// TODO: Should we verify the GL state for these tests?
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class CubeMapTextureGLTest extends GlTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp(getClass().getSimpleName());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void replaceWithBitmap() throws Exception {
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(false);
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
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
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
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST", references);
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
                    e.printStackTrace();
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void replaceFailNullData() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        texture.setTextureData(references);
        final TextureDataReference reference = mock(TextureDataReference.class);
        doReturn(true).when(reference).isDestroyed();
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(null);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @SuppressWarnings("Range")
    @Test
    public void replaceFailBadLengthData() throws Exception {
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        final TextureDataReference reference = mock(TextureDataReference.class);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(new TextureDataReference[]{ reference });
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void replaceFailNullReferences() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        texture.setTextureData(references);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(new TextureDataReference[]{ null, null, null, null, null, null });
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void replaceFailDestroyedData() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST");
        texture.setTextureData(references);
        final TextureDataReference reference = mock(TextureDataReference.class);
        doReturn(true).when(reference).isDestroyed();
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(new TextureDataReference[]{ reference, reference, reference,
                                                                       reference, reference, reference
                    });
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void replaceWithBitmapFailWidth() throws Exception {
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        assert texture.getTextureData() != null;
        assert texture.getTextureData()[0] != null;
        final int width = texture.getTextureData()[0].getWidth();
        final int height = texture.getTextureData()[0].getHeight();
        final Bitmap bitmap = Bitmap.createBitmap(width / 2, height, Config.ARGB_8888);
        final TextureDataReference[] newReferences = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            newReferences[i] = new TextureDataReference(bitmap, null, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE,
                                                        bitmap.getWidth(), bitmap.getHeight());
        }
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(newReferences);
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
        final int[] ids = new int[]{
                R.drawable.posx, R.drawable.posy, R.drawable.posz,
                R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        assert texture.getTextureData() != null;
        assert texture.getTextureData()[0] != null;
        final int width = texture.getTextureData()[0].getWidth();
        final int height = texture.getTextureData()[0].getHeight();
        final Bitmap bitmap = Bitmap.createBitmap(width, height / 2, Config.ARGB_8888);
        final TextureDataReference[] newReferences = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            newReferences[i] = new TextureDataReference(bitmap, null, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE,
                                                        bitmap.getWidth(), bitmap.getHeight());
        }
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(newReferences);
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
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST", references);
        final TextureDataReference[] newReferences = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            newReferences[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                                                        512, 512);
        }
        texture.setTextureData(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(newReferences);
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
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST", references);
        final TextureDataReference[] newReferences = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            newReferences[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                                                        256, 256);
        }
        texture.setTextureData(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(newReferences);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void replaceWithBitmapFailTexelFormat() throws Exception {
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        assert texture.getTextureData() != null;
        assert texture.getTextureData()[0] != null;
        final int width = texture.getTextureData()[0].getWidth();
        final int height = texture.getTextureData()[0].getHeight();
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        final TextureDataReference[] newReferences = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            newReferences[i] = new TextureDataReference(bitmap, null, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE,
                bitmap.getWidth(), bitmap.getHeight());
        }
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(newReferences);
                    texture.replace();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void add() throws Exception {
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        final TextureDataReference[] references = texture.getTextureData();
        assertNotNull(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setMipmaped(false);
        texture.willRecycle(true);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                } catch (TextureException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
        for (TextureDataReference reference : references) {
            assertTrue(reference.isDestroyed());
        }
        assertNull(texture.getTextureData());
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void addFailFilter1() throws Exception {
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setFilterType(-1);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
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
    public void addFailFilter2() throws Exception {
        final int[] ids = new int[]{
            R.drawable.posx, R.drawable.posy, R.drawable.posz,
            R.drawable.negx, R.drawable.negy, R.drawable.negz
        };
        final CubeMapTexture texture = new CubeMapTexture("TEST", getContext(), ids);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.setFilterType(-1);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
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
    public void addBufferWithoutRecycle() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST", references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
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
    public void addBufferFailWidth() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 0, 512);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST", references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
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
    public void addBufferFailHeight() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[6];
        for (int i = 0; i < 6; ++i) {
            references[i] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 0);
        }
        final CubeMapTexture texture = new CubeMapTexture("TEST", references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
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