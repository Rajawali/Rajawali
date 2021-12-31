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
package org.rajawali3d.materials.textures;

import android.opengl.GLES20;

public class RenderTargetTexture extends ATexture {

    public enum RenderTargetTextureFormat {
        RGBA(GLES20.GL_RGBA), RGB(GLES20.GL_RGB), DEPTH(GLES20.GL_DEPTH_COMPONENT),
        DEPTH16(GLES20.GL_DEPTH_COMPONENT16);

        private int mFormat;

        RenderTargetTextureFormat(int format) {
            mFormat = format;
        }

        public int getFormat() {
            return mFormat;
        }
    }

    public enum RenderTargetTextureType {
        UNSIGNED_BYTE(GLES20.GL_UNSIGNED_BYTE), BYTE(GLES20.GL_BYTE), UNSIGNED_SHORT(GLES20.GL_UNSIGNED_SHORT),
        SHORT(GLES20.GL_SHORT), UNSIGNED_INT(GLES20.GL_UNSIGNED_INT), INT(GLES20.GL_INT), FLOAT(GLES20.GL_FLOAT);

        private int mType;

        RenderTargetTextureType(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }

    }

    private RenderTargetTextureFormat mInternalFormat;
    private RenderTargetTextureFormat mFormat;
    private RenderTargetTextureType   mType;

    public RenderTargetTexture(RenderTargetTexture other) {
        super(other);
    }

    public RenderTargetTexture(String textureName) {
        this(textureName, 32, 32);
    }

    public RenderTargetTexture(String textureName, int width, int height) {
        this(textureName, width, height, RenderTargetTextureFormat.RGBA, RenderTargetTextureFormat.RGBA,
             RenderTargetTextureType.UNSIGNED_BYTE);
    }

    public RenderTargetTexture(String textureName, int width, int height, RenderTargetTextureFormat internalFormat,
                               RenderTargetTextureFormat format, RenderTargetTextureType type) {
        super(TextureType.RENDER_TARGET, textureName);
        mInternalFormat = internalFormat;
        mFormat = format;
        mType = type;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public RenderTargetTexture clone() {
        return new RenderTargetTexture(this);
    }

    @Override public void setWidth(int width) {
        super.setWidth(width);
        TextureManager.getInstance().getRenderer().resizeRenderTarget(this);
    }

    @Override public void setHeight(int height) {
        super.setHeight(height);
        TextureManager.getInstance().getRenderer().resizeRenderTarget(this);
    }

    /**
     * Resizes this render target texture. This method should be preferred over {@link #setWidth(int)} and {@link #setHeight(int)}
     * if both dimensions will be changing as it is more efficient than calling both seperately.
     *
     * @param width
     * @param height
     */
    public void resize(int width, int height) {
        mWidth = width;
        mHeight = height;
        TextureManager.getInstance().getRenderer().resizeRenderTarget(this);
    }

    public void setFrom(RenderTargetTexture other) {
        super.setFrom(other);
    }

    @Override protected void add() throws TextureException {
        if (mWidth == 0 || mHeight == 0) {
            throw new TextureException(
                    "FrameBufferTexture could not be added because the width and/or height weren't specified.");
        }

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];

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

            switch (mWrapType) {
            case MIRRORED:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
                break;
            case REPEAT:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
                break;
            default:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                break;
            }


            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, mInternalFormat.getFormat(), mWidth, mHeight, 0,
                                mFormat.getFormat(),
                                mType.getType(), null);
            if (isMipmap()) {
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            setTextureId(textureId);
        }
    }

    @Override protected void remove() {
        GLES20.glDeleteTextures(1, new int[]{ mTextureId }, 0);
    }

    @Override protected void replace() {
        return;
    }

    void resize() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, mInternalFormat.getFormat(), mWidth, mHeight, 0,
                            mFormat.getFormat(), mType.getType(), null);
        if (isMipmap()) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    protected void reset() {
        return;
    }
}
