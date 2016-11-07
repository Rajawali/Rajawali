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

import c.org.rajawali3d.textures.annotation.Compression2D;
import org.rajawali3d.util.RajLog;

import java.nio.ByteBuffer;

public class PvrtcTexture2D extends CompressedTexture2D {

    // PowerVR Texture2D compression constants
    private static final int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG  = 0x8C00;
    private static final int GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG  = 0x8C01;
    private static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
    private static final int GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;

    public enum PvrtcFormat {
        RGB_2BPP,
        RGB_4BPP,
        RGBA_2BPP,
        RGBA_4BPP
    }

    ;

    /**
     * PVRCT Texture2D Compression format. See {@link PvrtcFormat}.
     */
    protected PvrtcFormat mPvrtcFormat;

    public PvrtcTexture2D(PvrtcTexture2D other) throws TextureException {
        super(other);
        setPvrtcFormat(other.getPvrtcFormat());
    }

    public PvrtcTexture2D(String textureName, ByteBuffer byteBuffer, PvrtcFormat pvrtcFormat) throws TextureException {
        this(textureName, new ByteBuffer[]{ byteBuffer }, pvrtcFormat);
    }

    public PvrtcTexture2D(String textureName, ByteBuffer[] byteBuffers, PvrtcFormat pvrtcFormat) throws
                                                                                                 TextureException {
        super(textureName, byteBuffers);
        setCompressionType(Compression2D.PVRTC);
        setPvrtcFormat(pvrtcFormat);
    }

    /**
     * Copies every property from another PvrtcTexture object
     *
     * @param other another PvrtcTexture object to copy from
     */
    public void setFrom(PvrtcTexture2D other) throws TextureException {
        super.setFrom(other);
        mPvrtcFormat = other.getPvrtcFormat();
    }

    @Override
    public PvrtcTexture2D clone() {
        try {
            return new PvrtcTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    /**
     * @return the PVRCT Texture2D Compression format. See {@link PvrtcFormat}.
     */
    public PvrtcFormat getPvrtcFormat() {
        return mPvrtcFormat;
    }

    /**
     * @param pvrtcFormat the PVRCT Texture2D Compression format. See {@link PvrtcFormat}.
     */
    public void setPvrtcFormat(PvrtcFormat pvrtcFormat) {
        this.mPvrtcFormat = pvrtcFormat;
        switch (pvrtcFormat) {
            case RGB_2BPP:
                mCompressionFormat = GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
                break;
            case RGB_4BPP:
                mCompressionFormat = GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
                break;
            case RGBA_2BPP:
                mCompressionFormat = GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG;
                break;
            case RGBA_4BPP:
            default:
                mCompressionFormat = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
                break;
        }
    }
}
