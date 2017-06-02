package c.org.rajawali3d.gl.extensions.texture;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.GLExtension;

/**
 * The goal of this extension is to allow direct support of palettized textures in OpenGL ES. Palettized textures are
 * implemented in OpenGL ES using the CompressedTexImage2D call. The definition of the following parameters "level" and
 * "internalformat" in the CompressedTexImage2D call have been extended to support paletted textures.
 *
 * A paletted texture is described by the following data:
 *
 * palette format can be R5_G6_B5, RGBA4, RGB5_A1, RGB8, or RGBA8
 *
 * number of bits to represent texture data can be 4 bits or 8 bits per texel.  The number of bits also detemine the
 * size of the palette.  For 4 bits/texel the palette size is 16 entries and for 8 bits/texel the palette size will be
 * 256 entries.
 *
 * The palette format and bits/texel are encoded in the "internalformat" parameter.
 *
 * palette data and texture mip-levels The palette data followed by all necessary mip levels are passed in "data"
 * parameter of CompressedTexImage2D.
 *
 * The size of palette is given by palette format and bits / texel. A palette format of RGB_565 with 4 bits/texel imply
 * a palette size of 2 bytes/palette entry * 16 entries = 32 bytes.
 *
 * The level value is used to indicate how many mip levels are described.  Negative level values are used to define the
 * number of miplevels described in the "data" component. A level of zero indicates a single mip-level.

 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.opengl.org/registry/specs/OES/OES_compressed_paletted_texture.txt">
 * </a>
 */
@SuppressWarnings("WeakerAccess")
public class OESCompressedPalettedTexture extends GLExtension {

    public static final String name = "GL_OES_compressed_paletted_texture";

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PALETTE4_RGB8_OES, PALETTE4_RGBA8_OES, PALETTE4_R5_G6_B5_OES, PALETTE4_RGBA4_OES, PALETTE4_RGB5_A1_OES,
        PALETTE8_RGB8_OES, PALETTE8_RGBA8_OES, PALETTE8_R5_G6_B5_OES, PALETTE8_RGBA4_OES, PALETTE8_RGB5_A1_OES})
    public @interface PalettedFormat {
    }

    // Tokens accepted by the<level> parameter of CompressedTexImage2D Zero and negative values.
    // |level|+1 determines the number of mip levels defined for the paletted texture.

    // Tokens accepted by the<internalformat> paramter of CompressedTexImage2D

    public static final int PALETTE4_RGB8_OES = 0x8B90;
    public static final int PALETTE4_RGBA8_OES = 0x8B91;
    public static final int PALETTE4_R5_G6_B5_OES = 0x8B92;
    public static final int PALETTE4_RGBA4_OES = 0x8B93;
    public static final int PALETTE4_RGB5_A1_OES = 0x8B94;
    public static final int PALETTE8_RGB8_OES = 0x8B95;
    public static final int PALETTE8_RGBA8_OES = 0x8B96;
    public static final int PALETTE8_R5_G6_B5_OES = 0x8B97;
    public static final int PALETTE8_RGBA4_OES = 0x8B98;
    public static final int PALETTE8_RGB5_A1_OES = 0x8B99;

    @NonNull
    public static OESCompressedPalettedTexture load() throws Capabilities.UnsupportedCapabilityException {
        return new OESCompressedPalettedTexture();
    }

    private OESCompressedPalettedTexture() throws Capabilities.UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
