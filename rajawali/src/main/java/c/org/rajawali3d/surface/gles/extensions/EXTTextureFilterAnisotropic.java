package c.org.rajawali3d.surface.gles.extensions;

import android.opengl.GLES20;
import android.opengl.GLException;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.surface.gles.GLESCapabilities.UnsupportedCapabilityException;
import org.rajawali3d.util.RajLog;

/**
 * This extension permits the OpenGL application to specify on a per-texture object basis the maximum degree of
 * anisotropy to account for in texture filtering. Increasing a texture object's maximum degree of anisotropy may
 * improve texture filtering but may also significantly reduce the implementation's texture filtering rate.
 * Implementations are free to clamp the specified degree of anisotropy to the implementation's maximum supported
 * degree of anisotropy.
 *
 * Applications seeking the highest quality anisotropic filtering available are advised to request a
 * LINEAR_MIPMAP_LINEAR minification filter, a BILINEAR magnification filter, and a large maximum degree of anisotropy.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 *
 * @see <a href="https://www.opengl.org/registry/specs/EXT/texture_filter_anisotropic.txt">
 *     EXT_texture_filter_anisotropic</a>
 */
public class EXTTextureFilterAnisotropic implements GLExtension {

    public static final String name = "GL_EXT_texture_filter_anisotropic";

    // Tokens accepted by the <pname> parameters of GetTexParameterfv, GetTexParameteriv, TexParameterf, TexParameterfv,
    // TexParameteri, and TexParameteriv
    public static final int TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;

    // Tokens accepted by the <pname> parameters of GetBooleanv, GetDoublev, GetFloatv, and GetIntegerv
    public static final int MAX_TEXTURE_MAX_ANISOTROPY_EXT  = 0x84FF;

    @FloatRange(from = 1.0)
    private float maxSupportedAnisotropy = 1.0f;

    @NonNull
    public static EXTTextureFilterAnisotropic load() throws UnsupportedCapabilityException {
        return new EXTTextureFilterAnisotropic();
    }

    private EXTTextureFilterAnisotropic() throws UnsupportedCapabilityException {
        final float[] params = new float[1];
        GLES20.glGetFloatv(MAX_TEXTURE_MAX_ANISOTROPY_EXT, params, 0);
        try {
            RajLog.checkGLError(name);
        } catch (GLException e) {
            throw new UnsupportedCapabilityException(e);
        }
        maxSupportedAnisotropy = params[0];
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @FloatRange(from = 1.0)
    public float getMaxSupportedAnisotropy() {
        return maxSupportedAnisotropy;
    }
}
