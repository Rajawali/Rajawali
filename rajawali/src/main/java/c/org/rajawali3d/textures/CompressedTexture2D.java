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
import android.opengl.GLES30;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.jcip.annotations.ThreadSafe;

import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.gl.extensions.EXTTextureFilterAnisotropic;
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Compression2D.CompressionType2D;
import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.Type.TextureType;
import c.org.rajawali3d.textures.annotation.Wrap;
import c.org.rajawali3d.textures.annotation.Wrap.WrapType;

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
     * The texture data.
     */
    @Nullable
    private volatile TextureDataReference[] textureData;

    /**
     * Texture2D compression type
     */
    @CompressionType2D
    protected volatile int compressionType;

    /**
     * Bitmap compression format. Use together with {@link CompressionType2D}
     */
    protected volatile int compressionFormat;

    /**
     * Basic no-args constructor used by some subclasses. No initialization is performed.
     */
    protected CompressedTexture2D() throws TextureException {
        super();
    }

    /**
     * Constructs a new {@link SingleTexture2D} with the specified name and type.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
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
     */
    public CompressedTexture2D(@TextureType int type, @NonNull String name, @NonNull TextureDataReference data)
        throws TextureException {
        this(type, name);
        setTextureData(new TextureDataReference[]{data});
    }

    /**
     * Constructs a new {@link CompressedTexture2D} with the provided data.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
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
     */
    public CompressedTexture2D(@NonNull CompressedTexture2D other) throws TextureException {
        this();
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link CompressedTexture2D}.
     *
     * @param other The other {@link CompressedTexture2D}.
     */
    public void setFrom(@NonNull CompressedTexture2D other) throws TextureException {
        final TextureDataReference[] data = other.getTextureData();
        if (data != null) {
            super.setFrom(other);
            setTextureData(data);
            setCompressionType(other.getCompressionType());
            setCompressionFormat(other.getCompressionFormat());
        } else {
            throw new TextureException("Texture data was null!");
        }
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

    /**
     * Retrieves the compression format.
     *
     * @return {@code int} The compression format, such as {@link GLES30#GL_COMPRESSED_RGB8_ETC2}.
     */
    public int getCompressionFormat() {
        return compressionFormat;
    }

    /**
     * Sets the compression format.
     *
     * @param compressionFormat {@code int} The compression format, such as {@link GLES30#GL_COMPRESSED_RGB8_ETC2}.
     */
    public void setCompressionFormat(int compressionFormat) {
        this.compressionFormat = compressionFormat;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
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

        @Filter.FilterType final int filterType = getFilterType();
        @WrapType final int wrapType = getWrapType();

        // Generate a texture id
        int[] genTextureNames = new int[1];
        GLES20.glGenTextures(1, genTextureNames, 0);
        int textureId = genTextureNames[0];

        if (textureId > 0) {

            // Handle minification filtering
            if (isMipmaped()) {
                switch (filterType) {
                    case Filter.NEAREST:
                        GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_NEAREST_MIPMAP_NEAREST);
                        break;
                    case Filter.BILINEAR:
                        GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR_MIPMAP_NEAREST);
                        break;
                    case Filter.TRILINEAR:
                        GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR_MIPMAP_LINEAR);
                        break;
                    default:
                        throw new TextureException("Unknown texture filtering mode: " + filterType);
                }
            } else {
                switch (filterType) {
                    case Filter.NEAREST:
                        GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_NEAREST);
                        break;
                    case Filter.BILINEAR:
                        GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR);
                        break;
                    case Filter.TRILINEAR:
                        RajLog.e("Trilinear filtering requires the use of mipmaps which are not enabled for this "
                            + "texture. Falling back to bilinear filtering.");
                        GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR);
                        break;
                    default:
                        throw new TextureException("Unknown texture filtering mode: " + filterType);
                }
            }

            // Handle magnification filtering
            if (filterType == Filter.BILINEAR || filterType == Filter.TRILINEAR) {
                GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            } else {
                GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            }

            // Handle anisotropy if needed. We don't check if it is supported here because setting it to anything
            // other than 1.0 would have required the check.
            if (getMaxAnisotropy() > 1.0) {
                GLES20.glTexParameterf(getTextureTarget(), EXTTextureFilterAnisotropic.TEXTURE_MAX_ANISOTROPY_EXT,
                    getMaxAnisotropy());
            }

            // Handle s coordinate wrapping
            int wrap = GLES20.GL_REPEAT;
            if ((wrapType & Wrap.CLAMP_S) != 0) {
                wrap = GLES20.GL_CLAMP_TO_EDGE;
            } else if ((wrapType & Wrap.MIRRORED_REPEAT_S) != 0) {
                wrap = GLES20.GL_MIRRORED_REPEAT;
            }
            GLES20.glTexParameteri(getTextureTarget(), GLES20.GL_TEXTURE_WRAP_S, wrap);

            // Handle t coordinate wrapping
            wrap = GLES20.GL_REPEAT;
            if ((wrapType & Wrap.CLAMP_T) != 0) {
                wrap = GLES20.GL_CLAMP_TO_EDGE;
            } else if ((wrapType & Wrap.MIRRORED_REPEAT_T) != 0) {
                wrap = GLES20.GL_MIRRORED_REPEAT;
            }
            GLES20.glTexParameteri(getTextureTarget(), GLES20.GL_TEXTURE_WRAP_T, wrap);

            int w = getWidth();
            int h = getHeight();
            for (int i = 0; i < dataReferences.length; i++) {
                if (dataReferences[i].hasBuffer()) {
                    if (getWidth() == 0 || getHeight() == 0) {
                        throw new TextureException(
                            "Could not create ByteBuffer texture. One or more of the following properties haven't "
                                + "been set: width or height format");
                    }
                    GLES20.glCompressedTexImage2D(getTextureTarget(), i, compressionFormat, w, h, 0,
                        dataReferences[i].getByteBuffer().capacity(), dataReferences[i].getByteBuffer());
                    w = w > 1 ? w / 2 : 1;
                    h = h > 1 ? h / 2 : 1;
                }
            }
            setTextureId(textureId);
        } else {
            throw new TextureException("Couldn't generate a texture name.");
        }

        if (willRecycle()) {
            setTextureData((TextureDataReference[]) null);
        }

        GLES20.glBindTexture(getTextureTarget(), 0);
    }

    void replace() throws TextureException {
        if (textureData == null || textureData.length == 0)
            throw new TextureException("Texture2D could not be replaced because there is no ByteBuffer set.");

        if (getWidth() == 0 || getHeight() == 0)
            throw new TextureException(
                "Could not update ByteBuffer texture. One or more of the following properties haven't been set: width or height");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
        int w = getWidth(), h = getHeight();
        for (int i = 0; i < textureData.length; i++) {
            GLES20.glCompressedTexSubImage2D(GLES20.GL_TEXTURE_2D, i, 0, 0, w, h, compressionFormat,
                                             textureData[i].getByteBuffer().capacity(), textureData[i].getByteBuffer());
            w = w > 1 ? w / 2 : 1;
            h = h > 1 ? h / 2 : 1;
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    void reset() throws TextureException {
        if (textureData != null) {
            for (int i = 0; i < textureData.length; i++) {
                if (textureData[i] != null) {
                    textureData[i].getByteBuffer().limit(0);
                }
            }
        }
    }
}
