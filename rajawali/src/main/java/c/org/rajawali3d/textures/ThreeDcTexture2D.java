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

import java.nio.ByteBuffer;

import android.opengl.GLES11Ext;
import c.org.rajawali3d.textures.annotation.Compression2D;
import org.rajawali3d.util.RajLog;

public class ThreeDcTexture2D extends CompressedTexture2D {

    /**
     * 3DC Texture2D compression format.
     */
    public enum ThreeDcFormat {
        X,
        XY
    }

    ;

    /**
     * 3DC Texture2D Compression format. See {@link ThreeDcFormat}.
     */
    protected ThreeDcFormat mThreeDcFormat;

    public ThreeDcTexture2D(ThreeDcTexture2D other) throws TextureException {
        super(other);
        setThreeDcFormat(other.getThreeDcFormat());
    }

    public ThreeDcTexture2D(String textureName, ByteBuffer byteBuffer, ThreeDcFormat threeDcFormat) throws
                                                                                                    TextureException {
        this(textureName, new ByteBuffer[]{ byteBuffer }, threeDcFormat);
    }

    public ThreeDcTexture2D(String textureName, ByteBuffer[] byteBuffers, ThreeDcFormat threeDcFormat) throws
                                                                                                       TextureException {
        //super(textureName, byteBuffers);
        setCompressionType(Compression2D.THREEDC);
        setThreeDcFormat(threeDcFormat);
    }

    /**
     * Copies every property from another ThreeDcTexture object
     *
     * @param other another ThreeDcTexture object to copy from
     */
    public void setFrom(ThreeDcTexture2D other) throws TextureException {
        super.setFrom(other);
        mThreeDcFormat = other.getThreeDcFormat();
    }

    public ThreeDcTexture2D clone() {

        try {
            return new ThreeDcTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    /**
     * @return the 3DC Texture2D Compression format. See {@link ThreeDcFormat}.
     */
    public ThreeDcFormat getThreeDcFormat() {
        return mThreeDcFormat;
    }

    /**
     * @param threeDcFormat the 3DC Texture2D Compression format. See {@link ThreeDcFormat}.
     */
    public void setThreeDcFormat(ThreeDcFormat mThreeDcFormat) {
        this.mThreeDcFormat = mThreeDcFormat;
        if (mThreeDcFormat == ThreeDcFormat.X) {
            compressionFormat = GLES11Ext.GL_3DC_X_AMD;
        } else {
            compressionFormat = GLES11Ext.GL_3DC_XY_AMD;
        }
    }
}
