package c.org.rajawali3d.gl.extensions.texture;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.GLExtension;

import static c.org.rajawali3d.gl.extensions.texture.EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT3_EXT;

/**
 * This extension provides additional texture compression functionality specific to Imagination Technologies PowerVR
 * Texture compression format (called PVRTC) subject to all the requirements and limitations described by the OpenGL 1.3
 * specifications.
 *
 * This extension supports 4 and 2 bit per pixel texture compression formats. Because the compression of PVRTC is very
 * CPU intensive, it is not appropriate to carry out compression on the target platform. Therefore this extension only
 * supports the loading of compressed texture data.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/IMG/IMG_texture_compression_pvrtc.txt">
 * IMG_texture_compression_pvrtc</a>
 */
@SuppressWarnings("WeakerAccess")
public class IMGTextureCompressionPVRTC extends GLExtension {

    public static final String name = "GL_IMG_texture_compression_pvrtc";

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COMPRESSED_RGB_PVRTC_4BPPV1_IMG, COMPRESSED_RGB_PVRTC_2BPPV1_IMG, COMPRESSED_RGBA_PVRTC_4BPPV1_IMG,
        COMPRESSED_RGBA_S3TC_DXT3_EXT, COMPRESSED_RGBA_PVRTC_2BPPV1_IMG})
    public @interface PVRTCFormat {
    }

    // Tokens accepted by the <internalformat> parameter of CompressedTexImage2D and the <format> parameter of
    // CompressedTexSubImage2D:

    public static final int COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
    public static final int COMPRESSED_RGB_PVRTC_2BPPV1_IMG = 0x8C01;
    public static final int COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
    public static final int COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;

    @NonNull
    public static IMGTextureCompressionPVRTC load() throws Capabilities.UnsupportedCapabilityException {
        return new IMGTextureCompressionPVRTC();
    }

    private IMGTextureCompressionPVRTC() throws Capabilities.UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
