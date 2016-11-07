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
package c.org.rajawali3d.textures;


import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import c.org.rajawali3d.textures.annotation.Type;
import net.jcip.annotations.ThreadSafe;
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
     * @throws TextureException thrown if there is an error copying the texture.
     */
    public Texture2D(@NonNull Texture2D other) throws TextureException {
        super(other);
    }

    /**
     * Constructs a new {@link Texture2D} with the provided name.
     *
     * @param name {@link String} The texture name.
     */
    public Texture2D(@NonNull String name) {
        super(Type.DIFFUSE, name);
    }

    /**
     * Constructs a new {@link Texture2D} with the provided name and data provided by the specified Android resource.
     *
     * @param name {@link String} The texture name.
     * @param context {@link Context} The Android application context.
     * @param resourceId {@link DrawableRes} {@code int} The Android resource.
     */
    public Texture2D(@NonNull String name, @NonNull Context context, @DrawableRes int resourceId) {
        super(context, Type.DIFFUSE, resourceId);
        setTextureName(name);
    }

    /**
     * Constructs a new {@link Texture2D} with the provided name and data.
     *
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public Texture2D(@NonNull String name, @NonNull TextureDataReference data) {
        super(Type.DIFFUSE, name, data);
    }

	/*public Texture2D(String textureName, TextureAtlas atlas)
    {
		super(TextureType.DIFFUSE, textureName, atlas.getTileNamed(textureName).getPage());
	}*/

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Texture2D clone() {
        try {
            return new Texture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }
}
