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
package rajawali.materials.textures;

import java.nio.ByteBuffer;

public class PalettedTexture extends ACompressedTexture {

	// Paletted texture constants
	// Referenced from OpenGL ES 2.0 extension C header from Khronos Group
	// http://www.khronos.org/registry/gles/api/2.0/gl2ext.h
	private static final int GL_PALETTE4_RGB8_OES = 0x8B90;
	private static final int GL_PALETTE4_RGBA8_OES = 0x8B91;
	private static final int GL_PALETTE4_R5_G6_B5_OES = 0x8B92;
	private static final int GL_PALETTE4_RGBA4_OES = 0x8B93;
	private static final int GL_PALETTE4_RGB5_A1_OES = 0x8B94;
	private static final int GL_PALETTE8_RGB8_OES = 0x8B95;
	private static final int GL_PALETTE8_RGBA8_OES = 0x8B96;
	private static final int GL_PALETTE8_R5_G6_B5_OES = 0x8B97;
	private static final int GL_PALETTE8_RGBA4_OES = 0x8B98;
	private static final int GL_PALETTE8_RGB5_A1_OES = 0x8B99;

	/**
	 * Texture palette format.
	 */
	public enum PaletteFormat {
		PALETTE4_RGB8,
		PALETTE4_RGBA8,
		PALETTE4_R5_G6_B5,
		PALETTE4_RGBA4,
		PALETTE4_RGB5_A1,
		PALETTE8_RGB8,
		PALETTE8_RGBA8,
		PALETTE8_R5_G6_B5,
		PALETTE8_RGBA4,
		PALETTE8_RGB5_A1
	};

	/**
	 * Texture palette format. See {@link PaletteFormat}.
	 */
	private PaletteFormat mPaletteFormat;

	public PalettedTexture(PalettedTexture other)
	{
		super(other);
		setPaletteFormat(other.getPaletteFormat());
	}

	public PalettedTexture(String textureName, ByteBuffer byteBuffer, PaletteFormat paletteFormat)
	{
		this(textureName, new ByteBuffer[] { byteBuffer }, paletteFormat);
	}

	public PalettedTexture(String textureName, ByteBuffer[] byteBuffers, PaletteFormat paletteFormat)
	{
		super(textureName, byteBuffers);
		setPaletteFormat(paletteFormat);
		setCompressionType(CompressionType.PALETTED);
	}

	/**
	 * Copies every property from another PalettedTexture object
	 * 
	 * @param other
	 *            another PalettedTexture object to copy from
	 */
	public void setFrom(PalettedTexture other)
	{
		super.setFrom(other);
		mPaletteFormat = other.getPaletteFormat();
	}

	/**
	 * @return the texture palette format
	 */
	public PaletteFormat getPaletteFormat() {
		return mPaletteFormat;
	}

	/**
	 * @param paletteFormat
	 *            the texture palette format
	 */
	public void setPaletteFormat(PaletteFormat paletteFormat) {
		this.mPaletteFormat = paletteFormat;
		checkPaletteFormat();
	}

	public PalettedTexture clone()
	{
		return new PalettedTexture(this);
	}

	/**
	 * Adds and binds paletted texture. Pass in multiple buffer corresponding to different mipmap levels.
	 */
	private void checkPaletteFormat()
	{
		switch (mPaletteFormat) {
		case PALETTE4_RGB8:
			mCompressionFormat = GL_PALETTE4_RGB8_OES;
			break;
		case PALETTE4_RGBA8:
			mCompressionFormat = GL_PALETTE4_RGBA8_OES;
			break;
		case PALETTE4_R5_G6_B5:
			mCompressionFormat = GL_PALETTE4_R5_G6_B5_OES;
			break;
		case PALETTE4_RGBA4:
			mCompressionFormat = GL_PALETTE4_RGBA4_OES;
			break;
		case PALETTE4_RGB5_A1:
			mCompressionFormat = GL_PALETTE4_RGB5_A1_OES;
			break;
		case PALETTE8_RGB8:
			mCompressionFormat = GL_PALETTE8_RGB8_OES;
			break;
		case PALETTE8_RGBA8:
		default:
			mCompressionFormat = GL_PALETTE8_RGBA8_OES;
			break;
		case PALETTE8_R5_G6_B5:
			mCompressionFormat = GL_PALETTE8_R5_G6_B5_OES;
			break;
		case PALETTE8_RGBA4:
			mCompressionFormat = GL_PALETTE8_RGBA4_OES;
			break;
		case PALETTE8_RGB5_A1:
			mCompressionFormat = GL_PALETTE8_RGB5_A1_OES;
			break;
		}
	}
}
