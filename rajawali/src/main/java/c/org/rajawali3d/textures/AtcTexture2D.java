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

import android.support.annotation.NonNull;

import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.AMDCompressedATCTexture;
import c.org.rajawali3d.gl.extensions.AMDCompressedATCTexture.ATCFormat;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Type.TextureType;

/**
 * ATC is AMD's proprietary compression algorithm
 * for compressing textures for handheld devices to save on power consumption, memory footprint and bandwidth.
 * <p>
 * Three compression formats are introduced:
 * <p>
 * - A compression format for RGB textures.
 * - A compression format for RGBA textures using explicit alpha encoding.
 * - A compression format for RGBA textures using interpolated alpha encoding.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 * @see <a href="https://www.khronos.org/registry/gles/extensions/AMD/AMD_compressed_ATC_texture.txt">
 * AMD_compressed_ATC_texture</a>
 */
public class ATCTexture2D extends CompressedTexture2D {

    /**
     * Constructs a new {@link ETC1Texture} with the specified name and type.
     *
     * @param type   {@link TextureType} The texture usage type.
     * @param format {@link ATCFormat} The ATC compression format.
     * @param name   {@link String} The texture name.
     */
    public ATCTexture2D(@TextureType int type, @ATCFormat int format, @NonNull String name)
        throws TextureException {
        super(type, name);
        setCompressionType(Compression2D.ATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link ETC1Texture} with the provided data.
     *
     * @param type   {@link TextureType} The texture usage type.
     * @param format {@link ATCFormat} The ATC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public ATCTexture2D(@TextureType int type, @ATCFormat int format, @NonNull String name,
                        @NonNull TextureDataReference data) throws TextureException {
        super(type, name, data);
        setCompressionType(Compression2D.ATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link ETC1Texture} with the provided data.
     *
     * @param type   {@link TextureType} The texture usage type.
     * @param format {@link ATCFormat} The ATC compression format.
     * @param name   {@link String} The texture name.
     * @param data   {@link TextureDataReference} The texture data.
     */
    public ATCTexture2D(@TextureType int type, @ATCFormat int format, @NonNull String name,
                        @NonNull TextureDataReference[] data) throws TextureException {
        super(type, name, data);
        setCompressionType(Compression2D.ATC);
        setTexelFormat(format);
    }

    /**
     * Constructs a new {@link ATCTexture2D} with data and settings from the provided {@link ATCTexture2D}.
     *
     * @param other The other {@link ATCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public ATCTexture2D(ATCTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link ATCTexture2D}.
     *
     * @param other The other {@link ATCTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull ATCTexture2D other) throws TextureException {
        super.setFrom(other);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public ATCTexture2D clone() {
        try {
            return new ATCTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @GLThread
    @Override
    void add() throws TextureException {
        // Verify ATC is supported
        try {
            Capabilities.getInstance().loadExtension(AMDCompressedATCTexture.name);
        } catch (Capabilities.UnsupportedCapabilityException e) {
            throw new TextureException("ATC Textures are not supported on this device.");
        }

        // Call super.add()
        super.add();
    }
}
