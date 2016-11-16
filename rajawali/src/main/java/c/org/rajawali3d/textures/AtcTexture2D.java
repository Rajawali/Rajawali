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

import android.opengl.GLES11Ext;
import c.org.rajawali3d.textures.annotation.Compression2D;
import org.rajawali3d.util.RajLog;

import java.nio.ByteBuffer;

public class AtcTexture2D extends CompressedTexture2D {

    /**
     * ATC Texture2D compression format.
     */
    public enum AtcFormat {
        RGB,
        RGBA_EXPLICIT,
        RGBA_INTERPOLATED
    }

    ;

    /**
     * ATC Texture2D Compression format. See {@link AtcFormat}.
     */
    protected AtcFormat mAtcFormat;

    public AtcTexture2D(AtcTexture2D other) throws TextureException {
        super(other);
        setAtcFormat(other.getAtcFormat());
    }

    public AtcTexture2D(String textureName, ByteBuffer byteBuffer, AtcFormat atcFormat) throws TextureException {
        this(textureName, new ByteBuffer[]{ byteBuffer }, atcFormat);
    }

    public AtcTexture2D(String textureName, ByteBuffer[] byteBuffers, AtcFormat atcFormat) throws TextureException {
        super(textureName, byteBuffers);
        setCompressionType(Compression2D.ATC);
        setAtcFormat(atcFormat);
    }

    /**
     * Copies every property from another AtcTexture object
     *
     * @param other another AtcTexture object to copy from
     */
    public void setFrom(AtcTexture2D other) throws TextureException {
        super.setFrom(other);
        mAtcFormat = other.getAtcFormat();
    }

    @Override
    public AtcTexture2D clone() {
        try {
            return new AtcTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    /**
     * @return the ATC Texture2D Compression format. See {@link AtcFormat}.
     */
    public AtcFormat getAtcFormat() {
        return mAtcFormat;
    }

    /**
     * @param atcFormat ATC Texture2D Compression format. See {@link AtcFormat}.
     */
    public void setAtcFormat(AtcFormat atcFormat) {
        this.mAtcFormat = atcFormat;
        switch (atcFormat) {
            case RGB:
                compressionFormat = GLES11Ext.GL_ATC_RGB_AMD;
                break;
            case RGBA_EXPLICIT:
            default:
                compressionFormat = GLES11Ext.GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
                break;
            case RGBA_INTERPOLATED:
                compressionFormat = GLES11Ext.GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD;
                break;
        }
    }
}
