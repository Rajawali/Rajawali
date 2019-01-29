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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.ETC1;
import android.opengl.ETC1Util;

import org.rajawali3d.util.RajLog;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Etc1Texture extends ACompressedTexture {

    protected int mResourceId = -1;
    protected int[] mResourceIds;
    protected Bitmap mBitmap;

    public Etc1Texture(String textureName) {
        super(textureName);
        mCompressionType = CompressionType.ETC1;
        mCompressionFormat = ETC1.ETC1_RGB8_OES;
    }

    public Etc1Texture(int resourceId) {
        this(TextureManager.getInstance().getContext().getResources().getResourceName(resourceId));
        setResourceId(resourceId);
    }

    public Etc1Texture(String textureName, int resourceId, Bitmap fallbackTexture) {
        this(textureName);
        Context context = TextureManager.getInstance().getContext();
        setInputStream(context.getResources().openRawResource(resourceId), fallbackTexture);
    }

    public Etc1Texture(String textureName, int[] resourceIds) {
        this(textureName);
        setResourceIds(resourceIds);
    }

    public Etc1Texture(String textureName, ByteBuffer byteBuffer) {
        this(textureName);
        setByteBuffer(byteBuffer);
    }

    public Etc1Texture(String textureName, ByteBuffer[] byteBuffers) {
        this(textureName);
        setByteBuffers(byteBuffers);
    }

    public Etc1Texture(String textureName, InputStream compressedTexture, Bitmap fallbackTexture) {
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
    protected void add() throws TextureException {
        if(mResourceId != -1) {
            Resources resources = TextureManager.getInstance().getContext().getResources();
            try {
                ETC1Util.ETC1Texture texture = ETC1Util.createTexture(resources.openRawResource(mResourceId));
                mByteBuffers = new ByteBuffer[]{texture.getData()};
                setWidth(texture.getWidth());
                setHeight(texture.getHeight());
                setCompressionFormat(ETC1.ETC1_RGB8_OES);
            } catch (IOException e) {
                RajLog.e(e.getMessage());
                e.printStackTrace();
            }
        } else if(mResourceIds != null) {
            ByteBuffer[] mipmapChain = new ByteBuffer[mResourceIds.length];
            Resources resources = TextureManager.getInstance().getContext().getResources();
            int mip_0_width = 1, mip_0_height = 1;
            try {
                for (int i = 0, length = mResourceIds.length; i < length; i++) {
                    ETC1Util.ETC1Texture texture = ETC1Util.createTexture(resources.openRawResource(mResourceIds[i]));
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
        super.add();
        if (mShouldRecycle) {
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
            if(mByteBuffers != null) {
                int count = mByteBuffers.length;
                for(int i=0; i <count; i++) {
                    mByteBuffers[i].clear();
                    mByteBuffers[i] = null;
                }
                mByteBuffers = null;
            }
        }
    }

    @Override
    protected void reset() throws TextureException {
        super.reset();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        if(mByteBuffers != null) {
            int count = mByteBuffers.length;
            for(int i=0; i <count; i++) {
                mByteBuffers[i].clear();
                mByteBuffers[i] = null;
            }
            mByteBuffers = null;
        }
    }

    public void setResourceId(int resourceId) {
        mResourceId = resourceId;
    }

    public int getResourceId() {
        return mResourceId;
    }

    public void setResourceIds(int[] resourceIds) {
        mResourceIds = resourceIds;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        int imageSize = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer uncompressedBuffer = ByteBuffer.allocateDirect(imageSize);
        bitmap.copyPixelsToBuffer(uncompressedBuffer);
        uncompressedBuffer.position(0);

        ByteBuffer compressedBuffer = ByteBuffer.allocateDirect(
                ETC1.getEncodedDataSize(bitmap.getWidth(), bitmap.getHeight())).order(ByteOrder.nativeOrder());
        ETC1.encodeImage(uncompressedBuffer, bitmap.getWidth(), bitmap.getHeight(), 2, 2 * bitmap.getWidth(),
                compressedBuffer);

        mByteBuffers = new ByteBuffer[]{compressedBuffer};
        setWidth(bitmap.getWidth());
        setHeight(bitmap.getHeight());
    }

    public void setInputStream(InputStream compressedTexture, Bitmap fallbackTexture) {
        ETC1Util.ETC1Texture texture = null;

        try {
            texture = ETC1Util.createTexture(compressedTexture);
        } catch (IOException e) {
            RajLog.e("addEtc1Texture: " + e.getMessage());
        } finally {
            if (texture == null) {
                setBitmap(fallbackTexture);

                if (RajLog.isDebugEnabled())
                    RajLog.d("Falling back to uncompressed texture");
            } else {
                setByteBuffer(texture.getData());
                setWidth(texture.getWidth());
                setHeight(texture.getHeight());

                if (RajLog.isDebugEnabled())
                    RajLog.d("ETC1 texture load successful");
            }
        }
    }
}
