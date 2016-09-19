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

import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import org.rajawali3d.materials.Material;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ATexture {

	/**
	 * Texture types
	 */
	public enum TextureType {
		DIFFUSE,
		NORMAL,
		SPECULAR,
		ALPHA,
		RENDER_TARGET,
		DEPTH_BUFFER,
		LOOKUP,
		CUBE_MAP,
		SPHERE_MAP,
		VIDEO_TEXTURE,
		COMPRESSED
	}

	/**
	 * You can assign texture coordinates outside the range [0,1] and have them either clamp or repeat in the texture
	 * map. With repeating textures, if you have a large plane with texture coordinates running from 0.0 to 10.0 in both
	 * directions, for example, you'll get 100 copies of the texture tiled together on the screen.
	 */
	public enum WrapType {
		CLAMP,
		REPEAT
	}

	/**
	 * Texture filtering or texture smoothing is the method used to determine the texture color for a texture mapped
	 * pixel, using the colors of nearby texels (pixels of the texture).
	 */
	public enum FilterType {
		NEAREST,
		LINEAR
	}

	/**
	 * The texture id that is used by Rajawali
	 */
	protected int textureId = -1;

	/**
	 * Texture width
	 */
	protected int              width;

	/**
	 * Texture height
	 */
	protected int              height;

	/**
	 * Possible bitmap configurations. A bitmap configuration describes how pixels are stored. This affects the quality
	 * (color depth) as well as the ability to display transparent/translucent colors.
	 *
	 * {@link Config}
	 */
	protected int              bitmapFormat;

	/**
	 * Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized collections of images
	 * that accompany a main texture, intended to increase rendering speed and reduce aliasing artifacts.
	 */
	protected          boolean     mipmap;

	/**
	 * Indicates whether the source Bitmap or Buffer should be recycled immediately after the OpenGL texture has been
	 * created. The main reason for not recycling is Scene caching. Scene caching stores all textures and relevant
	 * OpenGL-specific data. This is used when the OpenGL context needs to be restored. The context typically needs to
	 * be restored when the application is re-activated or when a live wallpaper is rotated.
	 */
	protected          boolean                             shouldRecycle;

	/**
	 * The texture name that will be used in the shader.
	 */
	@NonNull protected String                              textureName;

	/**
	 * The type of texture {link {@link TextureType}
	 */
	protected          TextureType                         textureType;

	/**
	 * Texture wrap type. See {@link WrapType}.
	 */
	protected          WrapType                            wrapType;

	/**
	 * Texture filtering type. See {@link FilterType}.
	 */
	protected FilterType                                   filterType;

	/**
	 * Possible bitmap configurations. A bitmap configuration describes how pixels are stored. This affects the quality
	 * (color depth) as well as the ability to display transparent/translucent colors. See {@link Config}.
	 */
	protected Config                                       bitmapConfig;

	/**
	 * A list of materials that use this texture.
	 */
	protected List<Material>                               materialsUsingTexture;

	/**
	 * The optional compressed texture
	 */
	protected ACompressedTexture compressedTexture;

	/**
	 * The OpenGL texture type
	 */
	protected int glTextureType = GLES20.GL_TEXTURE_2D;

	protected float   influence = 1.0f;
	protected float[] repeat    = new float[] { 1, 1 };
	protected boolean enableOffset;
	protected float[] offset = new float[] { 0, 0 };

	/**
	 * Creates a new ATexture instance with the specified texture type
	 *
	 * @param textureType
	 */
	public ATexture(TextureType textureType, @NonNull String textureName)
	{
		this();
        this.textureType = textureType;
        this.textureName = textureName;
        mipmap = true;
        shouldRecycle = false;
        wrapType = WrapType.REPEAT;
        filterType = FilterType.LINEAR;
	}

	public ATexture(TextureType textureType, @NonNull String textureName, ACompressedTexture compressedTexture)
	{
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
	public ATexture(ATexture other)
	{
		setFrom(other);
	}

	/**
	 * Creates a clone
	 */
	abstract public ATexture clone();

	/**
	 * Copies every property from another ATexture object
	 *
	 * @param other
	 *            another ATexture object to copy from
	 */
	public void setFrom(ATexture other)
	{
        textureId = other.getTextureId();
        width = other.getWidth();
        height = other.getHeight();
        bitmapFormat = other.getBitmapFormat();
        mipmap = other.isMipmap();
        shouldRecycle = other.willRecycle();
        textureName = other.getTextureName();
        textureType = other.getTextureType();
        wrapType = other.getWrapType();
        filterType = other.getFilterType();
        bitmapConfig = other.getBitmapConfig();
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
	 * @param textureId
	 *            the texture id to set
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
	 * @param width
	 *            the texture's width
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
	 * @param height
	 *            the texture's height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the bitmap format.
	 */
	public int getBitmapFormat() {
		return bitmapFormat;
	}

	/**
	 * @param bitmapFormat
	 *            A bitmap configuration describes how pixels are stored. This affects the quality (color depth) as well
	 *            as the ability to display transparent/translucent colors.
	 */
	public void setBitmapFormat(int bitmapFormat) {
		this.bitmapFormat = bitmapFormat;
	}

	/**
	 * @return a boolean describing whether this is a mipmap or not.
	 */
	public boolean isMipmap() {
		return mipmap;
	}

	/**
	 * @param mipmap
	 *            Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized collections
	 *            of images that accompany a main texture, intended to increase rendering speed and reduce aliasing
	 *            artifacts.
	 */
	public void setMipmap(boolean mipmap) {
		this.mipmap = mipmap;
	}

	/**
	 * @return the a boolean describin whether the source Bitmap or Buffer should be recycled immediately after the
	 *         OpenGL texture has been created. The main reason for not recycling is Scene caching. Scene caching stores
	 *         all textures and relevant OpenGL-specific data. This is used when the OpenGL context needs to be
	 *         restored. The context typically needs to be restored when the application is re-activated or when a live
	 *         wallpaper is rotated.
	 */
	public boolean willRecycle() {
		return shouldRecycle;
	}

	/**
	 * @param shouldRecycle
	 *            Indicates whether the source Bitmap or Buffer should be recycled immediately after the OpenGL texture
	 *            has been created. The main reason for not recycling is Scene caching. Scene caching stores all
	 *            textures and relevant OpenGL-specific data. This is used when the OpenGL context needs to be restored.
	 *            The context typically needs to be restored when the application is re-activated or when a live
	 *            wallpaper is rotated.
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
	 * @param textureName
	 *            The texture name that will be used in the shader.
	 */
	public void setTextureName(String textureName) {
		this.textureName = textureName;
	}

	/**
	 * @return The type of texture {link {@link TextureType}
	 */
	public TextureType getTextureType() {
		return textureType;
	}

	/**
	 * @return the Texture wrap type. See {@link WrapType}.
	 */
	public WrapType getWrapType() {
		return wrapType;
	}

	/**
	 * @param wrapType
	 *            the texture wrap type. See {@link WrapType}.
	 */
	public void setWrapType(WrapType wrapType) {
		this.wrapType = wrapType;
	}

	/**
	 * @return Texture filtering type. See {@link FilterType}.
	 */
	public FilterType getFilterType() {
		return filterType;
	}

	/**
	 * @param filterType
	 *            Texture filtering type. See {@link FilterType}.
	 */
	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	/**
	 * @return the Bitmap configuration. A bitmap configuration describes how pixels are stored. This affects the
	 *         quality (color depth) as well as the ability to display transparent/translucent colors. See
	 *         {@link Config}.
	 */
	public Config getBitmapConfig() {
		return bitmapConfig;
	}

	/**
	 * @param bitmapConfig
	 *            the Bitmap configuration. A bitmap configuration describes how pixels are stored. This affects the
	 *            quality (color depth) as well as the ability to display transparent/translucent colors. See
	 *            {@link Config}.
	 */
	public void setBitmapConfig(Config bitmapConfig) {
		this.bitmapConfig = bitmapConfig;
	}

	public int getGLTextureType()
	{
		return glTextureType;
	}

	public void setGLTextureType(int glTextureType) {
        this.glTextureType = glTextureType;
	}

	public boolean registerMaterial(Material material) {
		if(isMaterialRegistered(material)) return false;
		materialsUsingTexture.add(material);
		return true;
	}

	public boolean unregisterMaterial(Material material) {
		return materialsUsingTexture.remove(material);
	}

	private boolean isMaterialRegistered(Material material) {
		int count = materialsUsingTexture.size();
		for(int i=0; i<count; i++)
		{
			if(materialsUsingTexture.get(i) == material)
				return true;
		}
		return false;
	}

	public void setInfluence(float influence)
	{
        this.influence = influence;
	}

	public float getInfluence()
	{
		return influence;
	}

	public void setRepeatU(float value)
	{
        repeat[0] = value;
	}

	public float getRepeatU()
	{
		return repeat[0];
	}

	public void setRepeatV(float value)
	{
        repeat[1] = value;
	}

	public float getRepeatV()
	{
		return repeat[1];
	}

	public void setRepeat(float u, float v)
	{
        repeat[0] = u;
        repeat[1] = v;
	}

	public float[] getRepeat()
	{
		return repeat;
	}

	public void enableOffset(boolean value)
	{
        enableOffset = value;
	}

	public boolean offsetEnabled()
	{
		return enableOffset;
	}

	public void setOffsetU(float value)
	{
        offset[0] = value;
	}

	public float getOffsetU()
	{
		return offset[0];
	}

	public float[] getOffset()
	{
		return offset;
	}

	public void setOffsetV(float value)
	{
        offset[1] = value;
	}

	public float getOffsetV()
	{
		return offset[1];
	}

	public void setOffset(float u, float v)
	{
        offset[0] = u;
        offset[1] = v;
	}

	public void setCompressedTexture(ACompressedTexture compressedTexture)
	{
        this.compressedTexture = compressedTexture;
	}

	public ACompressedTexture getCompressedTexture()
	{
		return compressedTexture;
	}

	abstract void add() throws TextureException;
	abstract void remove() throws TextureException;
	abstract void replace() throws TextureException;
	abstract void reset() throws TextureException;
}
