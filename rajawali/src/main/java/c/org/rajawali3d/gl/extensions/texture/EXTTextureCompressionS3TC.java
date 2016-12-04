package c.org.rajawali3d.gl.extensions.texture;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.GLExtension;

/**
 * This extension provides additional texture compression functionality specific to S3's S3TC format (called DXTC in
 * Microsoft's DirectX API), subject to all the requirements and limitations described by the extension
 * GL_ARB_texture_compression.
 *
 * This extension supports DXT1, DXT3, and DXT5 texture compression formats. For the DXT1 image format, this
 * specification supports an RGB-only mode and a special RGBA mode with single-bit "transparent" alpha.
 *
 * If NV_texture_array is supported, the S3TC compressed formats may also be used as the internal formats given to
 * CompressedTexImage3DNV and CompressedTexSubImage3DNV. The restrictions for the <width>, <height>, <xoffset>, and
 * <yoffset> parameters of the CompressedTexSubImage2D function when used with S3TC compressed texture formats,
 * described in this extension, also apply to the identically named parameters of CompressedTexSubImage3DNV.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.opengl.org/registry/specs/EXT/texture_compression_s3tc.txt">
 * EXT_texture_compression_s3tc</a>
 */
public class EXTTextureCompressionS3TC implements GLExtension {

    public static final String name = "GL_EXT_texture_compression_s3tc";

    @Documented
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COMPRESSED_RGB_S3TC_DXT1_EXT, COMPRESSED_RGBA_S3TC_DXT1_EXT, COMPRESSED_RGBA_S3TC_DXT3_EXT,
        COMPRESSED_RGBA_S3TC_DXT5_EXT})
    public @interface S3TCFormat {
    }

    // Tokens accepted by the <internalformat> parameter of CompressedTexImage2D and the <format> parameter of
    // CompressedTexSubImage2D:

    public static final int COMPRESSED_RGB_S3TC_DXT1_EXT = 0x83F0;
    public static final int COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83F1;
    public static final int COMPRESSED_RGBA_S3TC_DXT3_EXT = 0x83F2;
    public static final int COMPRESSED_RGBA_S3TC_DXT5_EXT = 0x83F3;

    @NonNull
    public static EXTTextureCompressionS3TC load() throws Capabilities.UnsupportedCapabilityException {
        return new EXTTextureCompressionS3TC();
    }

    private EXTTextureCompressionS3TC() throws Capabilities.UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
