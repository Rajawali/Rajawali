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

import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture.FilterType;
import org.rajawali3d.materials.textures.ATexture.TextureType;
import org.rajawali3d.materials.textures.ATexture.WrapType;
import org.rajawali3d.materials.textures.TextureException;
import org.rajawali3d.renderer.Renderer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//TODO: Remove this class during integration of new code in favor of old
public abstract class ATexture {

	/**
	 * The texture id that is used by Rajawali
	 */
	protected int mTextureId = -1;
	/**
	 * Texture width
	 */
	protected          int                mWidth;
	/**
	 * Texture height
	 */
	protected          int                mHeight;
	/**
	 * Possible bitmap configurations. A bitmap configuration describes how pixels are stored. This affects the quality
	 * (color depth) as well as the ability to display transparent/translucent colors.
	 *
	 * {@link Config}
	 */
	protected          int                mBitmapFormat;
	/**
	 * Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized collections of images
	 * that accompany a main texture, intended to increase rendering speed and reduce aliasing artifacts.
	 */
	protected          boolean     mMipmap;
	/**
	 * Indicates whether the source Bitmap or Buffer should be recycled immediately after the OpenGL texture has been
	 * created. The main reason for not recycling is Scene caching. Scene caching stores all textures and relevant
	 * OpenGL-specific data. This is used when the OpenGL context needs to be restored. The context typically needs to
	 * be restored when the application is re-activated or when a live wallpaper is rotated.
	 */
	protected          boolean            mShouldRecycle;
	/**
	 * The texture name that will be used in the shader.
	 */
	@NonNull protected String             mTextureName;
	/**
	 * The type of texture {link {@link TextureType}
	 */
	protected          TextureType        mTextureType;
	/**
	 * Texture wrap type. See {@link WrapType}.
	 */
	protected          WrapType           mWrapType;
	/**
	 * Texture filtering type. See {@link FilterType}.
	 */
	protected          FilterType         mFilterType;
	/**
	 * Possible bitmap configurations. A bitmap configuration describes how pixels are stored. This affects the quality
	 * (color depth) as well as the ability to display transparent/translucent colors. See {@link Config}.
	 */
	protected          Config             mBitmapConfig;
	/**
	 * A list of materials that use this texture.
	 */
	protected          List<Material>     mMaterialsUsingTexture;
	/**
	 * The optional compressed texture
	 */
	protected          ACompressedTexture mCompressedTexture;
	/**
	 * The OpenGL texture type
	 */
	protected int mGLTextureType = GLES20.GL_TEXTURE_2D;
	/**
	 * This texture's unique owner identity String. This is usually the
	 * fully qualified name of the {@link Renderer} instance.
	 */
	protected String mOwnerIdentity;
	protected float mInfluence = 1.0f;
	protected float[] mRepeat = new float[] { 1, 1 };
	protected boolean mEnableOffset;
	protected float[] mOffset = new float[] { 0, 0 };

	/**
	 * Creates a new ATexture instance with the specified texture type
	 *
	 * @param textureType
	 */
	public ATexture(TextureType textureType, @NonNull String textureName)
	{
		this();
		mTextureType = textureType;
		mTextureName = textureName;
		mMipmap = true;
		mShouldRecycle = false;
		mWrapType = WrapType.REPEAT;
		mFilterType = FilterType.LINEAR;
	}

	public ATexture(TextureType textureType, @NonNull String textureName, ACompressedTexture compressedTexture)
	{
		this(textureType, textureName);
		setCompressedTexture(compressedTexture);
	}

	protected ATexture() {
		mMaterialsUsingTexture = Collections.synchronizedList(new CopyOnWriteArrayList<Material>());
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
		mTextureId = other.getTextureId();
		mWidth = other.getWidth();
		mHeight = other.getHeight();
		mBitmapFormat = other.getBitmapFormat();
		mMipmap = other.isMipmap();
		mShouldRecycle = other.willRecycle();
		mTextureName = other.getTextureName();
		mTextureType = other.getTextureType();
		mWrapType = other.getWrapType();
		mFilterType = other.getFilterType();
		mBitmapConfig = other.getBitmapConfig();
		mCompressedTexture = other.getCompressedTexture();
		mGLTextureType = other.getGLTextureType();
		mMaterialsUsingTexture = other.mMaterialsUsingTexture;
	}

	/**
	 * @return the texture id
	 */
	public int getTextureId() {
		return mTextureId;
	}

	/**
	 * @param textureId
	 *            the texture id to set
	 */
	public void setTextureId(int textureId) {
		mTextureId = textureId;
	}

	/**
	 * @return the texture's width
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * @param width
	 *            the texture's width
	 */
	public void setWidth(int width) {
		this.mWidth = width;
	}

	/**
	 * @return the texture's height
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * @param height
	 *            the texture's height
	 */
	public void setHeight(int height) {
		this.mHeight = height;
	}

	/**
	 * @return the bitmap format.
	 */
	public int getBitmapFormat() {
		return mBitmapFormat;
	}

	/**
	 * @param bitmapFormat
	 *            A bitmap configuration describes how pixels are stored. This affects the quality (color depth) as well
	 *            as the ability to display transparent/translucent colors.
	 */
	public void setBitmapFormat(int bitmapFormat) {
		this.mBitmapFormat = bitmapFormat;
	}

	/**
	 * @return a boolean describing whether this is a mipmap or not.
	 */
	public boolean isMipmap() {
		return mMipmap;
	}

	/**
	 * @param mipmap
	 *            Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized collections
	 *            of images that accompany a main texture, intended to increase rendering speed and reduce aliasing
	 *            artifacts.
	 */
	public void setMipmap(boolean mipmap) {
		this.mMipmap = mipmap;
	}

	/**
	 * @return the a boolean describin whether the source Bitmap or Buffer should be recycled immediately after the
	 *         OpenGL texture has been created. The main reason for not recycling is Scene caching. Scene caching stores
	 *         all textures and relevant OpenGL-specific data. This is used when the OpenGL context needs to be
	 *         restored. The context typically needs to be restored when the application is re-activated or when a live
	 *         wallpaper is rotated.
	 */
	public boolean willRecycle() {
		return mShouldRecycle;
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
		this.mShouldRecycle = shouldRecycle;
	}

	/**
	 * @return The texture name that will be used in the shader.
	 */
	public String getTextureName() {
		return mTextureName;
	}

	/**
	 * @param textureName
	 *            The texture name that will be used in the shader.
	 */
	public void setTextureName(String textureName) {
		this.mTextureName = textureName;
	}

	/**
	 * @return The type of texture {link {@link TextureType}
	 */
	public TextureType getTextureType() {
		return mTextureType;
	}

	/**
	 * @return the Texture wrap type. See {@link WrapType}.
	 */
	public WrapType getWrapType() {
		return mWrapType;
	}

	/**
	 * @param wrapType
	 *            the texture wrap type. See {@link WrapType}.
	 */
	public void setWrapType(WrapType wrapType) {
		this.mWrapType = wrapType;
	}

	/**
	 * @return Texture filtering type. See {@link FilterType}.
	 */
	public FilterType getFilterType() {
		return mFilterType;
	}

	/**
	 * @param filterType
	 *            Texture filtering type. See {@link FilterType}.
	 */
	public void setFilterType(FilterType filterType) {
		this.mFilterType = filterType;
	}

	/**
	 * @return the Bitmap configuration. A bitmap configuration describes how pixels are stored. This affects the
	 *         quality (color depth) as well as the ability to display transparent/translucent colors. See
	 *         {@link Config}.
	 */
	public Config getBitmapConfig() {
		return mBitmapConfig;
	}

	/**
	 * @param bitmapConfig
	 *            the Bitmap configuration. A bitmap configuration describes how pixels are stored. This affects the
	 *            quality (color depth) as well as the ability to display transparent/translucent colors. See
	 *            {@link Config}.
	 */
	public void setBitmapConfig(Config bitmapConfig) {
		this.mBitmapConfig = bitmapConfig;
	}

	public int getGLTextureType()
	{
		return mGLTextureType;
	}

	public void setGLTextureType(int glTextureType) {
		mGLTextureType = glTextureType;
	}

	public void setOwnerIdentity(String identity)
	{
		mOwnerIdentity = identity;
	}

	public String getOwnerIdentity()
	{
		return mOwnerIdentity;
	}

	public boolean registerMaterial(Material material) {
		if(isMaterialRegistered(material)) return false;
		mMaterialsUsingTexture.add(material);
		return true;
	}

	public boolean unregisterMaterial(Material material) {
		return mMaterialsUsingTexture.remove(material);
	}

	private boolean isMaterialRegistered(Material material) {
		int count = mMaterialsUsingTexture.size();
		for(int i=0; i<count; i++)
		{
			if(mMaterialsUsingTexture.get(i) == material)
				return true;
		}
		return false;
	}

	public void setInfluence(float influence)
	{
		mInfluence = influence;
	}

	public float getInfluence()
	{
		return mInfluence;
	}

	public void setRepeatU(float value)
	{
		mRepeat[0] = value;
	}

	public float getRepeatU()
	{
		return mRepeat[0];
	}

	public void setRepeatV(float value)
	{
		mRepeat[1] = value;
	}

	public float getRepeatV()
	{
		return mRepeat[1];
	}

	public void setRepeat(float u, float v)
	{
		mRepeat[0] = u;
		mRepeat[1] = v;
	}

	public float[] getRepeat()
	{
		return mRepeat;
	}

	public void enableOffset(boolean value)
	{
		mEnableOffset = value;
	}

	public boolean offsetEnabled()
	{
		return mEnableOffset;
	}

	public void setOffsetU(float value)
	{
		mOffset[0] = value;
	}

	public float getOffsetU()
	{
		return mOffset[0];
	}

	public float[] getOffset()
	{
		return mOffset;
	}

	public void setOffsetV(float value)
	{
		mOffset[1] = value;
	}

	public float getOffsetV()
	{
		return mOffset[1];
	}

	public void setOffset(float u, float v)
	{
		mOffset[0] = u;
		mOffset[1] = v;
	}

	public void setCompressedTexture(ACompressedTexture compressedTexture)
	{
		mCompressedTexture = compressedTexture;
	}

	public ACompressedTexture getCompressedTexture()
	{
		return mCompressedTexture;
	}

	abstract void add() throws TextureException;
	abstract void remove() throws TextureException;
	abstract void replace() throws TextureException;
	abstract void reset() throws TextureException;
}
