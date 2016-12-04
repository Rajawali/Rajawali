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
import c.org.rajawali3d.gl.extensions.texture.NVTextureCompressionLATC;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type;

/**
 * LATC formats are designed to reduce the storage requirements and memory bandwidth required for luminance and
 * luminance-alpha textures by a factor of 2-to-1 over conventional uncompressed luminance and luminance-alpha textures
 * with 8-bit components.
 * <p>
 * The compressed signed luminance-alpha format is reasonably suited for storing compressed normal maps.
 * <p>
 * If NV_texture_array is supported, the LATC compressed formats may also be used as the internal formats given to
 * CompressedTexImage3DNV and CompressedTexSubImage3DNV. The restrictions for the <width>, <height>, <xoffset>, and
 * <yoffset> parameters of the CompressedTexSubImage2D function when used with LATC compressed texture formats,
 * described in this extension, also apply to the identically named parameters of CompressedTexSubImage3DNV.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/registry/gles/extensions/NV/NV_texture_compression_latc.txt">
 * NV_texture_compression_latc</a>
 */
public class LATCTexture2D extends CompressedTexture2D {

    /**
     * Constructs a new {@link LATCTexture2D} with the specified name and type.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link NVTextureCompressionLATC.LATCFormat} The LATC compression format.
     * @param name   {@link String} The texture name.
     */
    public LATCTexture2D(@Type.TextureType int type, @NVTextureCompressionLATC.LATCFormat int format,
                         @NonNull String name) throws TextureException {
        super(type, name);
        setCompressionType(Compression2D.LATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link LATCTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link NVTextureCompressionLATC.LATCFormat} The LATC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public LATCTexture2D(@Type.TextureType int type, @NVTextureCompressionLATC.LATCFormat int format,
                         @NonNull String name, @NonNull TextureDataReference data) throws TextureException {
        super(type, name, data);
        //TODO: GL ES 3 does not have luminance or alpha maps, so its requirements will be different
        if ((format == NVTextureCompressionLATC.COMPRESSED_LUMINANCE_LATC1_NV
            || format == NVTextureCompressionLATC.COMPRESSED_SIGNED_LUMINANCE_LATC1_NV)
            && data.getPixelFormat() != GLES20.GL_LUMINANCE) {
            throw new TextureException("When using COMPRESSED_LUMINANCE_LATC1_NV or "
                + "COMPRESSED_SIGNED_LUMINANCE_LATC1_NV texel formats, the pixel format must be GL_LUMINANCE.");
        } else if ((format == NVTextureCompressionLATC.COMPRESSED_LUMINANCE_ALPHA_LATC2_NV
            || format == NVTextureCompressionLATC.COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV)
            && data.getPixelFormat() != GLES20.GL_LUMINANCE_ALPHA) {
            throw new TextureException("When using COMPRESSED_LUMINANCE_ALPHA_LATC2_NV or "
                + "COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV texel formats, the pixel format must be GL_LUMINANCE.");
        }
        setCompressionType(Compression2D.LATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link LATCTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link NVTextureCompressionLATC.LATCFormat} The LATC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public LATCTexture2D(@Type.TextureType int type, @NVTextureCompressionLATC.LATCFormat int format,
                         @NonNull String name, @NonNull TextureDataReference[] data) throws TextureException {
        super(type, name, data);
        for (int i = 0; i < data.length; ++i) {
            //TODO: GL ES 3 does not have luminance or alpha maps, so its requirements will be different
            if ((format == NVTextureCompressionLATC.COMPRESSED_LUMINANCE_LATC1_NV
                || format == NVTextureCompressionLATC.COMPRESSED_SIGNED_LUMINANCE_LATC1_NV)
                && data[i].getPixelFormat() != GLES20.GL_LUMINANCE) {
                throw new TextureException("When using COMPRESSED_LUMINANCE_LATC1_NV or "
                    + "COMPRESSED_SIGNED_LUMINANCE_LATC1_NV texel formats, the pixel format must be GL_LUMINANCE.");
            } else if ((format == NVTextureCompressionLATC.COMPRESSED_LUMINANCE_ALPHA_LATC2_NV
                || format == NVTextureCompressionLATC.COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV)
                && data[i].getPixelFormat() != GLES20.GL_LUMINANCE_ALPHA) {
                throw new TextureException("When using COMPRESSED_LUMINANCE_ALPHA_LATC2_NV or "
                    + "COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV texel formats, the pixel format must be "
                    + "GL_LUMINANCE.");
            }
        }
        setCompressionType(Compression2D.LATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link LATCTexture2D} with data and settings from the provided {@link LATCTexture2D}.
     *
     * @param other The other {@link LATCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public LATCTexture2D(@NonNull LATCTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link LATCTexture2D}.
     *
     * @param other The other {@link LATCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull LATCTexture2D other) throws TextureException {
        super.setFrom(other);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public LATCTexture2D clone() {
        try {
            return new LATCTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @GLThread
    @Override
    void add() throws TextureException {
        // Verify LATC is supported
        try {
            Capabilities.getInstance().loadExtension(NVTextureCompressionLATC.name);
        } catch (Capabilities.UnsupportedCapabilityException e) {
            throw new TextureException("LATC Textures are not supported on this device.");
        }

        // Call super.add()
        super.add();
    }
}
