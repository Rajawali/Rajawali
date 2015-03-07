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

import android.opengl.GLES11Ext;

public class AtcTexture extends ACompressedTexture {

	/**
	 * ATC Texture compression format.
	 * 
	 */
	public enum AtcFormat {
		RGB,
		RGBA_EXPLICIT,
		RGBA_INTERPOLATED
	};

	/**
	 * ATC Texture Compression format. See {@link AtcFormat}.
	 */
	protected AtcFormat mAtcFormat;

	public AtcTexture(AtcTexture other)
	{
		super(other);
		setAtcFormat(other.getAtcFormat());
	}

	public AtcTexture(String textureName, ByteBuffer byteBuffer, AtcFormat atcFormat)
	{
		this(textureName, new ByteBuffer[] { byteBuffer }, atcFormat);
	}

	public AtcTexture(String textureName, ByteBuffer[] byteBuffers, AtcFormat atcFormat)
	{
		super(textureName, byteBuffers);
		setCompressionType(CompressionType.ATC);
		setAtcFormat(atcFormat);
	}

	/**
	 * Copies every property from another AtcTexture object
	 * 
	 * @param other
	 *            another AtcTexture object to copy from
	 */
	public void setFrom(AtcTexture other)
	{
		super.setFrom(other);
		mAtcFormat = other.getAtcFormat();
	}

	@Override
	public AtcTexture clone() {
		return new AtcTexture(this);
	}

	/**
	 * @return the ATC Texture Compression format. See {@link AtcFormat}.
	 */
	public AtcFormat getAtcFormat() {
		return mAtcFormat;
	}

	/**
	 * @param atcFormat
	 *            ATC Texture Compression format. See {@link AtcFormat}.
	 */
	public void setAtcFormat(AtcFormat atcFormat) {
		this.mAtcFormat = atcFormat;
		switch (atcFormat) {
		case RGB:
			mCompressionFormat = GLES11Ext.GL_ATC_RGB_AMD;
			break;
		case RGBA_EXPLICIT:
		default:
			mCompressionFormat = GLES11Ext.GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
			break;
		case RGBA_INTERPOLATED:
			mCompressionFormat = GLES11Ext.GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD;
			break;
		}
	}
}
