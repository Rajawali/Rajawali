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

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import net.jcip.annotations.ThreadSafe;

import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.gl.extensions.OESCompressedETC1RGB8;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type.TextureType;

/**
 * ETC1 compressed texture. This is the most widely supported compression format but does not allow for transparency.
 * In
 * spite of this, it is still an extremely import and useful tool for increasing performance of your application, and
 * alpha masking may be used in combination to provide an all or nothing alpha channel on the texture, or if a smooth
 * alpha is truly needed, creating blending/packing of textures could provide this channel in another texture.
 * <p>
 * If {@link Bitmap}s are provided in the {@link TextureDataReference}s, they will be used as fallback textures in the
 * event of an error decoding the ETC1 data.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
@SuppressWarnings("WeakerAccess")
@ThreadSafe
public class ETC1Texture extends CompressedTexture2D {

    /**
     * Constructs a new {@link ETC1Texture} with the specified name and type.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     */
    public ETC1Texture(@TextureType int type, @NonNull String name) throws TextureException {
        super(type, name);
        setCompressionType(Compression2D.ETC1);
        setTexelFormat(OESCompressedETC1RGB8.ETC1_RGB8_OES);
    }

    /**
     * Constructs a new {@link ETC1Texture} with the provided data.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public ETC1Texture(@TextureType int type, @NonNull String name, @NonNull TextureDataReference data)
        throws TextureException {
        super(type, name, data);
        setCompressionType(Compression2D.ETC1);
        setTexelFormat(OESCompressedETC1RGB8.ETC1_RGB8_OES);
    }

    /**
     * Constructs a new {@link ETC1Texture} with the provided data.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public ETC1Texture(@TextureType int type, @NonNull String name, @NonNull TextureDataReference[] data)
        throws TextureException {
        super(type, name, data);
        setCompressionType(Compression2D.ETC1);
        setTexelFormat(OESCompressedETC1RGB8.ETC1_RGB8_OES);
    }

    /**
     * Constructs a new {@link ETC1Texture} with data and settings from the provided {@link ETC1Texture}.
     *
     * @param other The other {@link ETC1Texture}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public ETC1Texture(@NonNull ETC1Texture other) throws TextureException {
        super();
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link ETC1Texture}.
     *
     * @param other The other {@link ETC1Texture}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull ETC1Texture other) throws TextureException {
        super.setFrom(other);
        setCompressionType(other.getCompressionType());
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public ETC1Texture clone() {
        try {
            return new ETC1Texture(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @Override
    void add() throws TextureException {
        // Verify ETC1 is supported

        // If unsupported and fallbacks exist, use them

        // Else throw exception

        // Call super.add()

    }
}
