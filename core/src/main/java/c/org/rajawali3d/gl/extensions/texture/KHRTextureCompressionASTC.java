package c.org.rajawali3d.gl.extensions.texture;

import android.support.annotation.NonNull;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.GLExtension;

/**
 * Adaptive Scalable Texture Compression (ASTC) is a new texture compression technology that offers unprecendented
 * flexibility, while producing better or comparable results than existing texture compressions at all bit rates. It
 * includes support for 2D and slice-based 3D textures, with low and high dynamic range, at bitrates from below 1
 * bit/pixel up to 8 bits/pixel in fine steps.
 * <p>
 * The goal of these extensions is to support the full 2D profile of the ASTC texture compression specification, and
 * allow construction of 3D textures from multiple compressed 2D slices.
 * <p>
 * ASTC-compressed textures are handled in OpenGL ES and OpenGL by adding new supported formats to the existing
 * commands
 * for defining and updating compressed textures, and defining the interaction of the ASTC formats with each texture
 * target.
 * <p>
 * Some of the functionality of these extensions is not supported if the underlying implementation does not support
 * cube
 * map array textures.
 *
 * NOTE: You should use the tokens defined in {@link OESTextureCompressionASTC}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/OES/OES_texture_compression_astc.txt">
 * OES_texture_compression_astc</a>
 */
@SuppressWarnings("WeakerAccess")
public class KHRTextureCompressionASTC extends GLExtension {

    public static final String name = "GL_KHR_texture_compression_astc_ldr";
    public static final String hdr_name = "GL_KHR_texture_compression_astc_hdr";

    private boolean hdrSupported = false;

    @NonNull
    public static KHRTextureCompressionASTC load() throws Capabilities.UnsupportedCapabilityException {
        return new KHRTextureCompressionASTC();
    }

    private KHRTextureCompressionASTC() throws Capabilities.UnsupportedCapabilityException {
        verifySupport(name);
        try {
            verifySupport(hdr_name);
            hdrSupported = true;
        } catch (Capabilities.UnsupportedCapabilityException e) {
            hdrSupported = false;
        }
    }

    /**
     * Retrieves the result of the load time check to see if the HDR profile of ASTC is supported on this device.
     *
     * @return {@code true} If the HDR profile is supported or {@code false} if only the LDR profile is supported.
     */
    public boolean isHDRSupported() {
        return hdrSupported;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
