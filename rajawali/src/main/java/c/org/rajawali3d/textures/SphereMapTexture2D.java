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
 * A 2D spherically mapped environmental texture. These textures are typically used to simulate highly reflective
 * surfaces by providing what the reflected environment would look like. For static or basic reflective appearances,
 * a single texture can be used. For more advanced reflections, the scene can be rendered to a FBO with spherical
 * mapping which is used as a {@link Type#SPHERE_MAP} texture.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class SphereMapTexture2D extends SingleTexture2D {

    private boolean isSkyTexture = false;

    /**
     * Constructs a new {@link SphereMapTexture2D} with data and settings from the provided {@link SphereMapTexture2D}.
     *
     * @param other The other {@link SphereMapTexture2D}.
     */
    public SphereMapTexture2D(@NonNull SphereMapTexture2D other) throws TextureException {
        super(other);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with the provided name and no data.
     *
     * @param name {@link String} The texture name.
     */
    public SphereMapTexture2D(@NonNull String name) {
        super(Type.SPHERE_MAP, name);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with data provided by the Android resource id. The texture name is
     * set by querying Android for the resource name.
     *
     * @param name       {@link String} The texture name.
     * @param context    {@link Context} The application context.
     * @param resourceId {@code int} The Android resource id to load from.
     *
     * @throws TextureException if there is an error reading the resource.
     */
    public SphereMapTexture2D(@NonNull String name, @NonNull Context context, @DrawableRes int resourceId)
        throws TextureException {
        super(Type.SPHERE_MAP, name);
        setTextureDataFromResourceId(context, resourceId);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with the provided data.
     *
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public SphereMapTexture2D(@NonNull String name, @NonNull TextureDataReference data) {
        super(Type.SPHERE_MAP, name, data);
    }

    /**
     * Sets whether or not this texture is treated a sky sphere or environmental map. By default,
     * {@link SphereMapTexture2D}s are treated as environmental maps.
     *
     * @param value {@code true} if this texture should be treated as a sky sphere.
     */
    public void isSkyTexture(boolean value) {
        isSkyTexture = value;
    }

    /**
     * Returns whether or not this texture is treated as a sky sphere or environmental map. By default,
     * {@link SphereMapTexture2D}s are treated as environmental maps.
     *
     * @return {@code true} if this texture is treated as a sky sphere.
     */
    public boolean isSkyTexture() {
        return isSkyTexture;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public SphereMapTexture2D clone() {
        try {
            return new SphereMapTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }
}
