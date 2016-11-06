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
import android.support.annotation.NonNull;
import c.org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.util.RajLog;

public class AlphaMapTexture2D extends SingleTexture2D {
    private float mAlphaMaskingThreshold = .5f;

    public AlphaMapTexture2D(AlphaMapTexture2D other) throws TextureException {
        super(other);
    }

    public AlphaMapTexture2D(String textureName) {
        super(Type.ALPHA, textureName);
    }

    public AlphaMapTexture2D(String textureName, @NonNull Context context, int resourceId) {
        super(Type.ALPHA, textureName);
        setTextureDataFromResourceId(context, resourceId);
    }

    public AlphaMapTexture2D(String textureName, TextureDataReference textureData) {
        super(Type.ALPHA, textureName, textureData);
    }

    public AlphaMapTexture2D clone() {
        try {
            return new AlphaMapTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    public void setAlphaMaskingThreshold(float threshold) {
        mAlphaMaskingThreshold = threshold;
    }

    public float getAlphaMaskingThreshold() {
        return mAlphaMaskingThreshold;
    }
}
