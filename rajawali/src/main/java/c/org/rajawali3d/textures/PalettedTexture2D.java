/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package c.org.rajawali3d.textures;

import android.opengl.GLES20;
import android.support.annotation.NonNull;

import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.texture.OESCompressedPalettedTexture;
import c.org.rajawali3d.gl.extensions.texture.OESCompressedPalettedTexture.PalettedFormat;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type;

@SuppressWarnings("WeakerAccess")
public class PalettedTexture2D extends CompressedTexture2D {

    /**
     * Constructs a new {@link PalettedTexture2D} with the specified name and type.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link PalettedFormat} The paletted compression format.
     * @param name   {@link String} The texture name.
     */
    public PalettedTexture2D(@Type.TextureType int type, @PalettedFormat int format, @NonNull String name)
        throws TextureException {
        super(type, name);
        setCompressionType(Compression2D.PALETTED);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link PalettedTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link PalettedFormat} The paletted compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public PalettedTexture2D(@Type.TextureType int type, @PalettedFormat int format, @NonNull String name,
                        @NonNull TextureDataReference data) throws TextureException {
        super(type, name, data);
        if ((format == OESCompressedPalettedTexture.PALETTE4_RGB8_OES
            || format == OESCompressedPalettedTexture.PALETTE4_R5_G6_B5_OES
            || format == OESCompressedPalettedTexture.PALETTE8_RGB8_OES
            || format == OESCompressedPalettedTexture.PALETTE8_R5_G6_B5_OES)
            && data.getPixelFormat() != GLES20.GL_RGB) {
            throw new TextureException("When using PALETTE4_RGB8_OES, PALETTE4_R5_G6_B5_OES, PALETTE8_RGB8_OES,"
                + " or PALETTE8_R5_G6_B5_OES texel formats, the pixel format must be GL_RGB.");
        } else if ((format == OESCompressedPalettedTexture.PALETTE4_RGBA8_OES
            || format == OESCompressedPalettedTexture.PALETTE4_RGBA4_OES
            || format == OESCompressedPalettedTexture.PALETTE4_RGB5_A1_OES
            || format == OESCompressedPalettedTexture.PALETTE8_RGBA8_OES
            || format == OESCompressedPalettedTexture.PALETTE8_RGBA4_OES
            || format == OESCompressedPalettedTexture.PALETTE4_RGB5_A1_OES)
            && data.getPixelFormat() != GLES20.GL_RGBA) {
            throw new TextureException("When using PALETTE4_RGBA8_OES, PALETTE4_RGBA4_OES, PALETTE4_RGB5_A1_OES, "
                + "PALETTE8_RGBA8_OES, PALETTE8_RGBA4_OES, PALETTE4_RGB5_A1_OES texel format, the pixel format must be"
                + " GL_RGBA.");
        }
        setCompressionType(Compression2D.PALETTED);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link PalettedTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link PalettedFormat} The paletted compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public PalettedTexture2D(@Type.TextureType int type, @PalettedFormat int format, @NonNull String name,
                        @NonNull TextureDataReference[] data) throws TextureException {
        super(type, name, data);
        for (int i = 0; i < data.length; ++i) {
            if ((format == OESCompressedPalettedTexture.PALETTE4_RGB8_OES
                || format == OESCompressedPalettedTexture.PALETTE4_R5_G6_B5_OES
                || format == OESCompressedPalettedTexture.PALETTE8_RGB8_OES
                || format == OESCompressedPalettedTexture.PALETTE8_R5_G6_B5_OES)
                && data[i].getPixelFormat() != GLES20.GL_RGB) {
                throw new TextureException("When using PALETTE4_RGB8_OES, PALETTE4_R5_G6_B5_OES, PALETTE8_RGB8_OES,"
                    + " or PALETTE8_R5_G6_B5_OES texel formats, the pixel format must be GL_RGB.");
            } else if ((format == OESCompressedPalettedTexture.PALETTE4_RGBA8_OES
                || format == OESCompressedPalettedTexture.PALETTE4_RGBA4_OES
                || format == OESCompressedPalettedTexture.PALETTE4_RGB5_A1_OES
                || format == OESCompressedPalettedTexture.PALETTE8_RGBA8_OES
                || format == OESCompressedPalettedTexture.PALETTE8_RGBA4_OES
                || format == OESCompressedPalettedTexture.PALETTE4_RGB5_A1_OES)
                && data[i].getPixelFormat() != GLES20.GL_RGBA) {
                throw new TextureException("When using PALETTE4_RGBA8_OES, PALETTE4_RGBA4_OES, PALETTE4_RGB5_A1_OES, "
                    + "PALETTE8_RGBA8_OES, PALETTE8_RGBA4_OES, PALETTE4_RGB5_A1_OES texel format, the pixel format must"
                    + " be GL_RGBA.");
            }
        }
        setCompressionType(Compression2D.PALETTED);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link PalettedTexture2D} with data and settings from the provided {@link PalettedTexture2D}.
     *
     * @param other The other {@link PalettedTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public PalettedTexture2D(@NonNull PalettedTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link PalettedTexture2D}.
     *
     * @param other The other {@link PalettedTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull PalettedTexture2D other) throws TextureException {
        super.setFrom(other);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public PalettedTexture2D clone() {
        try {
            return new PalettedTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @RenderThread
    @Override
    void add() throws TextureException {
        // Verify Paletted textures are supported
        try {
            Capabilities.getInstance().loadExtension(OESCompressedPalettedTexture.name);
        } catch (Capabilities.UnsupportedCapabilityException e) {
            throw new TextureException("Paletted Textures are not supported on this device.");
        }

        // Call super.add()
        super.add();
    }
}
