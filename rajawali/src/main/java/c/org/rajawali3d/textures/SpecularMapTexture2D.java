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

public class SpecularMapTexture2D extends SingleTexture2D {
    public SpecularMapTexture2D(SpecularMapTexture2D other) throws TextureException {
        super(other);
    }

    public SpecularMapTexture2D(String textureName) {
        super(Type.SPECULAR, textureName);
    }

    public SpecularMapTexture2D(String textureName, @NonNull Context context, int resourceId) {
        super(Type.SPECULAR, textureName);
        setTextureDataFromResourceId(context, resourceId);
    }

    public SpecularMapTexture2D(String textureName, TextureDataReference textureData) {
        super(Type.SPECULAR, textureName, textureData);
    }

    public SpecularMapTexture2D clone() {
        try {
            return new SpecularMapTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }
}
