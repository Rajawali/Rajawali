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
package org.rajawali3d.textures;

import android.opengl.GLES20;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import org.rajawali3d.materials.Material;
import org.rajawali3d.textures.annotation.Filter;
import org.rajawali3d.textures.annotation.Filter.FilterType;
import org.rajawali3d.textures.annotation.TexelFormat;
import org.rajawali3d.textures.annotation.TextureTarget;
import org.rajawali3d.textures.annotation.Type.TextureType;
import org.rajawali3d.textures.annotation.Wrap;
import org.rajawali3d.textures.annotation.Wrap.WrapType;

import java.nio.Buffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract texture class.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
//TODO: Build interface that allows textures to notify materials of relevant changes.
//TODO: Handle repeaat/offset functions
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
     * increase the amount of time for an initial texture push as well as the size of the texture (in video RAM), but
     * can dramatically improve render quality.
     */
    protected boolean mipmaped;

    /**
     * Indicates whether the source {@link TextureDataReference} should be recycled immediately after the OpenGL
     * texture
     * has been created. The main reason for not recycling is scene caching. Scene caching stores all textures and
     * relevant OpenGL-specific data. This is used when the OpenGL context needs to be restored. The context typically
     * needs to be restored when the application is re-activated or when a live wallpaper is rotated.
     */
    protected boolean shouldRecycle;

    /**
     * The texture name that will be used in the shader.
     */
    @NonNull
    protected String textureName;

    /**
     * The type of texture
     *
     * @see {@link TextureType}.
     */
    @TextureType
    protected int textureType;

    /**
     * Texture wrap type.
     *
     * @see {@link WrapType}.
     */
    @WrapType
    protected int wrapType;

    /**
     * Texture filtering type.
     *
     * @see {@link FilterType}
     */
    @FilterType
    protected int filterType;

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
    @TextureTarget
    protected int textureTarget = GLES20.GL_TEXTURE_2D;

    /**
     * Percentage influence this texture has on the final pixel color. Must be between 0 and 1. If the sum of all
     * influences does not equal 1.0, the individual influences will be normalized.
     */
    @FloatRange(from = 0, to = 1)
    protected float influence = 1.0f;
    protected float[] repeat = new float[]{1, 1};
    protected boolean enableOffset;
    protected float[] offset = new float[]{0, 0};

    // TODO: Is the synchronized list necessary with copy on write list?
    private final List<Material> registeredMaterials = Collections.synchronizedList(
            new CopyOnWriteArrayList<Material>());

    /**
     * Creates a new texture instance with the specified texture type with {@link GLES20#GL_REPEAT} texture wrapping on
     * all axes and {@link GLES20#GL_LINEAR} filtering.
     *
     * @param textureType {@link TextureType} The Rajawali texture type.
     * @param textureName The name of the texture. This name will be used in the shader code to reference this texture.
     */
    public ATexture(@TextureType int textureType, @NonNull String textureName) {
        this();
        this.textureType = textureType;
        this.textureName = textureName;
        mipmaped = true;
        shouldRecycle = false;
        wrapType = Wrap.REPEAT_S | Wrap.REPEAT_T | Wrap.REPEAT_R;
        filterType = Filter.LINEAR;
    }

    /**
     * Creates a new texture from the provided compressed texture data for the specified texture type with
     * {@link GLES20#GL_REPEAT} texture wrapping on all axes and {@link GLES20#GL_LINEAR} filtering.
     *
     * @param textureType       {@link TextureType} The Rajawali texture type.
     * @param textureName       The name of the texture. This name will be used in the shader code to reference this
     *                          texture.
     * @param compressedTexture {@link ACompressedTexture} The compressed texture this texture is built from.
     */
    public ATexture(@TextureType int textureType, @NonNull String textureName, ACompressedTexture compressedTexture) {
        this(textureType, textureName);
        setCompressedTexture(compressedTexture);
    }

    protected ATexture() {
        // TODO: Can we remove this constructor?
        textureName = "noName";
    }

    /**
     * Creates a new texture instance and copies all properties from another texture object.
     *
     * @param other The {@link ATexture} to copy from.
     */
    public ATexture(ATexture other) {
        setFrom(other);
    }

    /**
     * Creates a clone of this texture.
     */
    public abstract ATexture clone();

    /**
     * Copies every property from another {@link ATexture} object.
     *
     * @param other The {@link ATexture} to copy from.
     */
    public void setFrom(ATexture other) {
        textureId = other.getTextureId();
        width = other.getWidth();
        height = other.getHeight();
        texelFormat = other.getTexelFormat();
        mipmaped = other.isMipmaped();
        shouldRecycle = other.willRecycle();
        textureName = other.getTextureName();
        textureType = other.getTextureType();
        wrapType = other.getWrapType();
        filterType = other.getFilterType();
        compressedTexture = other.getCompressedTexture();
        textureTarget = other.getTextureTarget();
        influence = other.getInfluence();
        materialsUsingTexture = other.getRegisteredMaterials();
    }

    /**
     * Retrieves the id assigned to this {@link ATexture} by the render context.
     *
     * @return {@code int} The texture id.
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * Sets the id assigned to this {@link ATexture} by the render context.
     *
     * @param textureId {@code int} The texture id.
     */
    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    /**
     * Fetches the configured width of this {@link ATexture}.
     *
     * @return {@code int} The width in texels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the configured width of this {@link ATexture}. Once the {@link ATexture} has been pushed, using this method
     * require a update push to the GPU.
     *
     * @param width {@code int} The width in texels.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Fetches the configured height of this {@link ATexture}.
     *
     * @return {@code int} The height in texels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the configured height of this {@link ATexture}. Once the {@link ATexture} has been pushed, using this
     * method require a update push to the GPU.
     *
     * @param height {@code int} The height in texels.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Fetches the internal texel format of this {@link ATexture}.
     *
     * @return {@link TexelFormat} The internal texel format.
     * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
     */
    @TexelFormat
    public int getTexelFormat() {
        return texelFormat;
    }

    /**
     * Sets the internal texel format of this {@link ATexture}. This affects the quality (color depth) as well as the
     * ability to display transparent/translucent colors. It is vital that the provided format be in agreement with the
     * rules of {@link GLES20#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)} and related methods. These
     * rules differ between GL ES 2.x and GL ES 3.x.
     *
     * @param texelFormat {@link TexelFormat} The internal texel format.
     *
     * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
     */
    public void setTexelFormat(@TexelFormat int texelFormat) {
        this.texelFormat = texelFormat;
    }

    /**
     * Fetches whether or not this {@link ATexture} is configured to auto-generate mipmaps. Mipmaps are pre-calculated,
     * optimized collections of images that accompany a main texture, intended to increase rendering speed and reduce
     * aliasing artifacts.
     *
     * @return {@code true} if mipmaps should be auto-generated.
     */
    public boolean isMipmaped() {
        return mipmaped;
    }

    /**
     * Sets whether or not this {@link ATexture} is configured to auto-generate mipmaps. Mipmaps are pre-calculated,
     * optimized collections of images that accompany a main texture, intended to increase rendering speed and reduce
     * aliasing artifacts.
     *
     * @param mipmap {@code true} if mipmaps should be auto-generated.
     */
    public void setMipmaped(boolean mipmap) {
        this.mipmaped = mipmap;
    }

    /**
     * Fetches whether the source {@link TextureDataReference} should be recycled immediately after the texture has
     * been
     * created. The main reason for not recycling is Scene caching. Scene caching stores all textures and relevant
     * render context specific data. This is used when the render context needs to be restored. The context typically
     * needs to be restored when the application is re-activated or when a live wallpaper is rotated.
     *
     * @return {@code true} if the data will be recycled.
     */
    public boolean willRecycle() {
        return shouldRecycle;
    }

    /**
     * Fetches whether the source {@link TextureDataReference} should be recycled immediately after the texture has
     * been created. The main reason for not recycling is Scene caching. Scene caching stores all textures and relevant
     * render context specific data. This is used when the render context needs to be restored. The context typically
     * needs to be restored when the application is re-activated or when a live wallpaper is rotated.
     *
     * @return {@code true} if the data will be recycled.
     */
    public void shouldRecycle(boolean shouldRecycle) {
        this.shouldRecycle = shouldRecycle;
    }

    /**
     * Fetches the name of this {@link ATexture}. This name will be used in the shader code to reference this texture.
     *
     * @return {@link String} The texture name.
     */
    public String getTextureName() {
        return textureName;
    }

    /**
     * Sets the name of this {@link ATexture}. This name will be used in the shader code to reference this texture.
     *
     * @param textureName {@link String} The texture name.
     */
    public void setTextureName(String textureName) {
        this.textureName = textureName;
    }

    /**
     * Fetches the Rajawali texture type of this {@link ATexture}. These types determine how the engine treats the
     * texture internally. For example, as a diffuse color texture, a normal map, or a data lookup.
     *
     * @return {@link TextureType} The texture type.
     * @see {@link TextureType}
     */
    @TextureType
    public int getTextureType() {
        return textureType;
    }

    /**
     * Fetches the wrapping configuration for this {@link ATexture}. Wrapping determines how the GPU will handle
     * texture
     * coordinates outside the range of [0, 1].
     *
     * @return {@link WrapType} The wrapping configuration.
     * @see {@link WrapType}
     */
    @WrapType
    public int getWrapType() {
        return wrapType;
    }

    /**
     * Sets the wrapping configuration for this {@link ATexture}. Wrapping determines how the GPU will handle texture
     * coordinates outside the range of [0, 1].
     *
     * @param wrapType {@link WrapType} The wrapping configuration.
     *
     * @see {@link WrapType}.
     */
    public void setWrapType(@WrapType int wrapType) {
        this.wrapType = wrapType;
    }

    /**
     * Fetches the filtering configuration for this {@link ATexture}. Filtering determines how the GPU will interpolate
     * texel data when looking up for an individual pixel.
     *
     * @return {@link FilterType} The filtering configuration.
     * @see {@link FilterType}.
     */
    @FilterType
    public int getFilterType() {
        return filterType;
    }

    /**
     * Sets the filtering configuration for this {@link ATexture}. Filtering determines how the GPU will interpolate
     * texel data when looking up for an individual pixel.
     *
     * @param filterType {@link FilterType} The filtering configuration.
     *
     * @see {@link FilterType}.
     */
    public void setFilterType(@FilterType int filterType) {
        this.filterType = filterType;
    }

    /**
     * Fetches the render context texture target for this {@link ATexture}.
     *
     * @return {@link TextureTarget} The target of this texture in the render context.
     */
    @TextureTarget
    public int getTextureTarget() {
        return textureTarget;
    }

    /**
     * Sets the render context texture target for this {@link ATexture}.
     *
     * @param target {@link TextureTarget} The render context texture target.
     */
    public void setTextureTarget(@TextureTarget int target) {
        this.textureTarget = target;
    }

    /**
     * Registers a {@link Material} with this {@link ATexture}. This is used to track which materials might need to be
     * updated if changes are made to this texture.
     *
     * @param material {@link Material} The material utilizing this {@link ATexture}.
     *
     * @return {@code true} If the material was registered. {@code false} If the material was already registered.
     */
    public boolean registerMaterial(@NonNull Material material) {
        if (isMaterialRegistered(material)) {
            return false;
        }
        materialsUsingTexture.add(material);
        return true;
    }

    /**
     * Unregisters a {@link Material} with this {@link ATexture}.
     *
     * @param material The {@link Material} to unregister.
     *
     * @return {@code true} if the {@link Material} was registered.
     */
    public boolean unregisterMaterial(@NonNull Material material) {
        return materialsUsingTexture.remove(material);
    }

    /**
     * Retrieves the list of {@link Material}s registered with this {@link ATexture}.
     *
     * @return The list of registered {@link Material}s.
     */
    @NonNull
    public List<Material> getRegisteredMaterials() {
        return registeredMaterials;
    }

    /**
     * Checks is a {@link Material} is registered with this {@link ATexture}.
     *
     * @param material The {@link Material} to check.
     *
     * @return {@code true} if the {@link Material} is registered.
     */
    private boolean isMaterialRegistered(@NonNull Material material) {
        int count = materialsUsingTexture.size();
        for (int i = 0; i < count; i++) {
            if (materialsUsingTexture.get(i) == material) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the influence of this {@link ATexture} on the final color. If this {@link ATexture} is not used for
     * coloring
     * a pixel, this will have no effect.
     *
     * @param influence {@code float} Percentage influence this texture has on the final pixel color. Must be between 0
     *                  and 1. If the sum of all influences does not equal 1.0, the individual influences
     *                  will be normalized.
     */
    public void setInfluence(@FloatRange(from = 0, to = 1) float influence) {
        this.influence = influence;
    }

    /**
     * Fetches the influence of this {@link ATexture} on the final color. If this {@link ATexture} is not used for
     * coloring a pixel, this has no effect.
     *
     * @return {@code float} The influence.
     */
    @FloatRange(from = 0, to = 1)
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
