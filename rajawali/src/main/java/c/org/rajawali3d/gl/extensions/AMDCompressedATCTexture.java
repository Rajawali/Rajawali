package c.org.rajawali3d.gl.extensions;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import c.org.rajawali3d.gl.Capabilities;

/**
 * This extension enables support for ATC compressed texture formats.  ATC is AMD's proprietary compression algorithm
 * for compressing textures for handheld devices to save on power consumption, memory footprint and bandwidth.
 * <p>
 * Three compression formats are introduced:
 * <p>
 * - A compression format for RGB textures.
 * - A compression format for RGBA textures using explicit alpha encoding.
 * - A compression format for RGBA textures using interpolated alpha encoding.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/AMD/AMD_compressed_ATC_texture.txt">
 * AMD_compressed_ATC_texture</a>
 */
public class AMDCompressedATCTexture implements GLExtension {

    public static final String name = "GL_AMD_compressed_ATC_texture";

    @Documented
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GL_ATC_RGB_AMD, GL_ATC_RGBA_EXPLICIT_ALPHA_AMD, GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD})
    public @interface ATCFormat {
    }

    // Accepted by the<internalformat> parameter of CompressedTexImage2D and CompressedTexImage3DOES.

    /**
     * This format compresses blocks of source texels down to 4 bits per texel. Assuming 8-bit component source texels,
     * this represents a 8:1 compression ratio.  This is the best format to use when no alpha channel is needed.
     */
    public static final int GL_ATC_RGB_AMD = 0x8C92;

    /**
     * This format compresses blocks of source texels down to 8 bits per texel. Assuming 8-bit component source texels,
     * this represents a 4:1 compression ratio.  This is generally the best format to use when alpha transitions are
     * sharp.
     */
    public static final int GL_ATC_RGBA_EXPLICIT_ALPHA_AMD = 0x8C93;

    /**
     * This format compresses blocks of source texels down to 8 bits per texel. Assuming 8-bit component source texels,
     * this represents a 4:1 compression ratio.  This is generally the best format to use when alpha transitions are
     * gradient.
     */
    public static final int GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD = 0x87EE;

    @NonNull
    public static AMDCompressedATCTexture load() throws Capabilities.UnsupportedCapabilityException {
        return new AMDCompressedATCTexture();
    }

    private AMDCompressedATCTexture() throws Capabilities.UnsupportedCapabilityException {
        Capabilities.getInstance().verifyExtension(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
