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
import c.org.rajawali3d.textures.annotation.Compression2D;
import c.org.rajawali3d.textures.annotation.Compression2D.CompressionType2D;
import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.Filter.FilterType;
import c.org.rajawali3d.textures.annotation.Type.TextureType;
import c.org.rajawali3d.textures.annotation.Wrap;
import c.org.rajawali3d.textures.annotation.Wrap.WrapType;
import net.jcip.annotations.ThreadSafe;

/**
 * This class is used to specify common functions of a compressed 2D texture. Subclasses are expected to be thread
 * safe.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
@ThreadSafe
@SuppressWarnings("WeakerAccess")
public abstract class CompressedTexture2D extends SingleTexture2D {

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
        setTextureData(data);
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
        final TextureDataReference[] data = other.getTextureDataArray();
        if (data != null) {
            super.setFrom(other);
            setTextureData(data);
            setCompressionType(other.getCompressionType());
            setCompressionFormat(other.getCompressionFormat());
        } else {
            throw new TextureException("Texture data was null!");
        }
    }

    @Override
    public void setTextureData(@Nullable TextureDataReference data) {
        setTextureData(data != null ? new TextureDataReference[]{data} : null);
    }

    /**
     * Sets the data used by this {@link CompressedTexture2D} from the provided {@link TextureDataReference} array.
     * The data references will have their reference count incremented and any existing data reference will be released.
     *
     * @param data The new {@link TextureDataReference} array to use.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void setTextureData(@Nullable TextureDataReference[] data) {
        // Save a stack reference to the old data
        final TextureDataReference[] oldData = textureData;

        if (data != null) {
            for (int i = 0, j = data.length; i < j; ++i) {
                // Save and increment reference count of new data
                if (data[i] != null) {
                    data[i].holdReference();
                    if (getWidth() == 0 && getHeight() == 0) {
                        setWidth(data[i].getWidth());
                        setHeight(data[i].getHeight());
                    }
                }
            }
        }

        textureData = data;

        if (oldData != null) {
            for (int i = 0, j = oldData.length; i < j; ++i) {
                // Release any existing reference
                if (oldData[i] != null) {
                    oldData[i].recycle();
                }
            }
        }
    }

    @Override
    @Nullable
    public TextureDataReference getTextureData() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Compressed texture data must be retrieved as an array.");
    }

    /**
     * Retrieves the current {@link TextureDataReference} array used by this {@link CompressedTexture2D}.
     *
     * @return The current {@link TextureDataReference} array.
     */
    @Nullable
    public TextureDataReference[] getTextureDataArray() {
        return textureData;
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

    void add() throws TextureException {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        if (textureId > 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            @FilterType final int filterType = getFilterType();
            @WrapType final int wrapType = getWrapType();

            if (filterType == Filter.BILINEAR)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            else
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

            if (filterType == Filter.BILINEAR)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            else
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            if (wrapType == (Wrap.REPEAT_S | Wrap.REPEAT_T | Wrap.REPEAT_R)) {
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            } else {
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            }
            if ((textureData != null && textureData.length == 0) || textureData == null) {
                GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, 0, compressionFormat, getWidth(), getHeight(), 0, 0, null);
            } else {
                int w = getWidth(), h = getHeight();
                for (int i = 0; i < textureData.length; i++) {
                    GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, i, compressionFormat, w, h, 0,
                        textureData[i].getByteBuffer().capacity(), textureData[i].getByteBuffer());
                    w = w > 1 ? w / 2 : 1;
                    h = h > 1 ? h / 2 : 1;
                }
            }
            setTextureId(textureId);
        } else {
            throw new TextureException("Couldn't generate a texture name.");
        }

        for (int i = 0; i < textureData.length; i++) {
            if (textureData[i] != null) {
                textureData[i].getByteBuffer().limit(0);
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    void remove() throws TextureException {
        GLES20.glDeleteTextures(1, new int[]{getTextureId()}, 0);
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
