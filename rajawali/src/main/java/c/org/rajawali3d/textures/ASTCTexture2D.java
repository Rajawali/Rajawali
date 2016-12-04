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
import c.org.rajawali3d.gl.extensions.texture.EXTTextureCompressionS3TC;
import c.org.rajawali3d.gl.extensions.texture.KHRTextureCompressionASTC;
import c.org.rajawali3d.gl.extensions.texture.OESTextureCompressionASTC;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type;

/**
 * Adaptive Scalable Texture Compression (ASTC) is a new texture compression technology that offers unprecendented
 * flexibility, while producing better or comparable results than existing texture compressions at all bit rates. It
 * includes support for 2D and slice-based 3D textures, with low and high dynamic range, at bitrates from below 1
 * bit/pixel up to 8 bits/pixel in fine steps.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * TODO: Does HDR support have any impact on add?
 */
public class ASTCTexture2D extends CompressedTexture2D {

    /**
     * Constructs a new {@link ASTCTexture2D} with the specified name and type.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link OESTextureCompressionASTC.ASTCFormat} The ASTC compression format.
     * @param name   {@link String} The texture name.
     */
    public ASTCTexture2D(@Type.TextureType int type, @OESTextureCompressionASTC.ASTCFormat int format,
                         @NonNull String name) throws TextureException {
        super(type, name);
        setCompressionType(Compression2D.ASTC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link ASTCTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link OESTextureCompressionASTC.ASTCFormat} The ASTC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public ASTCTexture2D(@Type.TextureType int type, @OESTextureCompressionASTC.ASTCFormat int format,
                         @NonNull String name, @NonNull TextureDataReference data) throws TextureException {
        super(type, name, data);
        if (data.getPixelFormat() != GLES20.GL_RGB) {
            throw new TextureException("When using ASTC textures, the pixel format must be GL_RGBA.");
        }
        setCompressionType(Compression2D.ASTC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link ASTCTexture2D} with the provided data.
     *
     * @param type   {@link Type.TextureType} The texture usage type.
     * @param format {@link EXTTextureCompressionS3TC.S3TCFormat} The ASTC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public ASTCTexture2D(@Type.TextureType int type, @OESTextureCompressionASTC.ASTCFormat int format,
                         @NonNull String name, @NonNull TextureDataReference[] data) throws TextureException {
        super(type, name, data);
        for (int i = 0; i < data.length; ++i) {
            if (data[i].getPixelFormat() != GLES20.GL_RGB) {
                throw new TextureException("When using ASTC textures, the pixel format must be GL_RGBA.");
            }
        }
        setCompressionType(Compression2D.ASTC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link ASTCTexture2D} with data and settings from the provided {@link ASTCTexture2D}.
     *
     * @param other The other {@link ASTCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public ASTCTexture2D(@NonNull ASTCTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link ASTCTexture2D}.
     *
     * @param other The other {@link ASTCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull ASTCTexture2D other) throws TextureException {
        super.setFrom(other);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public ASTCTexture2D clone() {
        try {
            return new ASTCTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @GLThread
    @Override
    void add() throws TextureException {
        // Verify ASTC is supported
        try {
            Capabilities.getInstance().loadExtension(OESTextureCompressionASTC.name);
        } catch (Capabilities.UnsupportedCapabilityException e) {
            try {
                Capabilities.getInstance().loadExtension(KHRTextureCompressionASTC.name);
            } catch (Capabilities.UnsupportedCapabilityException e1) {
                throw new TextureException("ASTC Textures are not supported on this device.");
            }
        }

        // Call super.add()
        super.add();
    }
}
