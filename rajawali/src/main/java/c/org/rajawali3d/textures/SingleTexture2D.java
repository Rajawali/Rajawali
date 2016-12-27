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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.jcip.annotations.ThreadSafe;

import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.PixelFormat;
import c.org.rajawali3d.textures.annotation.Type.TextureType;
import c.org.rajawali3d.textures.annotation.Wrap;

import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.surface.gles.extensions.EXTTextureFilterAnisotropic;

/**
 * This class is used to specify common functions of a single 2D texture. Subclasses are expected to be thread safe.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
@SuppressWarnings("WeakerAccess")
@ThreadSafe
public abstract class SingleTexture2D extends BaseTexture {

    /**
     * The texture data.
     */
    @Nullable
    private volatile TextureDataReference textureData;

    /**
     * Basic no-args constructor used by some subclasses. No initialization is performed.
     */
    protected SingleTexture2D() {
        super();
    }

    /**
     * Constructs a new {@link SingleTexture2D} with the specified name and type.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     */
    public SingleTexture2D(@TextureType int type, @NonNull String name) {
        super(type, name);
    }

    /**
     * Constructs a new {@link SingleTexture2D} with data provided by the Android resource id. The texture name is
     * set by
     * querying Android for the resource name.
     *
     * @param context    {@link Context} The application context.
     * @param type       {@link TextureType} The texture usage type.
     * @param resourceId {@code int} The Android resource id to load from.
     */
    public SingleTexture2D(@NonNull Context context, @TextureType int type, @DrawableRes int resourceId) {
        this(type, context.getResources().getResourceName(resourceId));
        setTextureDataFromResourceId(context, resourceId);
    }

    /**
     * Constructs a new {@link SingleTexture2D} with the provided data.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public SingleTexture2D(@TextureType int type, @NonNull String name, @NonNull TextureDataReference data) {
        this(type, name);
        setTextureData(data);
    }

    /**
     * Constructs a new {@link SingleTexture2D} with data and settings from the provided {@link SingleTexture2D}.
     *
     * @param other The other {@link SingleTexture2D}.
     */
    public SingleTexture2D(@NonNull SingleTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link SingleTexture2D}.
     *
     * @param other The other {@link SingleTexture2D}.
     */
    public void setFrom(@NonNull SingleTexture2D other) throws TextureException {
        final TextureDataReference data = other.getTextureData();
        if (data != null) {
            super.setFrom(other);
            setTextureData(other.getTextureData());
        } else {
            throw new TextureException("Texture data was null!");
        }
    }

    /**
     * Sets the data used by this {@link SingleTexture2D} from an Android resource id. This will create a new
     * {@link TextureDataReference} and set it as the active data. Do not use this method if you wish to use the
     * texture as a Luminance texture as it will assume a {@link GLES20#GL_RGB} or {@link GLES20#GL_RGBA}
     * {@link PixelFormat}.
     *
     * @param context    {@link Context} The Android application context.
     * @param resourceId {@code int} The Android resource id to load from.
     *
     * @return The new {@link TextureDataReference} which was created.
     */
    @NonNull
    public TextureDataReference setTextureDataFromResourceId(@NonNull Context context, @DrawableRes int resourceId) {
        // Prevent the bitmap from being scaled as it is decoded
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        // Decode the bitmap
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        final TextureDataReference reference = new TextureDataReference(bitmap, null,
            bitmap.getConfig().equals(Config.RGB_565)
                ? GLES20.GL_RGB : GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, bitmap.getWidth(),
            bitmap.getHeight());
        setTextureData(reference);
        return reference;
    }

    /**
     * Sets the data used by this {@link SingleTexture2D} from the provided {@link TextureDataReference}. The data
     * reference will have its reference count incremented and any existing data reference will be released.
     *
     * @param data The new {@link TextureDataReference} to use.
     */
    public void setTextureData(@NonNull TextureDataReference data) {
        // Save a stack reference to the old data
        final TextureDataReference oldData = this.textureData;

        // Save and increment reference count of new data
        data.holdReference();
        this.textureData = data;

        // Release any existing reference
        if (oldData != null) {
            oldData.recycle();
        }
    }

    /**
     * Retrieves the current {@link TextureDataReference} used by this {@link SingleTexture2D}.
     *
     * @return The current {@link TextureDataReference}.
     */
    @Nullable
    public TextureDataReference getTextureData() {
        return textureData;
    }

    @Override
    void add() throws TextureException {
        // Check if there is valid data
        final TextureDataReference textureData = this.textureData;

        if (textureData == null || textureData.isDestroyed()
            || (textureData.hasBuffer() && textureData.getByteBuffer().limit() == 0 && !textureData.hasBitmap())) {
            throw new TextureException("Texture could not be added because there is no valid data set.");
        }

        // Set the dimensions
        setWidth(textureData.getWidth());
        setHeight(textureData.getHeight());

        // Fetch these once for efficiency. We use methods
        @Filter.FilterType final int filterType = getFilterType();
        @Wrap.WrapType final int wrapType = getWrapType();

        // Generate a texture id
        int[] genTextureNames = new int[1];
        GLES20.glGenTextures(1, genTextureNames, 0);
        int textureId = genTextureNames[0];

        if (textureId > 0) {
            // If a valid id was generated...
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            // Handle minification filtering
            if (isMipmaped()) {
                switch (filterType) {
                    case Filter.NEAREST:
                        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_NEAREST_MIPMAP_NEAREST);
                        break;
                    case Filter.BILINEAR:
                        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR_MIPMAP_NEAREST);
                        break;
                    case Filter.TRILINEAR:
                        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR_MIPMAP_LINEAR);
                        break;
                    default:
                        throw new TextureException("Unknown texture filtering mode: " + filterType);
                }
            } else {
                switch (filterType) {
                    case Filter.NEAREST:
                        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_NEAREST);
                        break;
                    case Filter.BILINEAR:
                        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR);
                        break;
                    case Filter.TRILINEAR:
                        RajLog.e("Trilinear filtering requires the use of mipmaps which are not enabled for this "
                            + "texture. Falling back to bilinear filtering.");
                        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR);
                        break;
                    default:
                        throw new TextureException("Unknown texture filtering mode: " + filterType);
                }
            }

            // Handle magnification filtering
            if (filterType == Filter.BILINEAR || filterType == Filter.TRILINEAR) {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            } else {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            }

            // Handle anisotropy if needed. We don't check if it is supported here because setting it to anything
            // other than 1.0 would have required the check.
            if (getMaxAnisotropy() > 1.0) {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.TEXTURE_MAX_ANISOTROPY_EXT,
                    getMaxAnisotropy());
            }

            // Handle s coordinate wrapping
            int wrap = GLES20.GL_REPEAT;
            if ((wrapType & Wrap.CLAMP_S) != 0) {
                wrap = GLES20.GL_CLAMP_TO_EDGE;
            } else if ((wrapType & Wrap.MIRRORED_REPEAT_S) != 0) {
                wrap = GLES20.GL_MIRRORED_REPEAT;
            }
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrap);

            // Handle t coordinate wrapping
            wrap = GLES20.GL_REPEAT;
            if ((wrapType & Wrap.CLAMP_T) != 0) {
                wrap = GLES20.GL_CLAMP_TO_EDGE;
            } else if ((wrapType & Wrap.MIRRORED_REPEAT_T) != 0) {
                wrap = GLES20.GL_MIRRORED_REPEAT;
            }
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrap);

            // Push the texture data
            if (textureData.hasBuffer()) {
                if (getWidth() == 0 || getHeight() == 0) {
                    throw new TextureException(
                        "Could not create ByteBuffer texture. One or more of the following properties haven't "
                            + "been set: width, height or bitmap format");
                }
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, getTexelFormat(), getWidth(), getHeight(), 0,
                    textureData.getPixelFormat(), textureData.getDataType(),
                    textureData.getByteBuffer());
            } else {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, getTexelFormat(), textureData.getBitmap(), 0);
            }

            // Generate mipmaps if enabled
            if (isMipmaped()) {
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            }

            // Store the texture id
            setTextureId(textureId);
        } else {
            throw new TextureException("Failed to generate a new texture id.");
        }

        if (willRecycle()) {
            textureData.recycle();
            this.textureData = null;
        }

        // Rebind the null texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    void remove() throws TextureException {
        final TextureDataReference textureData = this.textureData;
        final int id = getTextureId();
        if (id > 0) {
            // Call delete with GL only if necessary
            GLES20.glDeleteTextures(1, new int[]{getTextureId()}, 0);
            if (textureData != null) {
                // When removing a texture, release a reference count for its data if we have saved it.
                textureData.recycle();
            }
        }

        //TODO: Notify materials that were using this texture
    }

    @Override
    void replace() throws TextureException {
        final TextureDataReference textureData = this.textureData;
        if (textureData == null || textureData.isDestroyed() || (textureData.hasBuffer()
            && textureData.getByteBuffer().limit() == 0 && !textureData.hasBitmap())) {
            final String error = "Texture2D could not be replaced because there is no Bitmap or ByteBuffer set. Flags: "
                                 + (textureData == null ? "null" : ("false || " + textureData.isDestroyed() + " || "
                                                                   + textureData.hasBuffer() + " && "
                                                                   + (textureData.getByteBuffer().limit() == 0) + " && "
                                                                   + !textureData.hasBitmap()));
            RajLog.e(error);
            throw new TextureException(error);
        }

        if (textureData.getWidth() != getWidth() || textureData.getHeight() != getHeight()) {
            throw new TextureException(
                    "Texture could not be updated because the texture size is different from the original.");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());

        if (textureData.hasBuffer()) {
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, getWidth(), getHeight(), textureData.getPixelFormat(),
                GLES20.GL_UNSIGNED_BYTE, textureData.getByteBuffer());
        } else {
            int bitmapFormat = textureData.getBitmap().getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;

            if (bitmapFormat != getTexelFormat()) {
                throw new TextureException(
                        "Texture could not be updated because the texel format is different from the original");
            }

            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, textureData.getBitmap(), getTexelFormat(),
                                  GLES20.GL_UNSIGNED_BYTE);
        }

        if (isMipmaped()) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    void reset() throws TextureException {
        final TextureDataReference textureData = this.textureData;
        if (textureData != null) {
            textureData.recycle();
            this.textureData = null;
        }
    }

    //TODO: Update method
}
