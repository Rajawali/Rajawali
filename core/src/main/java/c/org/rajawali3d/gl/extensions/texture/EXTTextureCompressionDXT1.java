package c.org.rajawali3d.gl.extensions.texture;

import android.support.annotation.NonNull;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.GLExtension;

/**
 * This extension provides additional texture compression functionality specific to S3's S3TC format (called DXTC in
 * Microsoft's DirectX API), subject to all the requirements and limitations described by the extension
 * GL_ARB_texture_compression.
 *
 * This extension supports S3TC texture compression format only. For the S3TC image format, this specification supports
 * an RGB-only mode and a special RGBA mode with single-bit "transparent" alpha.
 *
 * If NV_texture_array is supported, the S3TC compressed formats may also be used as the internal formats given to
 * CompressedTexImage3DNV and CompressedTexSubImage3DNV. The restrictions for the <width>, <height>, <xoffset>, and
 * <yoffset> parameters of the CompressedTexSubImage2D function when used with S3TC compressed texture formats,
 * described in this extension, also apply to the identically named parameters of CompressedTexSubImage3DNV.
 *
 * NOTE: You should use the DXT1 tokens defined in {@link EXTTextureCompressionS3TC}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/EXT/texture_compression_dxt1.txt">
 * EXT_texture_compression_dxt1</a>
 */
public class EXTTextureCompressionDXT1 extends GLExtension {

    public static final String name = "GL_EXT_texture_compression_dxt1";

    @NonNull
    public static EXTTextureCompressionDXT1 load() throws Capabilities.UnsupportedCapabilityException {
        return new EXTTextureCompressionDXT1();
    }

    private EXTTextureCompressionDXT1() throws Capabilities.UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
