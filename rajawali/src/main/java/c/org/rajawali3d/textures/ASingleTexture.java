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
package c.org.rajawali3d.textures;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import org.rajawali3d.materials.textures.ATexture.FilterType;
import org.rajawali3d.materials.textures.ATexture.TextureType;
import org.rajawali3d.materials.textures.ATexture.WrapType;
import org.rajawali3d.materials.textures.TextureException;
import org.rajawali3d.materials.textures.TextureManager;

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

    public ASingleTexture(TextureType textureType, String textureName) {
        super(textureType, textureName);
    }

    public ASingleTexture(TextureType textureType, int resourceId) {
        this(textureType, TextureManager.getInstance().getContext().getResources().getResourceName(resourceId));
        setResourceId(resourceId);
    }

    public ASingleTexture(TextureType textureType, String textureName, TextureDataReference textureData) {
        this(textureType, textureName);
        setTextureData(textureData);
    }

    public ASingleTexture(TextureType textureType, String textureName, ACompressedTexture compressedTexture) {
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
    public TextureDataReference setResourceId(int resourceId) {
        mResourceId = resourceId;
        Context context = TextureManager.getInstance().getContext();
        BitmapFactory.Options bitmapScalingOptions = new BitmapFactory.Options();
        bitmapScalingOptions.inScaled = false;
        setTextureData(new TextureDataReference(
                BitmapFactory.decodeResource(context.getResources(), resourceId, bitmapScalingOptions), null));
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
    }

    @NonNull
    public TextureDataReference getTextureData() {
        return textureData;
    }

    void add() throws TextureException {
        if (mCompressedTexture != null) {
            mCompressedTexture.add();
            setWidth(mCompressedTexture.getWidth());
            setHeight(mCompressedTexture.getHeight());
            setTextureId(mCompressedTexture.getTextureId());
            return;
        }

        if (textureData == null || textureData.isDestroyed() || (textureData.hasBuffer()
                                                                 && textureData.getByteBuffer().limit() == 0)) {
            throw new TextureException("Texture could not be added because there is no valid data set.");
        }

        if (textureData.hasBitmap()) {
            setBitmapFormat(textureData.getBitmap().getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB);
            setWidth(textureData.getBitmap().getWidth());
            setHeight(textureData.getBitmap().getHeight());
        }

        int[] genTextureNames = new int[1];
        GLES20.glGenTextures(1, genTextureNames, 0);
        int textureId = genTextureNames[0];

        if (textureId > 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            if (isMipmap()) {
                if (mFilterType == FilterType.LINEAR) {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                           GLES20.GL_LINEAR_MIPMAP_LINEAR);
                } else {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                           GLES20.GL_NEAREST_MIPMAP_NEAREST);
                }
            } else {
                if (mFilterType == FilterType.LINEAR) {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                } else {
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                }
            }

            if (mFilterType == FilterType.LINEAR) {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            } else {
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            }

            if (mWrapType == WrapType.REPEAT) {
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            } else {
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            }

            if (textureData.hasBuffer()) {
                if (mWidth == 0 || mHeight == 0 || mBitmapFormat == 0) {
                    throw new TextureException(
                            "Could not create ByteBuffer texture. One or more of the following properties haven't "
                            + "been set: width, height or bitmap format");
                }
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmapFormat, mWidth, mHeight, 0, mBitmapFormat,
                                    GLES20.GL_UNSIGNED_BYTE, textureData.getByteBuffer());
            } else {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmapFormat, textureData.getBitmap(), 0);
            }

            if (isMipmap()) {
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            }

            setTextureId(textureId);
        } else {
            throw new TextureException("Couldn't generate a texture name.");
        }

        if (mShouldRecycle) {
            textureData.recycle();
            textureData = null;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    void remove() throws TextureException {
        if (mCompressedTexture != null) {
            mCompressedTexture.remove();
        } else {
            GLES20.glDeleteTextures(1, new int[]{ mTextureId }, 0);
        }
        if (textureData != null) {
            // When removing a texture, release a reference count for its data if we have saved it.
            textureData.recycle();
        }
    }

    void replace() throws TextureException {
        if (mCompressedTexture != null) {
            mCompressedTexture.replace();
            setWidth(mCompressedTexture.getWidth());
            setHeight(mCompressedTexture.getHeight());
            setTextureId(mCompressedTexture.getTextureId());
            return;
        }

        if (textureData == null || textureData.isDestroyed() || (textureData.hasBuffer()
                                                                 && textureData.getByteBuffer().limit() == 0)) {
            throw new TextureException("Texture could not be replaced because there is no Bitmap or ByteBuffer set.");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        if (textureData.hasBitmap()) {
            int bitmapFormat = textureData.getBitmap().getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
            if (textureData.getBitmap().getWidth() != mWidth || textureData.getBitmap().getHeight() != mHeight) {
                throw new TextureException(
                        "Texture could not be updated because the texture size is different from the original.");
            }
            if (bitmapFormat != mBitmapFormat) {
                throw new TextureException(
                        "Texture could not be updated because the bitmap format is different from the original");
            }

            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, textureData.getBitmap(), mBitmapFormat, GLES20.GL_UNSIGNED_BYTE);
        } else if (textureData.hasBuffer()) {
            if (mWidth == 0 || mHeight == 0 || mBitmapFormat == 0) {
                throw new TextureException(
                        "Could not update ByteBuffer texture. One or more of the following properties haven't been set: width, height or bitmap format");
            }
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, mBitmapFormat,
                                   GLES20.GL_UNSIGNED_BYTE, textureData.getByteBuffer());
        }

        if (mMipmap) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    void reset() throws TextureException {
        if (mCompressedTexture != null) {
            mCompressedTexture.reset();
            return;
        }

        if (textureData != null) {
            textureData.recycle();
            textureData = null;
        }
    }

    /**
     * @param wrapType the texture wrap type. See {@link WrapType}.
     */
    public void setWrapType(WrapType wrapType) {
        super.setWrapType(wrapType);
        if (mCompressedTexture != null) {
            mCompressedTexture.setWrapType(wrapType);
        }
    }

    /**
     * @param filterType Texture filtering type. See {@link FilterType}.
     */
    public void setFilterType(FilterType filterType) {
        super.setFilterType(filterType);
        if (mCompressedTexture != null) {
            mCompressedTexture.setFilterType(filterType);
        }
    }
}
