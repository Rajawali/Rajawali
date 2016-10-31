package c.org.rajawali3d.gl.extensions;

import android.support.annotation.NonNull;
import org.rajawali3d.textures.annotation.DataType;
import org.rajawali3d.textures.annotation.PixelFormat;
import org.rajawali3d.textures.annotation.TexelFormat;
import org.rajawali3d.textures.annotation.TextureTarget;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * This extension adds support for 3D textures.  The OpenGL ES 2.0 texture wrap modes and mip-mapping is supported
 * for power of two 3D textures.  Mip-mapping and texture wrap modes other than CLAMP_TO_EDGE are not supported for
 * non-power of two 3D textures.
 *
 * The OES_texture_npot extension, if supported, will enable mip-mapping and other wrap modes for non-power of two 3D
 * textures.

 * @author Jared Woolston (Jared.Woolston@gmail.com)
 *
 * @see <a href="https://www.khronos.org/registry/gles/extensions/OES/OES_texture_3D.txt">OES_texture_3D</a>
 */
public class OESTexture3D implements GLExtension {

    public static final String name = "GL_OES_texture_3D";

    // Tokens accepted by the <target> parameter of TexImage3DOES, TexSubImage3DOES, CopyTexSubImage3DOES,
    // CompressedTexImage3DOES and CompressedTexSubImage3DOES, GetTexParameteriv, and GetTexParameterfv
    public static final int TEXTURE_3D_OES = 0x806F;

    // Tokens accepted by the <pname> parameter of TexParameteriv, TexParameterfv, GetTexParameteriv,
    // and GetTexParameterfv
    public static final int TEXTURE_WRAP_R_OES = 0x8072;

    // Tokens accepted by the <pname> parameter of GetBooleanv, GetIntegerv, and GetFloatv
    public static final int MAX_3D_TEXTURE_SIZE_OES = 0x8073;
    public static final int TEXTURE_BINDING_3D_OES = 0x806A;

    private static native void loadFunctions();

    public static native void texImage3DOES(@TextureTarget int target, int level, @TexelFormat int internalFormat,
                                            int width, int height, int depth, int border,
                                            @PixelFormat int format, @DataType int dataType, @NonNull Buffer pixels);

    public static native void texSubImage3DOES(@TextureTarget int target, int level, int xoffset, int yoffset,
                                               int zoffset, int width, int height, int depth, @PixelFormat int format,
                                               @DataType int dataType, @NonNull ByteBuffer pixels);

    public static native void copyTexSubImage3DOES(@TextureTarget int target, int level, int xoffset, int yoffset,
                                                   int zoffset, int x, int y, int width, int height);

    @NonNull
    public static OESTexture3D load() {
        return new OESTexture3D();
    }

    private OESTexture3D() {

    }

    @NonNull
    @Override
    public String getName() {
        return null;
    }
}
