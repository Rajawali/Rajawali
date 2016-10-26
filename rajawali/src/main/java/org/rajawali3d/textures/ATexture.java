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

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import org.rajawali3d.materials.Material;
import org.rajawali3d.textures.annotation.Filter;
import org.rajawali3d.textures.annotation.Filter.FilterType;
import org.rajawali3d.textures.annotation.TexelFormat;
import org.rajawali3d.textures.annotation.Type.TextureType;
import org.rajawali3d.textures.annotation.Wrap;
import org.rajawali3d.textures.annotation.Wrap.WrapType;

import java.nio.Buffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract texture class.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ATexture {

    /**
     * The GL texture id that is used by Rajawali.
     */
    protected int textureId = -1;

    /**
     * Texture width, in texels.
     */
    protected int width;

    /**
     * Texture height, in texels.
     */
    protected int height;

    /**
     * The OpenGL texel storage format. The format describes how texels are stored. This affects the
     * quality (color depth) as well as the ability to display transparent/translucent colors. It is vital that
     * the provided format be in agreement with the rules of
     * {@link GLES20#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)} and related methods. These rules
     * differ between GL ES 2.x and GL ES 3.x.
     *
     * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
     */
    @TexelFormat
    protected int texelFormat;

    /**
     * Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized collections of images
     * that accompany a main texture, intended to increase rendering speed and reduce aliasing artifacts. They
     * increase the ammount of time for an initial texture push as well as the size of the texture (in video RAM),
     * but can dramatically improve render quality.
     */
    protected boolean mipmap;

    /**
     * Indicates whether the source {@link TextureDataReference} should be recycled immediately after the OpenGL texture
     * has been created. The main reason for not recycling is scene caching. Scene caching stores all textures and
     * relevant OpenGL-specific data. This is used when the OpenGL context needs to be restored. The context typically
     * needs to be restored when the application is re-activated or when a live wallpaper is rotated.
     */
    protected boolean shouldRecycle;

    /**
     * The texture name that will be used in the shader.
     */
    @NonNull protected String textureName;

    /**
     * The type of texture
     *
     * @see {@link TextureType}.
     */
    @TextureType protected int textureType;

    /**
     * Texture wrap type.
     *
     * @see {@link WrapType}.
     */
    @WrapType protected int wrapType;

    /**
     * Texture filtering type.
     *
     * @see {@link FilterType}
     */
    @FilterType protected int filterType;

    /**
     * A list of materials that use this texture.
     */
    protected List<Material> materialsUsingTexture;

    /**
     * The optional compressed texture.
     */
    protected ACompressedTexture compressedTexture;

    /**
     * The OpenGL texture type.
     */
    protected int glTextureType = GLES20.GL_TEXTURE_2D;

    protected float   influence = 1.0f;
    protected float[] repeat    = new float[]{ 1, 1 };
    protected boolean enableOffset;
    protected float[] offset = new float[]{ 0, 0 };

    /**
     * Creates a new ATexture instance with the specified texture type.
     *
     * @param textureType
     */
    public ATexture(@TextureType int textureType, @NonNull String textureName) {
        this();
        this.textureType = textureType;
        this.textureName = textureName;
        mipmap = true;
        shouldRecycle = false;
        wrapType = Wrap.REPEAT;
        filterType = Filter.LINEAR;
    }

    public ATexture(@TextureType int textureType, @NonNull String textureName, ACompressedTexture compressedTexture) {
        this(textureType, textureName);
        setCompressedTexture(compressedTexture);
    }

    protected ATexture() {
        materialsUsingTexture = Collections.synchronizedList(new CopyOnWriteArrayList<Material>());
    }

    /**
     * Creates a new TextureConfig instance and copies all properties from another TextureConfig object.
     *
     * @param other
     */
    public ATexture(ATexture other) {
        setFrom(other);
    }

    /**
     * Creates a clone.
     */
    public abstract ATexture clone();

    /**
     * Copies every property from another ATexture object.
     *
     * @param other another ATexture object to copy from
     */
    public void setFrom(ATexture other) {
        textureId = other.getTextureId();
        width = other.getWidth();
        height = other.getHeight();
        texelFormat = other.getTexelFormat();
        mipmap = other.isMipmap();
        shouldRecycle = other.willRecycle();
        textureName = other.getTextureName();
        textureType = other.getTextureType();
        wrapType = other.getWrapType();
        filterType = other.getFilterType();
        compressedTexture = other.getCompressedTexture();
        glTextureType = other.getGLTextureType();
        materialsUsingTexture = other.materialsUsingTexture;
    }

    /**
     * @return the texture id
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * @param textureId the texture id to set
     */
    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    /**
     * @return the texture's width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the texture's width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the texture's height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the texture's height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the bitmap format.
     */
    public int getTexelFormat() {
        return texelFormat;
    }

    /**
     * @param texelFormat A bitmap configuration describes how pixels are stored. This affects the quality (color
     *                    depth) as well as the ability to display transparent/translucent colors. It is vital that
     *                    the provided format be in agreement with the rules of
     *                    {@link GLES20#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)} and related
     *                    methods. These rules differ between GL ES 2.x and GL ES 3.x.
     *
     * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
     */
    public void setTexelFormat(@TexelFormat int texelFormat) {
        this.texelFormat = texelFormat;
    }

    /**
     * @return a boolean describing whether this is a mipmap or not.
     */
    public boolean isMipmap() {
        return mipmap;
    }

    /**
     * @param mipmap Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized
     *               collections
     *               of images that accompany a main texture, intended to increase rendering speed and reduce aliasing
     *               artifacts.
     */
    public void setMipmap(boolean mipmap) {
        this.mipmap = mipmap;
    }

    /**
     * @return the a boolean describin whether the source Bitmap or Buffer should be recycled immediately after the
     * OpenGL texture has been created. The main reason for not recycling is Scene caching. Scene caching stores
     * all textures and relevant OpenGL-specific data. This is used when the OpenGL context needs to be
     * restored. The context typically needs to be restored when the application is re-activated or when a live
     * wallpaper is rotated.
     */
    public boolean willRecycle() {
        return shouldRecycle;
    }

    /**
     * @param shouldRecycle Indicates whether the source Bitmap or Buffer should be recycled immediately after the
     *                      OpenGL texture
     *                      has been created. The main reason for not recycling is Scene caching. Scene caching stores
     *                      all
     *                      textures and relevant OpenGL-specific data. This is used when the OpenGL context needs to
     *                      be restored.
     *                      The context typically needs to be restored when the application is re-activated or when a
     *                      live
     *                      wallpaper is rotated.
     */
    public void shouldRecycle(boolean shouldRecycle) {
        this.shouldRecycle = shouldRecycle;
    }

    /**
     * @return The texture name that will be used in the shader.
     */
    public String getTextureName() {
        return textureName;
    }

    /**
     * @param textureName The texture name that will be used in the shader.
     */
    public void setTextureName(String textureName) {
        this.textureName = textureName;
    }

    /**
     * @return The type of texture
     *
     * @see {@link TextureType}
     */
    @TextureType
    public int getTextureType() {
        return textureType;
    }

    /**
     * @return the Texture wrap type.
     *
     * @see {@link WrapType}.
     */
    @WrapType
    public int getWrapType() {
        return wrapType;
    }

    /**
     * @param wrapType the texture wrap type.
     *
     * @see {@link WrapType}.
     */
    public void setWrapType(@WrapType int wrapType) {
        this.wrapType = wrapType;
    }

    /**
     * @return Texture filtering type.
     *
     * @see {@link FilterType}.
     */
    @FilterType
    public int getFilterType() {
        return filterType;
    }

    /**
     * @param filterType Texture filtering type.
     *
     * @see {@link FilterType}.
     */
    public void setFilterType(@FilterType int filterType) {
        this.filterType = filterType;
    }

    public int getGLTextureType() {
        return glTextureType;
    }

    public void setGLTextureType(int glTextureType) {
        this.glTextureType = glTextureType;
    }

    public boolean registerMaterial(Material material) {
        if (isMaterialRegistered(material)) {
            return false;
        }
        materialsUsingTexture.add(material);
        return true;
    }

    public boolean unregisterMaterial(Material material) {
        return materialsUsingTexture.remove(material);
    }

    private boolean isMaterialRegistered(Material material) {
        int count = materialsUsingTexture.size();
        for (int i = 0; i < count; i++) {
            if (materialsUsingTexture.get(i) == material) {
                return true;
            }
        }
        return false;
    }

    public void setInfluence(float influence) {
        this.influence = influence;
    }

    public float getInfluence() {
        return influence;
    }

    public void setRepeatU(float value) {
        repeat[0] = value;
    }

    public float getRepeatU() {
        return repeat[0];
    }

    public void setRepeatV(float value) {
        repeat[1] = value;
    }

    public float getRepeatV() {
        return repeat[1];
    }

    public void setRepeat(float u, float v) {
        repeat[0] = u;
        repeat[1] = v;
    }

    public float[] getRepeat() {
        return repeat;
    }

    public void enableOffset(boolean value) {
        enableOffset = value;
    }

    public boolean offsetEnabled() {
        return enableOffset;
    }

    public void setOffsetU(float value) {
        offset[0] = value;
    }

    public float getOffsetU() {
        return offset[0];
    }

    public float[] getOffset() {
        return offset;
    }

    public void setOffsetV(float value) {
        offset[1] = value;
    }

    public float getOffsetV() {
        return offset[1];
    }

    public void setOffset(float u, float v) {
        offset[0] = u;
        offset[1] = v;
    }

    public void setCompressedTexture(ACompressedTexture compressedTexture) {
        this.compressedTexture = compressedTexture;
    }

    public ACompressedTexture getCompressedTexture() {
        return compressedTexture;
    }

    abstract void add() throws TextureException;

    abstract void remove() throws TextureException;

    abstract void replace() throws TextureException;

    abstract void reset() throws TextureException;
}
