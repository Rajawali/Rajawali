package c.org.rajawali3d.textures;

import static c.org.rajawali3d.textures.annotation.Wrap.MIRRORED_REPEAT_T;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.ETC1;
import android.opengl.GLES20;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.textures.annotation.Wrap;
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
public class CompressedTexture2DGLTest extends GlTestCase {

    private static class Testable extends CompressedTexture2D {

        public Testable() {
            super();
        }

        public Testable(int type, String name) throws TextureException {
            super(type, name);
        }

        public Testable(int type, String name, TextureDataReference data) throws TextureException {
            super(type, name, data);
        }

        public Testable(int type, String name, TextureDataReference[] data) throws TextureException {
            super(type, name, data);
        }

        public Testable(CompressedTexture2D other) throws TextureException {
            super(other);
        }

        @Override public BaseTexture clone() {
            return null;
        }

        @Override public int getCompressionType() {
            return Compression2D.ETC1;
        }

        @Override public int getTexelFormat() {
            return ETC1.ETC1_RGB8_OES;
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
    public void replaceWithBufferNotMipmapped() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
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
    public void replaceWithBufferMipmapped() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[2];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        references[1] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 128, 256);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
        final StringBuilder builder = new StringBuilder();
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.replace();
                } catch (TextureException e) {
                    e.printStackTrace();
                    thrown[0] = true;
                    builder.append(e.getMessage());
                }
            }
        });
        assertFalse(builder.toString(), thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void replaceWithBuffer1x1() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 1, 1);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
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
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
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

    @Test
    public void replaceFailNullReferences() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.setTextureData(references);
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    texture.add();
                    texture.setTextureData(new TextureDataReference[]{ null });
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
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
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

    /*@Test
    public void replaceFailWithBitmap() throws Exception {
        final TextureDataReference[] data = new TextureDataReference[1];
        final Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.earth_diffuse);
        data[0] = new TextureDataReference(bitmap, null, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmap.getWidth(),
                                           bitmap.getHeight());
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", data);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
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
        assertTrue(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }*/

    @Test
    public void replaceFailWithMipmappedBitmap() throws Exception {
        final TextureDataReference[] data = new TextureDataReference[1];
        final Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.earth_diffuse);
        data[0] = new TextureDataReference(bitmap, null, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmap.getWidth(),
                                           bitmap.getHeight());
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", data);
        texture.setTexelFormat(GLES20.GL_RGBA);
        texture.willRecycle(false);
        final boolean[] thrown = new boolean[]{ false };
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
        assertTrue(thrown[0]);
        assertTrue(texture.getTextureId() > 0);
    }

    @Test
    public void replaceWithBufferFailWidth() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        final TextureDataReference[] newReferences = new TextureDataReference[1];
        newReferences[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 512, 512);
        texture.setTextureData(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{ false };
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
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        final TextureDataReference[] newReferences = new TextureDataReference[1];
        newReferences[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 512, 512);
        texture.setTextureData(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{ false };
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
    public void replaceWithBufferFailDimensions() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        final TextureDataReference[] newReferences = new TextureDataReference[1];
        newReferences[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 512, 512);
        texture.setTextureData(references);
        texture.setTexelFormat(GLES20.GL_RGBA);
        final boolean[] thrown = new boolean[]{ false };
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
    public void add1() throws Exception {
        final TextureDataReference[] data = new TextureDataReference[1];
        final Bitmap original = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.earth_diffuse);
        data[0] = new TextureDataReference(original, null, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, original.getWidth(),
                                           original.getHeight());
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", data);
        final TextureDataReference[] references = texture.getTextureData();
        assertNotNull(references);
        texture.setWrapType(Wrap.CLAMP_S | MIRRORED_REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        texture.willRecycle(true);
        final boolean[] thrown = new boolean[]{ false };
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

    @Test
    public void add2() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 1, 1);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        assertNotNull(references);
        texture.setWrapType(Wrap.CLAMP_S | MIRRORED_REPEAT_T);
        texture.setFilterType(Filter.NEAREST);
        texture.setMipmaped(false);
        texture.willRecycle(true);
        final boolean[] thrown = new boolean[]{ false };
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
        final TextureDataReference[] data = new TextureDataReference[1];
        final Bitmap original = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.earth_diffuse);
        data[0] = new TextureDataReference(original, null, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, original.getWidth(),
                                           original.getHeight());
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", data);
        texture.setFilterType(-1);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
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
        final TextureDataReference[] data = new TextureDataReference[1];
        final Bitmap original = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.earth_diffuse);
        data[0] = new TextureDataReference(original, null, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, original.getWidth(),
                                           original.getHeight());
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", data);
        texture.setFilterType(-1);
        texture.setMipmaped(false);
        final boolean[] thrown = new boolean[]{ false };
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
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
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
    public void addBufferFailMipmapWidth() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[2];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        references[1] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 256);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
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
    public void addBufferFailMipmapHeight() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[2];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 512);
        references[1] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 128, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
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
    public void addBufferFailWidth() throws Exception {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 256 * 512);
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 0, 512);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
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
        final TextureDataReference[] references = new TextureDataReference[1];
        references[0] = new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 256, 0);
        final CompressedTexture2D texture = new Testable(Type.DIFFUSE, "TEST", references);
        texture.willRecycle(false);
        texture.setMipmaped(true);
        final boolean[] thrown = new boolean[]{ false };
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