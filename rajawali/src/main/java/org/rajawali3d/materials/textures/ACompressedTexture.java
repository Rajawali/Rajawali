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

import java.nio.ByteBuffer;

import android.opengl.GLES20;

import org.rajawali3d.util.RajLog;

public abstract class ACompressedTexture extends ATexture {

	protected ByteBuffer[] mByteBuffers;

	/**
	 * Texture compression type. Texture compression can significantly increase the performance by reducing memory
	 * requirements and making more efficient use of memory bandwidth.
	 */
	public enum CompressionType {
		NONE,
		ETC1,
        ETC2,
		PALETTED,
		THREEDC,
		ATC,
		DXT1,
		PVRTC
	};

	/**
	 * Texture compression type
	 */
	protected CompressionType mCompressionType;
	/**
	 * Bitmap compression format. Use together with {@link CompressionType}
	 */
	protected int mCompressionFormat;

	protected ACompressedTexture() {
		super();
		mTextureType = TextureType.COMPRESSED;
		mWrapType = WrapType.REPEAT;
	}

	public ACompressedTexture(ACompressedTexture other)
	{
		this();
		setFrom(other);
	}

	public ACompressedTexture(String textureName)
	{
		this();
		mTextureType = TextureType.COMPRESSED;
		mTextureName = textureName;
	}

	public ACompressedTexture(String textureName, ByteBuffer byteBuffer)
	{
		this(textureName);
		setByteBuffer(byteBuffer);
	}

	public ACompressedTexture(String textureName, ByteBuffer[] byteBuffers)
	{
		this(textureName);
		setByteBuffers(byteBuffers);
	}

	/**
	 * Copies every property from another ACompressedTexture object
	 * 
	 * @param other
	 *            another ACompressedTexture object to copy from
	 */
	public void setFrom(ACompressedTexture other)
	{
		super.setFrom(other);
		mCompressionType = other.getCompressionType();
		mCompressionFormat = other.getCompressionFormat();
	}

	/**
	 * @return the texture compression type
	 */
	public CompressionType getCompressionType() {
		return mCompressionType;
	}

	/**
	 * @param compressionType
	 *            the texture compression type
	 */
	public void setCompressionType(CompressionType compressionType) {
		this.mCompressionType = compressionType;
	}

	/**
	 * @return the Bitmap compression format
	 */
	public int getCompressionFormat() {
		return mCompressionFormat;
	}

	/**
	 * @param internalFormat
	 *            the Bitmap compression format
	 */
	public void setCompressionFormat(int compressionFormat) {
		this.mCompressionFormat = compressionFormat;
	}

	public void setByteBuffer(ByteBuffer byteBuffer) {
		setByteBuffers(new ByteBuffer[] { byteBuffer });
	}

	public void setByteBuffers(ByteBuffer[] byteBuffers) {
		mByteBuffers = byteBuffers;
	}

	void add() throws TextureException
	{
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		if (textureId > 0)
		{
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

			if (mFilterType == FilterType.LINEAR)
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			else
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

			if (mFilterType == FilterType.LINEAR)
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			else
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			if (mWrapType == WrapType.REPEAT) {
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			} else {
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			}
			if ((mByteBuffers != null && mByteBuffers.length == 0) || mByteBuffers == null) {
				GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, 0, mCompressionFormat, mWidth, mHeight, 0, 0, null);
			} else {
				int w = mWidth, h = mHeight;
				for (int i = 0; i < mByteBuffers.length; i++) {
					GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, i, mCompressionFormat, w, h, 0,
							mByteBuffers[i].capacity(), mByteBuffers[i]);
					w = w > 1 ? w / 2 : 1;
					h = h > 1 ? h / 2 : 1;
				}
			}
			setTextureId(textureId);
		} else {
			throw new TextureException("Couldn't generate a texture name.");
		}

		for (int i = 0; i < mByteBuffers.length; i++) {
			if (mByteBuffers[i] != null) {
				mByteBuffers[i].limit(0);
			}
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	void remove() throws TextureException
	{
		GLES20.glDeleteTextures(1, new int[] { mTextureId }, 0);
	}

	void replace() throws TextureException
	{
		if (mByteBuffers == null || mByteBuffers.length == 0)
			throw new TextureException("Texture could not be replaced because there is no ByteBuffer set.");

		if (mWidth == 0 || mHeight == 0)
			throw new TextureException(
					"Could not update ByteBuffer texture. One or more of the following properties haven't been set: width or height");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
		int w = mWidth, h = mHeight;
		for (int i = 0; i < mByteBuffers.length; i++) {
			GLES20.glCompressedTexSubImage2D(GLES20.GL_TEXTURE_2D, i, 0, 0, w, h, mCompressionFormat,
					mByteBuffers[i].capacity(), mByteBuffers[i]);
			w = w > 1 ? w / 2 : 1;
			h = h > 1 ? h / 2 : 1;
		}
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	void reset() throws TextureException
	{
		if (mByteBuffers != null)
		{
			for (int i = 0; i < mByteBuffers.length; i++) {
				if (mByteBuffers[i] != null) {
					mByteBuffers[i].limit(0);
				}
			}
		}
	}
}
