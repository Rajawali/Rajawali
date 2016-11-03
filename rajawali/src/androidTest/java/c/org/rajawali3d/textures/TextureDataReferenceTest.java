package c.org.rajawali3d.textures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.support.test.filters.SmallTest;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class TextureDataReferenceTest {

    @Test
    public void recycle() throws Exception {
        final ByteBuffer data = ByteBuffer.allocateDirect(4 * 256 * 256);
        TextureDataReference reference = new TextureDataReference(null, data, GLES20.GL_RGBA,
                                                                  GLES20.GL_UNSIGNED_BYTE, 256, 256);
        reference.holdReference();
        reference.holdReference();
        assertFalse(reference.recycle());
        assertTrue(reference.recycle());
        assertTrue(reference.isDestroyed());
    }

    @Test
    public void getBitmap() throws Exception {

    }

    @Test
    public void getByteBuffer() throws Exception {
        final ByteBuffer data = ByteBuffer.allocateDirect(4 * 256 * 256);
        TextureDataReference reference = new TextureDataReference(null, data, GLES20.GL_RGBA,
                                                                  GLES20.GL_UNSIGNED_BYTE, 256, 256);
        assertEquals(data, reference.getByteBuffer());
    }

    @Test(expected = TextureException.class)
    public void getBitmapWhenDestroyed() throws Exception {
        final Bitmap bitmap = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
        TextureDataReference reference = new TextureDataReference(bitmap, null, GLES20.GL_RGBA,
                                                                  GLES20.GL_UNSIGNED_BYTE, 256, 256);
        reference.recycle();
        reference.getBitmap();
    }

    @Test(expected = TextureException.class)
    public void getByteBufferWhenDestroyed() throws Exception {
        final ByteBuffer data = ByteBuffer.allocateDirect(4 * 256 * 256);
        TextureDataReference reference = new TextureDataReference(null, data, GLES20.GL_RGBA,
                                                                  GLES20.GL_UNSIGNED_BYTE, 256, 256);
        reference.recycle();
        reference.getByteBuffer();
    }

    @Test(expected = TextureException.class)
    public void getBitmapWhenNoBitmap() throws Exception {
        TextureDataReference reference = new TextureDataReference(null, null, GLES20.GL_RGBA,
                                                                  GLES20.GL_UNSIGNED_BYTE, 256, 256);
        reference.getBitmap();
    }

    @Test(expected = TextureException.class)
    public void getByteBufferWhenNoBuffer() throws Exception {
        TextureDataReference reference = new TextureDataReference(null, null, GLES20.GL_RGBA,
                                                                  GLES20.GL_UNSIGNED_BYTE, 256, 256);
        reference.getByteBuffer();
    }
}