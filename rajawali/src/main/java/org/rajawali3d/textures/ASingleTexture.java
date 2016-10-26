/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import org.rajawali3d.textures.annotation.Filter;
import org.rajawali3d.textures.annotation.Filter.FilterType;
import org.rajawali3d.textures.annotation.Type.TextureType;
import org.rajawali3d.textures.annotation.Wrap;
import org.rajawali3d.textures.annotation.Wrap.WrapType;

/**
 * This class is used to specify texture options.
 *
 * @author dennis.ippel
 */
public abstract class ASingleTexture extends ATexture {
    protected TextureDataReference textureData;
    protected int                  mResourceId;

    protected ASingleTexture() {
        super();
    }

    public ASingleTexture(@TextureType int textureType, String textureName) {
        super(textureType, textureName);
    }

    public ASingleTexture(@TextureType int textureType, @NonNull Context context, int resourceId) {
        this(textureType, context.getResources().getResourceName(resourceId));
        setResourceId(context, resourceId);
    }

    public ASingleTexture(@TextureType int textureType, String textureName, TextureDataReference textureData) {
        this(textureType, textureName);
        setTextureData(textureData);
    }

    public ASingleTexture(@TextureType int textureType, String textureName, ACompressedTexture compressedTexture) {
        super(textureType, textureName, compressedTexture);
    }

    public ASingleTexture(ASingleTexture other) {
        super(other);
        setFrom(other);
    }

    /**
     * Creates a clone
     */
    public abstract ASingleTexture clone();

    /**
     * Copies every property from another ATexture object
     *
     * @param other another ATexture object to copy from
     */
    public void setFrom(ASingleTexture other) {
        super.setFrom(other);
        setTextureData(other.getTextureData());
    }

    @NonNull
    public TextureDataReference setResourceId(@NonNull Context context, int resourceId) {
        mResourceId = resourceId;
        BitmapFactory.Options bitmapScalingOptions = new BitmapFactory.Options();
        bitmapScalingOptions.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, bitmapScalingOptions);
        setTextureData(new TextureDataReference(bitmap, null, bitmap.getConfig().equals(Config.RGB_565)
                                                              ? GLES20.GL_RGB : GLES20.GL_RGBA,
                                                GLES20.GL_UNSIGNED_BYTE));
        return textureData;
    }

    public int getResourceId() {
        return mResourceId;
    }

    public void setTextureData(@NonNull TextureDataReference data) {
        if (textureData != null) {
            textureData.recycle();
        }
        textureData = data;
        textureData.holdReference();
    }

    @NonNull
    public TextureDataReference getTextureData() {
        return textureData;
    }

    void add() throws TextureException {
        if (compressedTexture != null) {
            compressedTexture.add();
            setWidth(compressedTexture.getWidth());
            setHeight(compressedTexture.getHeight());
            setTextureId(compressedTexture.getTextureId());
            return;
        }

        if (textureData == null || textureData.isDestroyed() || (textureData.hasBuffer()
                                                                 && textureData.getByteBuffer().limit() == 0)) {
            throw new TextureException("Texture could not be added because there is no valid data set.");
        }

        if (textureData.hasBitmap()) {
            setTexelFormat(textureData.getBitmap().getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB);
            setWidth(textureData.getBitmap().getWidth());
            setHeight(textureData.getBitmap().getHeight());
        }

        int[] genTextureNames = new int[1];
        GLES20.glGenTextures(1, genTextureNames, 0);
        int textureId = genTextureNames[0];

        if (textureId > 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            if (isMipmap()) {
                if (filterType == Filter.LINEAR) {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                           GLES20.GL_LINEAR_MIPMAP_LINEAR);
                } else {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                           GLES20.GL_NEAREST_MIPMAP_NEAREST);
                }
            } else {
                if (filterType == Filter.LINEAR) {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                } else {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                }
            }

            if (filterType == Filter.LINEAR) {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            } else {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            }

            if (wrapType == Wrap.REPEAT) {
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            } else {
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            }

            if (textureData.hasBuffer()) {
                if (width == 0 || height == 0 || texelFormat == 0) {
                    throw new TextureException(
                            "Could not create ByteBuffer texture. One or more of the following properties haven't "
                            + "been set: width, height or bitmap format");
                }
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, texelFormat, width, height, 0, texelFormat,
                                    GLES20.GL_UNSIGNED_BYTE, textureData.getByteBuffer());
            } else {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texelFormat, textureData.getBitmap(), 0);
            }

            if (isMipmap()) {
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            }

            setTextureId(textureId);
        } else {
            throw new TextureException("Couldn't generate a texture name.");
        }

        if (shouldRecycle) {
            textureData.recycle();
            textureData = null;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    void remove() throws TextureException {
        if (compressedTexture != null) {
            compressedTexture.remove();
        } else {
            GLES20.glDeleteTextures(1, new int[]{ textureId }, 0);
        }
        if (textureData != null) {
            // When removing a texture, release a reference count for its data if we have saved it.
            textureData.recycle();
        }

        //TODO: Notify materials that were using this texture
    }

    void replace() throws TextureException {
        if (compressedTexture != null) {
            compressedTexture.replace();
            setWidth(compressedTexture.getWidth());
            setHeight(compressedTexture.getHeight());
            setTextureId(compressedTexture.getTextureId());
            return;
        }

        if (textureData == null || textureData.isDestroyed() || (textureData.hasBuffer()
                                                                 && textureData.getByteBuffer().limit() == 0)) {
            throw new TextureException("Texture could not be replaced because there is no Bitmap or ByteBuffer set.");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        if (textureData.hasBitmap()) {
            int bitmapFormat = textureData.getBitmap().getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
            if (textureData.getBitmap().getWidth() != width || textureData.getBitmap().getHeight() != height) {
                throw new TextureException(
                        "Texture could not be updated because the texture size is different from the original.");
            }
            if (bitmapFormat != this.texelFormat) {
                throw new TextureException(
                        "Texture could not be updated because the bitmap format is different from the original");
            }

            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, textureData.getBitmap(), this.texelFormat,
                                  GLES20.GL_UNSIGNED_BYTE);
        } else if (textureData.hasBuffer()) {
            if (width == 0 || height == 0 || texelFormat == 0) {
                throw new TextureException(
                        "Could not update ByteBuffer texture. One or more of the following properties haven't been "
                        + "set: width, height or bitmap format");
            }
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, texelFormat,
                                   GLES20.GL_UNSIGNED_BYTE, textureData.getByteBuffer());
        }

        if (mipmap) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    void reset() throws TextureException {
        if (compressedTexture != null) {
            compressedTexture.reset();
            return;
        }

        if (textureData != null) {
            textureData.recycle();
            textureData = null;
        }
    }

    @Override
    public void setWrapType(@WrapType int wrapType) {
        super.setWrapType(wrapType);
        if (compressedTexture != null) {
            compressedTexture.setWrapType(wrapType);
        }
    }

    @Override
    public void setFilterType(@FilterType int filterType) {
        super.setFilterType(filterType);
        if (compressedTexture != null) {
            compressedTexture.setFilterType(filterType);
        }
    }
}
