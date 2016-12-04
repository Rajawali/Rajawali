package c.org.rajawali3d.gl.extensions.texture;

import android.support.annotation.NonNull;

import c.org.rajawali3d.gl.Capabilities.UnsupportedCapabilityException;
import c.org.rajawali3d.gl.extensions.GLExtension;

/**
 * This extension allows for full or partial image updates to a compressed 2D texture from an uncompressed texel data
 * buffer using TexImage2D and TexSubImage2D. Consequently, if a compressed internal format is used, all the
 * restrictions associated with compressed textures will apply. These include sub-image updates aligned to 4x4 pixel
 * blocks and the restriction on usage as render targets.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 *
 * @see <a href="https://www.khronos.org/registry/gles/extensions/NV/NV_texture_compression_s3tc_update.txt">
 *     NV_texture_compression_s3tc_update</a>
 */
public class NVTextureCompressionS3TCUpdate extends GLExtension {

    public static final String name = "GL_NV_texture_compression_s3tc_update";

    @NonNull
    public static NVTextureCompressionS3TCUpdate load() throws UnsupportedCapabilityException {
        return new NVTextureCompressionS3TCUpdate();
    }

    private NVTextureCompressionS3TCUpdate() throws UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
