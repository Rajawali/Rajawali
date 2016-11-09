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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import c.org.rajawali3d.textures.annotation.Filter;
import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.textures.annotation.Wrap;
import c.org.rajawali3d.textures.annotation.Wrap.WrapType;

import java.nio.ByteBuffer;

/**
 * A 2D cube mapped environmental texture. These textures are typically used to simulate highly reflective
 * surfaces by providing what the reflected environment would look like. For static or basic reflective appearances,
 * a single texture can be used. For more advanced reflections, the scene can be rendered to a FBO with cube
 * mapping which is used as a {@link Type#SPHERE_MAP} texture. They are also commonly used for sky boxes, simulating
 * the appearance of a far off sky.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class CubeMapTexture extends AMultiTexture {

    public final int[] CUBE_FACES = new int[]{
        GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
        GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
        GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
        GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
        GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
        GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
    };

    private boolean isSkyTexture = false;
    private boolean hasCompressedTextures;

    /**
     * Constructs a new {@link CubeMapTexture} with data and settings from the provided {@link CubeMapTexture}.
     *
     * @param other The other {@link CubeMapTexture}.
     */
    public CubeMapTexture(@NonNull CubeMapTexture other) {
        super(other);
    }

    /**
     * Constructs a new {@link CubeMapTexture} with the provided name and no data.
     *
     * @param name {@link String} The texture name.
     */
    public CubeMapTexture(@NonNull String name) {
        super(Type.CUBE_MAP, name);
        setWrapType((Wrap.CLAMP_S | Wrap.CLAMP_T | Wrap.CLAMP_R));
        setTextureTarget(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(@NonNull String name, @NonNull @DrawableRes int[] resourceIds) {
        super(Type.CUBE_MAP, name, resourceIds);
        setWrapType((Wrap.CLAMP_S | Wrap.CLAMP_T | Wrap.CLAMP_R));
        setTextureTarget(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(@NonNull String name, @NonNull Bitmap[] bitmaps) {
        super(Type.CUBE_MAP, name, bitmaps);
        setWrapType((Wrap.CLAMP_S | Wrap.CLAMP_T | Wrap.CLAMP_R));
        setTextureTarget(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(@NonNull String name, @NonNull ByteBuffer[] byteBuffers) {
        super(Type.CUBE_MAP, name, byteBuffers);
        setWrapType((Wrap.CLAMP_S | Wrap.CLAMP_T | Wrap.CLAMP_R));
        setTextureTarget(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    public CubeMapTexture(@NonNull String name, @NonNull CompressedTexture2D[] compressedTexture2Ds) {
        super(Type.CUBE_MAP, name, compressedTexture2Ds);
        hasCompressedTextures = true;
        setWrapType(Wrap.CLAMP_S | Wrap.CLAMP_T | Wrap.CLAMP_R);
        setTextureTarget(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    /**
     * Sets whether or not this texture is treated a sky sphere or environmental map. By default,
     * {@link SphereMapTexture2D}s are treated as environmental maps.
     *
     * @param value {@code true} if this texture should be treated as a sky sphere.
     */
    public void isSkyTexture(boolean value) {
        isSkyTexture = value;
    }

    /**
     * Returns whether or not this texture is treated as a sky sphere or environmental map. By default,
     * {@link SphereMapTexture2D}s are treated as environmental maps.
     *
     * @return {@code true} if this texture is treated as a sky sphere.
     */
    public boolean isSkyTexture() {
        return isSkyTexture;
    }

    private void checkBitmapConfiguration() throws TextureException {
        if ((mBitmaps == null || mBitmaps.length == 0) && (mByteBuffers == null || mByteBuffers.length == 0) && !hasCompressedTextures)
            throw new TextureException("Texture2D could not be added because no Bitmaps or ByteBuffers set.");
        if (mBitmaps != null && mBitmaps.length != 6)
            throw new TextureException("CubeMapTexture could not be added because it needs six textures instead of " + mBitmaps.length);

        if (mBitmaps != null) {
            setTexelFormat(mBitmaps[0].getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB);
            setWidth(mBitmaps[0].getWidth());
            setHeight(mBitmaps[0].getHeight());
        }
    }

    private void setTextureData() {
        @Filter.FilterType final int filterType = getFilterType();
        @WrapType final int wrapType = getWrapType();

        if (isMipmaped()) {
            if (filterType == Filter.BILINEAR)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_LINEAR);
            else
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST_MIPMAP_NEAREST);
        } else {
            if (filterType == Filter.BILINEAR)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            else
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        }

        if (filterType == Filter.BILINEAR)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        else
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        if (wrapType == (Wrap.REPEAT_S | Wrap.REPEAT_T | Wrap.REPEAT_R)) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        } else {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

        for (int i = 0; i < 6; i++) {
            GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
            if (mBitmaps != null) {
                GLUtils.texImage2D(CUBE_FACES[i], 0, mBitmaps[i], 0);
            } else if(hasCompressedTextures) {
                CompressedTexture2D tex = mCompressedTextures[i];
                int w = tex.getWidth(), h = tex.getHeight();
                for (int j = 0; j < tex.getByteBuffers().length; j++) {
                    GLES20.glCompressedTexImage2D(CUBE_FACES[i], j, tex.getCompressionFormat(), w, h, 0,
                            tex.getByteBuffers()[j].capacity(), tex.getByteBuffers()[j]);
                    w = w > 1 ? w / 2 : 1;
                    h = h > 1 ? h / 2 : 1;
                }
            } else {
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP, 0, getTexelFormat(), getWidth(), getHeight(), 0, getTexelFormat(),
                                    GLES20.GL_UNSIGNED_BYTE, mByteBuffers[i]);
            }
        }

        if (isMipmaped())
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);

        if (willRecycle()) {
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

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public CubeMapTexture clone() {
        return new CubeMapTexture(this);
    }

    @Override
    void add() throws TextureException {
        if(hasCompressedTextures) {
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
    void remove() throws TextureException {
        if(hasCompressedTextures) {
            for(int i=0; i<mCompressedTextures.length; i++) {
                mCompressedTextures[i].remove();
            }
        }
        GLES20.glDeleteTextures(1, new int[]{ getTextureId() }, 0);
    }

    @Override
    void replace() throws TextureException {
        checkBitmapConfiguration();

        if (getTextureId() > 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, getTextureId());
            if(hasCompressedTextures) {
                for (int i = 0; i < 6; i++) {
                    CompressedTexture2D tex = mCompressedTextures[i];
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
}
