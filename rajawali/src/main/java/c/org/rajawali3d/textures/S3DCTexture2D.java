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
import c.org.rajawali3d.gl.extensions.texture.EXTTextureCompressionDXT1;
import c.org.rajawali3d.gl.extensions.texture.EXTTextureCompressionS3TC;
import c.org.rajawali3d.gl.extensions.texture.NVTextureCompressionS3TC;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type;

/**
 * S3 Texture Compression is a lossy, fixed-rate, texture compression format. This style of compression makes S3TC an
 * ideal texture compression format for textures used in hardware-accelerated 3D computer graphics. There are at least 5
 * different variations of the S3TC format (including DXT1 through DXT5), though DXT2 and DXT4 are not typically
 * supported on mobile devices.
 *
 * DXT1 is the smallest mode of S3TC compression; it converts each block of 16 pixels into 64 bits. Additionally, it is
 * composed of two different 16-bit RGB 5:8:5 color values and a 4x4 2-bit lookup table. DXT1 does not support alpha
 * channels.
 *
 * DXT3 converts each block of 16 pixels into 128 bits and is composed of 64 bits of alpha channel data and 64 bits of
 * color data. DXT3 is a good format choice for images or textures with sharp alpha transitions (opaque versus
 * translucent).
 *
 * DXT5 converts each block of 16 pixels into 128 bits and is composed of 64 bits of alpha channel data and 64 bits of
 * color data. DXT5 is a good format choice for images or textures with gradient alpha transitions.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class S3DCTexture2D extends CompressedTexture2D {

    /**
     * Constructs a new {@link S3DCTexture2D} with the specified name and type.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link EXTTextureCompressionS3TC.S3TCFormat} The S3TC compression format.
     * @param name   {@link String} The texture name.
     */
    public S3DCTexture2D(@Type.TextureType int type, @EXTTextureCompressionS3TC.S3TCFormat int format,
                         @NonNull String name) throws TextureException {
        super(type, name);
        setCompressionType(Compression2D.S3TC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link S3DCTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link EXTTextureCompressionS3TC.S3TCFormat} The S3TC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public S3DCTexture2D(@Type.TextureType int type, @EXTTextureCompressionS3TC.S3TCFormat int format,
                         @NonNull String name, @NonNull TextureDataReference data) throws TextureException {
        super(type, name, data);
        if (format == EXTTextureCompressionS3TC.COMPRESSED_RGB_S3TC_DXT1_EXT
            && data.getPixelFormat() != GLES20.GL_RGB) {
            throw new TextureException("When using COMPRESSED_RGB_S3TC_DXT1_EXT texel format, the pixel format must be "
                + "GL_RGB.");
        } else if ((format == EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT1_EXT
            || format == EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT3_EXT
            || format == EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT5_EXT)
            && data.getPixelFormat() != GLES20.GL_RGBA) {
            throw new TextureException("When using COMPRESSED_RGBA_S3TC_DXT1_EXT or COMPRESSED_RGBA_S3TC_DXT3_EXT "
                + "COMPRESSED_RGBA_S3TC_DXT5_EXT texel format, the pixel format must be GL_RGBA.");
        }
        setCompressionType(Compression2D.S3TC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link S3DCTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link EXTTextureCompressionS3TC.S3TCFormat} The S3TC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public S3DCTexture2D(@Type.TextureType int type, @EXTTextureCompressionS3TC.S3TCFormat int format,
                         @NonNull String name, @NonNull TextureDataReference[] data) throws TextureException {
        super(type, name, data);
        for (int i = 0; i < data.length; ++i) {
            if (format == EXTTextureCompressionS3TC.COMPRESSED_RGB_S3TC_DXT1_EXT
                && data[i].getPixelFormat() != GLES20.GL_RGB) {
                throw new TextureException("When using COMPRESSED_RGB_S3TC_DXT1_EXT texel format, the pixel format must"
                    + " be GL_RGB.");
            } else if ((format == EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT1_EXT
                || format == EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT3_EXT
                || format == EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT5_EXT)
                && data[i].getPixelFormat() != GLES20.GL_RGBA) {
                throw new TextureException("When using COMPRESSED_RGBA_S3TC_DXT1_EXT or COMPRESSED_RGBA_S3TC_DXT3_EXT "
                    + "COMPRESSED_RGBA_S3TC_DXT5_EXT texel format, the pixel format must be GL_RGBA.");
            }
        }
        setCompressionType(Compression2D.S3TC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link S3DCTexture2D} with data and settings from the provided {@link S3DCTexture2D}.
     *
     * @param other The other {@link S3DCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public S3DCTexture2D(@NonNull S3DCTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link S3DCTexture2D}.
     *
     * @param other The other {@link S3DCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull S3DCTexture2D other) throws TextureException {
        super.setFrom(other);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public S3DCTexture2D clone() {
        try {
            return new S3DCTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @RenderThread
    @Override
    void add() throws TextureException {
        // Verify S3TC is supported
        try {
            // First we check the broad EXT S3TC extension
            Capabilities.getInstance().loadExtension(EXTTextureCompressionS3TC.name);
        } catch (Capabilities.UnsupportedCapabilityException e) {
            try {
                // Next we check for nVidia's version of S3TC
                Capabilities.getInstance().loadExtension(NVTextureCompressionS3TC.name);
            } catch (Capabilities.UnsupportedCapabilityException e1) {
                if (getTexelFormat() == EXTTextureCompressionS3TC.COMPRESSED_RGB_S3TC_DXT1_EXT
                    || getTexelFormat() == EXTTextureCompressionS3TC.COMPRESSED_RGBA_S3TC_DXT1_EXT) {
                    try {
                        // Finally, if this texture uses DXT1, we check if only DXT1 is supported
                        Capabilities.getInstance().loadExtension(EXTTextureCompressionDXT1.name);
                    } catch (Capabilities.UnsupportedCapabilityException e2) {
                        throw new TextureException("ATC Textures are not supported on this device.");
                    }
                } else {
                    throw new TextureException("ATC Textures are not supported on this device.");
                }
            }
        }

        // Call super.add()
        super.add();
    }
}
