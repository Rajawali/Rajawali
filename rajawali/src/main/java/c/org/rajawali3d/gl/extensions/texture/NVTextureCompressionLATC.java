package c.org.rajawali3d.gl.extensions.texture;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.GLExtension;

/**
 * This extension introduces four new block-based texture compression formats suited for unsigned and signed luminance
 * and luminance-alpha textures (hence the name "latc" for Luminance-Alpha Texture Compression).
 * <p>
 * These formats are designed to reduce the storage requirements and memory bandwidth required for luminance and
 * luminance-alpha textures by a factor of 2-to-1 over conventional uncompressed luminance and luminance-alpha textures
 * with 8-bit components.
 * <p>
 * The compressed signed luminance-alpha format is reasonably suited for storing compressed normal maps.
 *
 * If NV_texture_array is supported, the LATC compressed formats may also be used as the internal formats given to
 * CompressedTexImage3DNV and CompressedTexSubImage3DNV. The restrictions for the <width>, <height>, <xoffset>, and
 * <yoffset> parameters of the CompressedTexSubImage2D function when used with LATC compressed texture formats,
 * described in this extension, also apply to the identically named parameters of CompressedTexSubImage3DNV.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/NV/NV_texture_compression_latc.txt">
 * NV_texture_compression_latc</a>
 */
public class NVTextureCompressionLATC extends GLExtension {

    public static final String name = "GL_NV_texture_compression_latc";

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COMPRESSED_LUMINANCE_LATC1_NV, COMPRESSED_SIGNED_LUMINANCE_LATC1_NV, COMPRESSED_LUMINANCE_ALPHA_LATC2_NV,
        COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV})
    public @interface LATCFormat {
    }

    // Accepted by the<internalformat> parameter of CompressedTexImage2D and CompressedTexImage3DOES.

    public static final int COMPRESSED_LUMINANCE_LATC1_NV = 0x8C70;
    public static final int COMPRESSED_SIGNED_LUMINANCE_LATC1_NV = 0x8C71;
    public static final int COMPRESSED_LUMINANCE_ALPHA_LATC2_NV = 0x8C72;
    public static final int COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV = 0x8C73;

    @NonNull
    public static NVTextureCompressionLATC load() throws Capabilities.UnsupportedCapabilityException {
        return new NVTextureCompressionLATC();
    }

    private NVTextureCompressionLATC() throws Capabilities.UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
