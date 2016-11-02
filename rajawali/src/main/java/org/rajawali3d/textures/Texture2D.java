/**
 * Copyright 2013 Dennis Ippel
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.textures;


import android.content.Context;
import android.support.annotation.NonNull;

import net.jcip.annotations.ThreadSafe;

import org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.util.RajLog;

/**
 * Diffuse 2D texture.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class Texture2D extends SingleTexture2D {

    /**
     * Constructs a new {@link Texture2D} from the provided {@link Texture2D}.
     *
     * @param other {@link Texture2D} The source texture.
     * @throws TextureException
     */
    public Texture2D(Texture2D other) throws TextureException {
        super(other);
    }

    public Texture2D(String textureName) {
        super(Type.DIFFUSE, textureName);
    }

    public Texture2D(String textureName, @NonNull Context context, int resourceId) {
        super(Type.DIFFUSE, textureName);
        setTextureDataFromResourceId(context, resourceId);
    }

    public Texture2D(String textureName, TextureDataReference textureData) {
        super(Type.DIFFUSE, textureName, textureData);
    }

	/*public Texture2D(String textureName, TextureAtlas atlas)
    {
		super(TextureType.DIFFUSE, textureName, atlas.getTileNamed(textureName).getPage());
	}*/

    public Texture2D clone() {
        try {
            return new Texture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }
}
