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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.jcip.annotations.ThreadSafe;

import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.textures.annotation.PixelFormat;
import c.org.rajawali3d.textures.annotation.Type.TextureType;

/**
 * This class is used to specify common functions of a multi-2D texture. Subclasses are expected to be thread safe.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
@SuppressWarnings("WeakerAccess")
@ThreadSafe
public abstract class MultiTexture2D extends BaseTexture {

    /**
     * The texture data.
     */
    @Nullable
    private volatile TextureDataReference[] textureData;

    /**
     * Basic no-args constructor used by some subclasses. No initialization is performed.
     */
    protected MultiTexture2D() {
        super();
    }

    /**
     * Constructs a new {@link MultiTexture2D} with the specified name and type.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     */
    public MultiTexture2D(@TextureType int type, @NonNull String name) {
        super(type, name);
    }

    /**
     * Constructs a new {@link MultiTexture2D} with data provided by the Android resource id.
     *
     * @param type        {@link TextureType} The texture usage type.
     * @param name        {@link String} The texture name.
     * @param context     {@link Context} The application context.
     * @param resourceIds {@code int[]} The Android resource id to load from.
     */
    public MultiTexture2D(@TextureType int type, @NonNull String name, @NonNull Context context,
                          @NonNull @DrawableRes int[] resourceIds) {
        super(type, name);
        setTextureDataFromResourceIds(context, resourceIds);
    }

    /**
     * Constructs a new {@link MultiTexture2D} with the provided data.
     *
     * @param type {@link TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference}[] The texture data.
     */
    public MultiTexture2D(@TextureType int type, @NonNull String name, @NonNull TextureDataReference[] data) {
        super(type, name);
        setTextureData(data);
    }

    /**
     * Constructs a new {@link MultiTexture2D} with data and settings from the provided {@link MultiTexture2D}.
     *
     * @param other The other {@link MultiTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture creation process.
     */
    public MultiTexture2D(@NonNull MultiTexture2D other) throws TextureException {
        super(other);
        setFrom(other);
    }

    /**
     * Copies all properties and data from another {@link MultiTexture2D}.
     *
     * @param other The other {@link MultiTexture2D}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture creation process.
     */
    public void setFrom(@NonNull MultiTexture2D other) throws TextureException {
        final TextureDataReference[] data = other.getTextureData();
        if (data != null) {
            super.setFrom(other);
            setTextureData(data);
        } else {
            throw new TextureException("Texture data was null!");
        }
    }

    /**
     * Sets the data used by this {@link MultiTexture2D} from an Android resource id. This will create a new
     * {@link TextureDataReference} array and set it as the active data. Do not use this method if you wish to use the
     * texture as a Luminance texture as it will assume a {@link GLES20#GL_RGB} or {@link GLES20#GL_RGBA}
     * {@link PixelFormat}.
     *
     * @param context     {@link Context} The Android application context.
     * @param resourceIds {@code int[]} The Android resource id to load from.
     *
     * @return The new {@link TextureDataReference} array which was created.
     */
    @NonNull
    public TextureDataReference[] setTextureDataFromResourceIds(@NonNull Context context,
                                                                @DrawableRes int[] resourceIds) {
        // Prevent the bitmap from being scaled as it is decoded
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final TextureDataReference[] references = new TextureDataReference[resourceIds.length];
        for (int i = 0, j = resourceIds.length; i < j; ++i) {
            // Decode the bitmap
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceIds[i], options);
            references[i] = new TextureDataReference(bitmap, null, bitmap.getConfig().equals(Config.RGB_565)
                ? GLES20.GL_RGB : GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, bitmap.getWidth(), bitmap.getHeight());
        }
        setTextureData(references);
        return references;
    }

    /**
     * Sets the data used by this {@link MultiTexture2D} from the provided {@link TextureDataReference} array. The data
     * references will have their reference count incremented and any existing data reference will be released.
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

    /**
     * Retrieves the current {@link TextureDataReference} array used by this {@link MultiTexture2D}.
     *
     * @return The current {@link TextureDataReference}.
     */
    @Nullable
    protected TextureDataReference[] getTextureData() {
        return textureData;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @GLThread
    @Override
    void remove() throws TextureException {
        final TextureDataReference[] textureData = getTextureData();
        final int id = getTextureId();
        if (id > 0) {
            // Call delete with GL only if necessary
            GLES20.glDeleteTextures(1, new int[]{getTextureId()}, 0);
            if (textureData != null) {
                // When removing a texture, release a reference count for its data if we have saved it.
                for (int i = 0, j = textureData.length; i < j; ++i) {
                    if (textureData[i] != null) {
                        textureData[i].recycle();
                    }
                }
            }
        }

        //TODO: Notify materials that were using this texture
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @GLThread
    @Override
    void reset() throws TextureException {
        setTextureData(null);
    }
}
