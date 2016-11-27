package c.org.rajawali3d.gl.extensions;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.Capabilities.UnsupportedCapabilityException;
import c.org.rajawali3d.textures.TextureDataReference;

/**
 * This extension permits the OpenGL application to utilize ETC1 textures.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/OES/OES_compressed_ETC1_RGB8_texture.txt">
 * OES_compressed_ETC1_RGB8_texture</a>
 */
public class OESCompressedETC1RGB8 implements GLExtension {

    public static final String name = "GL_OES_compressed_ETC1_RGB8";

    // Tokens accepted by the <internalformat> parameter of CompressedTexImage2D
    public static final int ETC1_RGB8_OES = 0x8D64;

    @NonNull
    public static OESCompressedETC1RGB8 load() throws UnsupportedCapabilityException {
        return new OESCompressedETC1RGB8();
    }

    private OESCompressedETC1RGB8() throws UnsupportedCapabilityException {
        Capabilities.getInstance().verifyExtension(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * Creates a {@link TextureDataReference} array containing a {@link ByteBuffer} of the ETC1 texture data and no
     * fallback {@link Bitmap}.
     *
     * @param resources {@link Resources} The Android application resources.
     * @param id        {@link DrawableRes} or {@link RawRes} resource id to load.
     *
     * @return {@link TextureDataReference} array of texture data.
     * @throws IOException Thrown if an error occurs reading the resource data.
     */
    @NonNull
    public TextureDataReference[] createReferenceFromResourceId(@NonNull Resources resources,
                                                                       @RawRes @DrawableRes int id)
        throws IOException {
        final InputStream stream = resources.openRawResource(id);
        final ETC1Util.ETC1Texture texture = ETC1Util.createTexture(stream);
        final TextureDataReference reference = new TextureDataReference(null, texture.getData(), GLES20.GL_RGB,
            GLES20.GL_UNSIGNED_BYTE, texture.getWidth(), texture.getHeight());
        return new TextureDataReference[]{reference};
    }

    /**
     * Creates a {@link TextureDataReference} array containing {@link ByteBuffer}s of the ETC1 texture data and no
     * fallback {@link Bitmap}s.
     *
     * @param resources {@link Resources} The Android application resources.
     * @param ids       {@link DrawableRes} or {@link RawRes} {@code int[]} The resource ids to load.
     *
     * @return {@link TextureDataReference} array of texture data.
     * @throws IOException Thrown if an error occurs reading the resource data.
     */
    @NonNull
    public TextureDataReference[] createReferenceFromResourceId(@NonNull Resources resources,
                                                                       @NonNull @RawRes @DrawableRes int[] ids)
        throws IOException {
        final TextureDataReference[] dataReferences = new TextureDataReference[ids.length];
        for (int i = 0, j = ids.length; i < j; ++i) {
            final InputStream stream = resources.openRawResource(ids[i]);
            final ETC1Util.ETC1Texture texture = ETC1Util.createTexture(stream);
            dataReferences[i] = new TextureDataReference(null, texture.getData(), GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_BYTE, texture.getWidth(), texture.getHeight());

        }
        return dataReferences;
    }
}
