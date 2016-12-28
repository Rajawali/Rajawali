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

import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.textures.annotation.Type;

/**
 * A 2D specular mapping texture, in essence, how "shinny" the object is for each color component. Specular maps
 * allow for detailed, localized reflectance without requiring excessively refined geometries and are an extremely
 * useful tool for simulating realistic lighting on complex surfaces.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class SpecularMapTexture2D extends SingleTexture2D {

    /**
     * Constructs a new {@link SpecularMapTexture2D} with data and settings from the provided {@link
     * SpecularMapTexture2D}.
     *
     * @param other The other {@link SpecularMapTexture2D}.
     */
    public SpecularMapTexture2D(@NonNull SpecularMapTexture2D other) throws TextureException {
        super(other);
    }

    /**
     * Constructs a new {@link SpecularMapTexture2D} with the provided name and no data.
     *
     * @param name {@link String} The texture name.
     */
    public SpecularMapTexture2D(@NonNull String name) {
        super(Type.SPECULAR, name);
    }

    /**
     * Constructs a new {@link SpecularMapTexture2D} with data provided by the Android resource id. The texture name is
     * set by querying Android for the resource name.
     *
     * @param name       {@link String} The texture name.
     * @param context    {@link Context} The application context.
     * @param resourceId {@code int} The Android resource id to load from.
     *
     * @throws TextureException if there is an error reading the resource.
     */
    public SpecularMapTexture2D(@NonNull String name, @NonNull Context context, @DrawableRes int resourceId)
        throws TextureException {
        super(Type.SPECULAR, name);
        setTextureDataFromResourceId(context, resourceId);
    }

    /**
     * Constructs a new {@link SpecularMapTexture2D} with the provided data.
     *
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public SpecularMapTexture2D(@NonNull String name, @NonNull TextureDataReference data) {
        super(Type.SPECULAR, name, data);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public SpecularMapTexture2D clone() {
        try {
            return new SpecularMapTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }
}
