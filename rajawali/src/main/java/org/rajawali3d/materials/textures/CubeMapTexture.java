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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;


public class CubeMapTexture extends AMultiTexture {
    public final int[] CUBE_FACES = new int[]{
        GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
        GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
        GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
        GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
        GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
        GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
    };

    private boolean mIsSkyTexture;
    private boolean mIsEnvironmentTexture;
    private boolean mHasCompressedTextures;

    public CubeMapTexture(CubeMapTexture other) {
        super(other);
    }

    public CubeMapTexture(String textureName) {
        super(TextureType.CUBE_MAP, textureName);
        setWrapType(WrapType.CLAMP);
        setGLTextureType(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(String textureName, int[] resourceIds) {
        super(TextureType.CUBE_MAP, textureName, resourceIds);
        setWrapType(WrapType.CLAMP);
        setGLTextureType(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(String textureName, Bitmap[] bitmaps) {
        super(TextureType.CUBE_MAP, textureName, bitmaps);
        setWrapType(WrapType.CLAMP);
        setGLTextureType(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(String textureName, ByteBuffer[] byteBuffers) {
        super(TextureType.CUBE_MAP, textureName, byteBuffers);
        setWrapType(WrapType.CLAMP);
        setGLTextureType(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(String textureName, ACompressedTexture[] compressedTextures) {
        super(TextureType.CUBE_MAP, textureName, compressedTextures);
        mHasCompressedTextures = true;
        setWrapType(WrapType.CLAMP);
        setGLTextureType(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture clone() {
        return new CubeMapTexture(this);
    }

    private void checkBitmapConfiguration() throws TextureException {
        if ((mBitmaps == null || mBitmaps.length == 0) && (mByteBuffers == null || mByteBuffers.length == 0) && !mHasCompressedTextures)
            throw new TextureException("Texture could not be added because no Bitmaps or ByteBuffers set.");
        if (mBitmaps != null && mBitmaps.length != 6)
            throw new TextureException("CubeMapTexture could not be added because it needs six textures instead of " + mBitmaps.length);

        if (mBitmaps != null) {
            setBitmapConfig(mBitmaps[0].getConfig());
            setBitmapFormat(mBitmapConfig == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB);
            setWidth(mBitmaps[0].getWidth());
            setHeight(mBitmaps[0].getHeight());
        }
    }

    private void setTextureData() {
        if (isMipmap()) {
            if (mFilterType == FilterType.LINEAR)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_LINEAR);
            else
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST_MIPMAP_NEAREST);
        } else {
            if (mFilterType == FilterType.LINEAR)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            else
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        }

        if (mFilterType == FilterType.LINEAR)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        else
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

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
	}

        for (int i = 0; i < 6; i++) {
            GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
            if (mBitmaps != null) {
                GLUtils.texImage2D(CUBE_FACES[i], 0, mBitmaps[i], 0);
            } else if(mHasCompressedTextures) {
                ACompressedTexture tex = mCompressedTextures[i];
                int w = tex.getWidth(), h = tex.getHeight();
                for (int j = 0; j < tex.getByteBuffers().length; j++) {
                    GLES20.glCompressedTexImage2D(CUBE_FACES[i], j, tex.getCompressionFormat(), w, h, 0,
                            tex.getByteBuffers()[j].capacity(), tex.getByteBuffers()[j]);
                    w = w > 1 ? w / 2 : 1;
                    h = h > 1 ? h / 2 : 1;
                }
            } else {
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP, 0, mBitmapFormat, mWidth, mHeight, 0, mBitmapFormat,
                        GLES20.GL_UNSIGNED_BYTE, mByteBuffers[i]);
            }
        }

        if (isMipmap())
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);

        if (mShouldRecycle) {
            if (mBitmaps != null) {
                for (Bitmap bitmap : mBitmaps) {
                    bitmap.recycle();
                    bitmap = null;
                }
                mBitmaps = null;
            }
            mByteBuffers = null;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0);
    }

    @Override
    protected void add() throws TextureException {
        if(mHasCompressedTextures) {
            for(int i=0; i<mCompressedTextures.length; i++) {
                mCompressedTextures[i].add();
            }
        }
        checkBitmapConfiguration();
        int[] genTextureNames = new int[1];
        GLES20.glGenTextures(1, genTextureNames, 0);
        int textureId = genTextureNames[0];

        if (textureId > 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
            setTextureData();
            setTextureId(textureId);
        } else {
            throw new TextureException("Couldn't generate a texture name.");
        }
    }

    @Override
    protected void remove() throws TextureException {
        if(mHasCompressedTextures) {
            for(int i=0; i<mCompressedTextures.length; i++) {
                mCompressedTextures[i].remove();
            }
        }
        GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
    }

    @Override
    protected void replace() throws TextureException {
        checkBitmapConfiguration();

        if (mTextureId > 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, mTextureId);
            if(mHasCompressedTextures) {
                for (int i = 0; i < 6; i++) {
                    ACompressedTexture tex = mCompressedTextures[i];
                    tex.add();
                    int w = tex.getWidth(), h = tex.getHeight();
                    for (int j = 0; j < tex.getByteBuffers().length; j++) {
                        GLES20.glCompressedTexSubImage2D(CUBE_FACES[i], j, 0, 0, w, h, tex.getCompressionFormat(),
                                tex.getByteBuffers()[j].capacity(), tex.getByteBuffers()[j]);
                        w = w > 1 ? w / 2 : 1;
                        h = h > 1 ? h / 2 : 1;
                    }
                }
                GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0);
            } else {
                setTextureData();
            }
        } else {
            throw new TextureException("Couldn't generate a texture name.");
        }
    }

    public void isSkyTexture(boolean value) {
        mIsSkyTexture = value;
        mIsEnvironmentTexture = !value;
    }

    public boolean isSkyTexture() {
        return mIsSkyTexture;
    }

    public void isEnvironmentTexture(boolean value) {
        mIsEnvironmentTexture = value;
        mIsSkyTexture = !mIsEnvironmentTexture;
    }

    public boolean isEnvironmentTexture() {
        return mIsEnvironmentTexture;
    }
}
