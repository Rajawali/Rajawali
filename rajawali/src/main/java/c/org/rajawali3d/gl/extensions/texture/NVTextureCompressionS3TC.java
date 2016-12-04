package c.org.rajawali3d.gl.extensions.texture;

import android.support.annotation.NonNull;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.GLExtension;

/**
 * This extension provides additional texture compression functionality specific to S3's S3TC format (called DXTC in
 * Microsoft's DirectX API), subject to all the requirements and limitations described by the extension
 * GL_ARB_texture_compression.
 *
 * This extension supports S3TC, DXT3, and DXT5 texture compression formats. For the S3TC image format, this
 * specification supports an RGB-only mode and a special RGBA mode with single-bit "transparent" alpha.
 *
 * If NV_texture_array is supported, the S3TC compressed formats may also be used as the internal formats given to
 * CompressedTexImage3DNV and CompressedTexSubImage3DNV. The restrictions for the <width>, <height>, <xoffset>, and
 * <yoffset> parameters of the CompressedTexSubImage2D function when used with S3TC compressed texture formats,
 * described in this extension, also apply to the identically named parameters of CompressedTexSubImage3DNV.
 *
 * NOTE: You should use the tokens defined in {@link EXTTextureCompressionS3TC}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.opengl.org/registry/specs/EXT/texture_compression_s3tc.txt">
 * EXT_texture_compression_s3tc</a>
 */
public class NVTextureCompressionS3TC extends GLExtension {

    public static final String name = "GL_NV_texture_compression_s3tc";

    @NonNull
    public static NVTextureCompressionS3TC load() throws Capabilities.UnsupportedCapabilityException {
        return new NVTextureCompressionS3TC();
    }

    private NVTextureCompressionS3TC() throws Capabilities.UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
