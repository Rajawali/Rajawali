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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;

import c.org.rajawali3d.textures.annotation.Type.TextureType;


public abstract class AMultiTexture extends BaseTexture {
	protected Bitmap[] mBitmaps;
	protected ByteBuffer[] mByteBuffers;
    protected CompressedTexture2D[] mCompressedTextures;
	protected int[] mResourceIds;

	protected AMultiTexture() {
		super();
	}

	public AMultiTexture(@TextureType int textureType, String textureName) {
		super(textureType, textureName);
	}

	public AMultiTexture(@TextureType int textureType, String textureName, int[] resourceIds)
	{
		super(textureType, textureName);
		setResourceIds(resourceIds);
	}

	public AMultiTexture(@TextureType int textureType, String textureName, Bitmap[] bitmaps)
	{
		super(textureType, textureName);
		setBitmaps(bitmaps);
	}

	public AMultiTexture(@TextureType int textureType, String textureName, ByteBuffer[] byteBuffers)
	{
		super(textureType, textureName);
		setByteBuffers(byteBuffers);
	}

    public AMultiTexture(@TextureType int textureType, String textureName, CompressedTexture2D[] compressedTexture2Ds)
    {
        super(textureType, textureName);
        setCompressedTextures(compressedTexture2Ds);
    }

	public AMultiTexture(BaseTexture other) {
		super(other);
	}

	/**
	 * Copies every property from another AMultiTexture object
	 *
	 * @param other
	 *            another AMultiTexture object to copy from
	 */
	public void setFrom(AMultiTexture other)
	{
		super.setFrom(other);
		setBitmaps(mBitmaps);
		setResourceIds(mResourceIds);
		setByteBuffers(mByteBuffers);
	}

	public void setResourceIds(int[] resourceIds)
	{
		mResourceIds = resourceIds;
		int numResources = resourceIds.length;
		mBitmaps = new Bitmap[numResources];
		Context context = org.rajawali3d.materials.textures.TextureManager.getInstance().getContext();

		for(int i=0; i<numResources; i++)
		{
			mBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), resourceIds[i]);
		}
	}

	public int[] getResourceIds()
	{
		return mResourceIds;
	}

	public void setBitmaps(Bitmap[] bitmaps)
	{
		mBitmaps = bitmaps;
	}

	public Bitmap[] getBitmaps()
	{
		return mBitmaps;
	}

	public void setByteBuffers(ByteBuffer[] byteBuffers)
	{
		mByteBuffers = byteBuffers;
	}

	public ByteBuffer[] getByteBuffers()
	{
		return mByteBuffers;
	}

    public CompressedTexture2D[] getCompressedTextures()
    {
        return mCompressedTextures;
    }

    public void setCompressedTextures(CompressedTexture2D[] compressedTexture2Ds)
    {
        mCompressedTextures = compressedTexture2Ds;
    }

	void reset() throws TextureException
	{
		if(mBitmaps != null)
		{
			int count = mBitmaps.length;
			for(int i=0; i<count; i++)
			{
				Bitmap bitmap = mBitmaps[i];
				bitmap.recycle();
				mBitmaps[i] = null;
			}
		}
		if(mByteBuffers != null)
		{
			int count = mByteBuffers.length;
			for(int i=0; i<count; i++)
			{
				ByteBuffer byteBuffer = mByteBuffers[i];
				byteBuffer.clear();
				mByteBuffers[i] = null;
			}
		}
        if(mCompressedTextures != null)
        {
            int count = mCompressedTextures.length;
            for(int i=0; i<count; i++)
            {
                CompressedTexture2D texture = mCompressedTextures[i];
                texture.remove();
                mCompressedTextures[i] = null;
            }
        }
	}
}
