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
import android.util.Log;

import net.jcip.annotations.ThreadSafe;

import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Compression2D.CompressionType2D;
import c.org.rajawali3d.textures.annotation.Type.TextureType;

/**
 * This class is used to specify common functions of a compressed 2D texture. Subclasses are expected to be thread
 * safe.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
@ThreadSafe
@SuppressWarnings("WeakerAccess")
public abstract class CompressedTexture2D extends MultiTexture2D {

    /**
     * 2D Texture compression type
     */
    @CompressionType2D
    protected volatile int compressionType;


    /**
     * Basic no-args constructor used by some subclasses. No initialization is performed.
     */
    protected CompressedTexture2D() {
        super();
    }

    /**
     * Constructs a new {@link SingleTexture2D} with the specified name and type.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture creation process.
     */
    public CompressedTexture2D(@TextureType int type, @NonNull String name) throws TextureException {
        super(type, name);
    }

    /**
     * Constructs a new {@link CompressedTexture2D} with the provided data.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture creation process.
     */
    public CompressedTexture2D(@TextureType int type, @NonNull String name, @NonNull TextureDataReference data)
            throws TextureException {
        this(type, name);
        setTextureData(new TextureDataReference[]{ data });
    }

    /**
     * Constructs a new {@link CompressedTexture2D} with the provided data.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture creation process.
     */
    public CompressedTexture2D(@TextureType int type, @NonNull String name, @NonNull TextureDataReference[] data)
            throws TextureException {
        this(type, name);
        setTextureData(data);
    }

    /**
     * Constructs a new {@link CompressedTexture2D} with data and settings from the provided
     * {@link CompressedTexture2D}.
     *
     * @param other The other {@link CompressedTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public CompressedTexture2D(@NonNull CompressedTexture2D other) throws TextureException {
        this();
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link CompressedTexture2D}.
     *
     * @param other The other {@link CompressedTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull CompressedTexture2D other) throws TextureException {
        super.setFrom(other);
        setCompressionType(other.getCompressionType());
    }

    /**
     * Retrieves the compression type.
     *
     * @return {@link CompressionType2D} The compression type, such as {@link Compression2D#ETC1}
     */
    @CompressionType2D
    public int getCompressionType() {
        return compressionType;
    }

    /**
     * Sets the compression type.
     *
     * @param compressionType {@link CompressionType2D} The compression type, such as {@link Compression2D#ETC1}
     */
    public void setCompressionType(@CompressionType2D int compressionType) {
        this.compressionType = compressionType;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @GLThread
    @Override
    void add() throws TextureException {
        final TextureDataReference[] dataReferences = getTextureData();

        if (dataReferences == null) {
            throw new TextureException("Texture data was null!");
        }

        for (int i = 0; i < dataReferences.length; ++i) {
            if (dataReferences[i] == null || dataReferences[i].isDestroyed()
                || (dataReferences[i].hasBuffer() && dataReferences[i].getByteBuffer().limit() == 0
                    && !dataReferences[i].hasBitmap())) {
                throw new TextureException("Texture could not be added because there is no valid data set.");
            }
        }

        // Generate a texture id
        int textureId = generateTextureId();

        if (textureId > 0) {

            // Handle minification filtering
            applyMinificationFilter();

            // Handle magnification filtering
            applyMagnificationFilter();

            // Handle anisotropy if needed.
            applyAnisotropy();

            // Handle s coordinate wrapping
            applySWrapping();

            // Handle t coordinate wrapping
            applyTWrapping();

            int width;
            int height;
            int lastWidth = 0;
            int lastHeight = 0;

            for (int i = 0, j = isMipmaped() ? dataReferences.length : 1; i < j; i++) {
                width = dataReferences[i].getWidth();
                height = dataReferences[i].getHeight();
                if ((lastWidth > 0 && (lastWidth / 2 != width)) || (lastHeight > 0 && (lastHeight / 2 != height))) {
                    throw new TextureException("mipmap chain must be sized as multiples of two in each direction.");
                }
                if (dataReferences[i].hasBuffer()) {
                    if (width == 0 || height == 0) {
                        throw new TextureException(
                                "Could not create ByteBuffer texture. One or more of the following properties haven't "
                                + "been set: width or height format");
                    }
                    GLES20.glCompressedTexImage2D(getTextureTarget(), i, getTexelFormat(), width, height, 0,
                                                  dataReferences[i].getByteBuffer().capacity(),
                                                  dataReferences[i].getByteBuffer());
                    lastWidth = width;
                    lastHeight = height;
                }
                // TODO: Check if fallback texture is necessary
            }
            setTextureId(textureId);
        } else {
            throw new TextureException("Couldn't generate a texture name.");
        }

        if (willRecycle()) {
            setTextureData(null);
        }

        GLES20.glBindTexture(getTextureTarget(), 0);
    }

    @GLThread
    @Override
    void replace() throws TextureException {
        final TextureDataReference[] dataReferences = getTextureData();

        if (dataReferences == null) {
            throw new TextureException("Texture data was null!");
        }

        for (int i = 0; i < dataReferences.length; ++i) {
            // NOTE: Unlike other textures, replacing with a bitmap here is not allowed
            if (dataReferences[i] == null || dataReferences[i].isDestroyed() || !dataReferences[i].hasBuffer()
                || (dataReferences[i].getByteBuffer().limit() == 0)) {
                throw new TextureException("Texture could not be added because there is no valid data set.");
            }
            // TODO: This needs to take into account mipmap dimensions
            if (dataReferences[0].getWidth() != getWidth() || dataReferences[0].getHeight() != getHeight()) {
                throw new TextureException(
                        "Texture could not be updated because the texture size is different from the original.");
            }
        }

        GLES20.glBindTexture(getTextureTarget(), getTextureId());

        int width;
        int height;
        int lastWidth = 0;
        int lastHeight = 0;

        for (int i = 0, j = isMipmaped() ? dataReferences.length : 1; i < j; i++) {
            width = dataReferences[i].getWidth();
            height = dataReferences[i].getHeight();
            if ((lastWidth > 0 && (lastWidth / 2 != width)) || (lastHeight > 0 && (lastHeight / 2 != height))) {
                throw new TextureException("mipmap chain must be sized as multiples of two in each direction.");
            } else {
                Log.d("DIMENSIONS", "Passing with condition width conditions: " + (lastWidth > 0) + "/"
                                    + (lastWidth / 2 != width));
                Log.d("DIMENSIONS", "Passing with condition height conditions: " + (lastHeight > 0) + "/"
                                    + (lastHeight / 2 != height));
            }
            GLES20.glCompressedTexImage2D(getTextureTarget(), i, getTexelFormat(), width, height, 0,
                                          dataReferences[i].getByteBuffer().capacity(),
                                          dataReferences[i].getByteBuffer());
            lastWidth = width;
            lastHeight = height;
        }
        GLES20.glBindTexture(getTextureTarget(), 0);
    }
}
