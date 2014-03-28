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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rajawali.util.RajLog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.util.Log;

public class Etc1Texture extends ACompressedTexture {

	protected int mResourceId;
	protected Bitmap mBitmap;

	public Etc1Texture(String textureName)
	{
		super(textureName);
		mCompressionType = CompressionType.ETC1;
		mCompressionFormat = ETC1.ETC1_RGB8_OES;
	}

	public Etc1Texture(int resourceId)
	{
		this(TextureManager.getInstance().getContext().getResources().getResourceName(resourceId));
		setResourceId(resourceId);
	}

	public Etc1Texture(String textureName, int resourceId, Bitmap fallbackTexture)
	{
		this(textureName);
		Context context = TextureManager.getInstance().getContext();
		setInputStream(context.getResources().openRawResource(resourceId), fallbackTexture);
	}

	public Etc1Texture(String textureName, int[] resourceIds)
	{
		this(textureName);
		setResourceIds(resourceIds);
	}

	public Etc1Texture(String textureName, ByteBuffer byteBuffer)
	{
		this(textureName);
		setByteBuffer(byteBuffer);
	}

	public Etc1Texture(String textureName, ByteBuffer[] byteBuffers)
	{
		this(textureName);
		setByteBuffers(byteBuffers);
	}

	public Etc1Texture(String textureName, InputStream compressedTexture, Bitmap fallbackTexture)
	{
		this(textureName);
		setInputStream(compressedTexture, fallbackTexture);
	}

	public Etc1Texture(Etc1Texture other) {
		super();
		setFrom(other);
	}

	@Override
	public Etc1Texture clone() {
		return new Etc1Texture(this);
	}

	@Override
	void add() throws TextureException {
		super.add();
		if (mShouldRecycle)
		{
			if (mBitmap != null)
			{
				mBitmap.recycle();
				mBitmap = null;
			}
		}
	}

	@Override
	void reset() throws TextureException {
		super.reset();
		if (mBitmap != null)
		{
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	public void setResourceId(int resourceId) {
		mResourceId = resourceId;
		Resources resources = TextureManager.getInstance().getContext().getResources();
		try {
			ETC1Util.ETC1Texture texture = ETC1Util.createTexture(resources.openRawResource(resourceId));
			mByteBuffers = new ByteBuffer[] { texture.getData() };
			setWidth(texture.getWidth());
			setHeight(texture.getHeight());
			setCompressionFormat(ETC1.ETC1_RGB8_OES);
		} catch (IOException e) {
			RajLog.e(e.getMessage());
			e.printStackTrace();
		}
	}

	public int getResourceId()
	{
		return mResourceId;
	}

	public void setResourceIds(int[] resourceIds)
	{
		ByteBuffer[] mipmapChain = new ByteBuffer[resourceIds.length];
		Resources resources = TextureManager.getInstance().getContext().getResources();
		int mip_0_width = 1, mip_0_height = 1;
		try {
			for (int i = 0, length = resourceIds.length; i < length; i++) {
				ETC1Util.ETC1Texture texture = ETC1Util.createTexture(resources.openRawResource(resourceIds[i]));
				mipmapChain[i] = texture.getData();
				if (i == 0) {
					mip_0_width = texture.getWidth();
					mip_0_height = texture.getHeight();
				}
			}
			setWidth(mip_0_width);
			setHeight(mip_0_height);
			setCompressionFormat(ETC1.ETC1_RGB8_OES);
		} catch (IOException e) {
			RajLog.e(e.getMessage());
			e.printStackTrace();
		}

		mByteBuffers = mipmapChain;
	}
	
	public void setBitmap(Bitmap bitmap)
	{
		mBitmap = bitmap;
		int imageSize = bitmap.getRowBytes() * bitmap.getHeight();
		ByteBuffer uncompressedBuffer = ByteBuffer.allocateDirect(imageSize);
		bitmap.copyPixelsToBuffer(uncompressedBuffer);
		uncompressedBuffer.position(0);

		ByteBuffer compressedBuffer = ByteBuffer.allocateDirect(
				ETC1.getEncodedDataSize(bitmap.getWidth(), bitmap.getHeight())).order(ByteOrder.nativeOrder());
		ETC1.encodeImage(uncompressedBuffer, bitmap.getWidth(), bitmap.getHeight(), 2, 2 * bitmap.getWidth(),
				compressedBuffer);

		mByteBuffers = new ByteBuffer[] { compressedBuffer };
		setWidth(bitmap.getWidth());
		setHeight(bitmap.getHeight());
	}

	public void setInputStream(InputStream compressedTexture, Bitmap fallbackTexture)
	{
		ETC1Util.ETC1Texture texture = null;

		try {
			texture = ETC1Util.createTexture(compressedTexture);
		} catch (IOException e) {
			Log.e("addEtc1Texture", e.getMessage());
		} finally {
			if (texture == null) {
				setBitmap(fallbackTexture);
				Log.d("ETC1", "Falling back to uncompressed texture");
			} else {
				setByteBuffer(texture.getData());
				setWidth(texture.getWidth());
				setHeight(texture.getHeight());
				Log.d("ETC1", "ETC1 texture load successful");
			}
		}
	}
}
