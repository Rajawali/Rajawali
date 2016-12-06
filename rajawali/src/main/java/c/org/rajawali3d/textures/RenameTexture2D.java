/**
 * Copyright 2013 Dennis Ippel
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package c.org.rajawali3d.textures;

import android.opengl.GLES20;
import android.support.annotation.NonNull;

import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.texture.IMGTextureCompressionPVRTC;
import c.org.rajawali3d.gl.extensions.texture.IMGTextureCompressionPVRTC.PVRTCFormat;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type.TextureType;

/**
 * PowerVR Texture Compression is a lossy, fixed-rate texture compression format utilized primarily in Imagination
 * Technologies’ PowerVR* MBX, SGX, and Rogue technologies. It is currently being employed in all iPhone*, iPod*, and
 * iPad* devices as the standard compression format. Unlike ETC and S3TC, PVRTC is not block-based but rather involves
 * the bilinear upscaling and low-precision blending of two low-resolution images. In addition to the unique process of
 * compression by the PVRTC format, it also supports RGBA (alpha channel supported) for both the 2-bpp (2 bits per
 * pixel) and 4-bpp (4 bits per pixel) options.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/IMG/IMG_texture_compression_pvrtc.txt">
 * IMG_texture_compression_pvrtc</a>
 */
public class RenameTexture2D extends CompressedTexture2D {

    /**
     * Constructs a new {@link RenameTexture2D} with the specified name and type.
     *
     * @param type   {@link TextureType} The texture usage type.
     * @param format {@link PVRTCFormat} The PVRTC compression format.
     * @param name   {@link String} The texture name.
     */
    public RenameTexture2D(@TextureType int type, @PVRTCFormat int format, @NonNull String name)
        throws TextureException {
        super(type, name);
        setCompressionType(Compression2D.ATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link RenameTexture2D} with the provided data.
     *
     * @param type   {@link TextureType} The texture usage type.
     * @param format {@link PVRTCFormat} The PVRTC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public RenameTexture2D(@TextureType int type, @PVRTCFormat int format, @NonNull String name,
                           @NonNull TextureDataReference data) throws TextureException {
        super(type, name, data);
        if ((format == IMGTextureCompressionPVRTC.COMPRESSED_RGB_PVRTC_2BPPV1_IMG
            || format == IMGTextureCompressionPVRTC.COMPRESSED_RGB_PVRTC_4BPPV1_IMG)
            && data.getPixelFormat() != GLES20.GL_RGB) {
            throw new TextureException("When using COMPRESSED_RGB_PVRTC_2BPPV1_IMG or COMPRESSED_RGB_PVRTC_4BPPV1_IMG "
                + "texel formats, the pixel format must be GL_RGB.");
        } else if ((format == IMGTextureCompressionPVRTC.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG
            || format == IMGTextureCompressionPVRTC.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG)
            && data.getPixelFormat() != GLES20.GL_RGBA) {
            throw new TextureException("When using COMPRESSED_RGBA_PVRTC_2BPPV1_IMG or COMPRESSED_RGBA_PVRTC_4BPPV1_IMG"
                + " texel formats, the pixel format must be GL_RGBA.");
        }
        setCompressionType(Compression2D.ATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link RenameTexture2D} with the provided data.
     *
     * @param type   {@link TextureType} The texture usage type.
     * @param format {@link PVRTCFormat} The PVRTC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public RenameTexture2D(@TextureType int type, @PVRTCFormat int format, @NonNull String name,
                           @NonNull TextureDataReference[] data) throws TextureException {
        super(type, name, data);
        for (int i = 0; i < data.length; ++i) {
            if ((format == IMGTextureCompressionPVRTC.COMPRESSED_RGB_PVRTC_2BPPV1_IMG
                || format == IMGTextureCompressionPVRTC.COMPRESSED_RGB_PVRTC_4BPPV1_IMG)
                && data[i].getPixelFormat() != GLES20.GL_RGB) {
                throw new TextureException("When using COMPRESSED_RGB_PVRTC_2BPPV1_IMG or COMPRESSED_RGB_PVRTC_4BPPV1_IMG "
                    + "texel formats, the pixel format must be GL_RGB.");
            } else if ((format == IMGTextureCompressionPVRTC.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG
                || format == IMGTextureCompressionPVRTC.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG)
                && data[i].getPixelFormat() != GLES20.GL_RGBA) {
                throw new TextureException("When using COMPRESSED_RGBA_PVRTC_2BPPV1_IMG or COMPRESSED_RGBA_PVRTC_4BPPV1_IMG"
                    + " texel formats, the pixel format must be GL_RGBA.");
            }
        }
        setCompressionType(Compression2D.ATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link RenameTexture2D} with data and settings from the provided {@link RenameTexture2D}.
     *
     * @param other The other {@link RenameTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public RenameTexture2D(@NonNull RenameTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link RenameTexture2D}.
     *
     * @param other The other {@link RenameTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull RenameTexture2D other) throws TextureException {
        super.setFrom(other);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public RenameTexture2D clone() {
        try {
            return new RenameTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @GLThread
    @Override
    void add() throws TextureException {
        // Verify PVRTC is supported
        try {
            Capabilities.getInstance().loadExtension(IMGTextureCompressionPVRTC.name);
        } catch (Capabilities.UnsupportedCapabilityException e) {
            throw new TextureException("ATC Textures are not supported on this device.");
        }

        // Call super.add()
        super.add();
    }
}
