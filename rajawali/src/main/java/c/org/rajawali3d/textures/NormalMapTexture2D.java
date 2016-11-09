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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import c.org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.util.RajLog;

/**
 * A 2D normal mapping texture. Each texel's RGB components represent a normalized Normal Vector XYZ coordinates in
 * object space. Normal maps provide a computationally efficient way to simulate high resolution surfaces without
 * requiring a high resolution mesh.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class NormalMapTexture2D extends SingleTexture2D {

    /**
     * Constructs a new {@link NormalMapTexture2D} with data and settings from the provided {@link NormalMapTexture2D}.
     *
     * @param other The other {@link NormalMapTexture2D}.
     */
    public NormalMapTexture2D(@NonNull NormalMapTexture2D other) throws TextureException {
        super(other);
    }

    /**
     * Constructs a new {@link NormalMapTexture2D} with the provided name and no data.
     *
     * @param name {@link String} The texture name.
     */
    public NormalMapTexture2D(@NonNull String name) {
        super(Type.NORMAL, name);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with data provided by the Android resource id. The texture name is
     * set by querying Android for the resource name.
     *
     * @param name {@link String} The texture name.
     * @param context    {@link Context} The application context.
     * @param resourceId {@code int} The Android resource id to load from.
     */
    public NormalMapTexture2D(@NonNull String name, @NonNull Context context, @DrawableRes int resourceId) {
        super(Type.NORMAL, name);
        setTextureDataFromResourceId(context, resourceId);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with the provided data.
     *
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public NormalMapTexture2D(@NonNull String name, @NonNull TextureDataReference data) {
        super(Type.NORMAL, name, data);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public NormalMapTexture2D clone() {
        try {
            return new NormalMapTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }
}
