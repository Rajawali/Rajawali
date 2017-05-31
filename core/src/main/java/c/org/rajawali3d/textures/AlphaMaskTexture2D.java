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
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.util.RajLog;

/**
 * A 2D alpha masking texture. Alpha masking is used to add transparency when the diffuse textures don't otherwise
 * include this information. The red channel of this texture is used to determine if the material this texture is
 * attached to should render the pixel or discard it. This decision is made based on the threshold value which
 * defaults to {@code 0.5f}.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 *
 * @see {@link #setAlphaMaskingThreshold(float)}
 * @see {@link #getAlphaMaskingThreshold()}
 */
public class AlphaMaskTexture2D extends SingleTexture2D {

    private float threshold = .5f;

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with data and settings from the provided {@link AlphaMaskTexture2D}.
     *
     * @param other The other {@link AlphaMaskTexture2D}.
     */
    public AlphaMaskTexture2D(@NonNull AlphaMaskTexture2D other) throws TextureException {
        super(other);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with the provided name and no data.
     *
     * @param name {@link String} The texture name.
     */
    public AlphaMaskTexture2D(@NonNull String name) {
        super(Type.ALPHA_MASK, name);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with data provided by the Android resource id. The texture name is
     * set by querying Android for the resource name.
     *
     * @param name {@link String} The texture name.
     * @param context    {@link Context} The application context.
     * @param resourceId {@code int} The Android resource id to load from.
     */
    public AlphaMaskTexture2D(@NonNull String name, @NonNull Context context, @DrawableRes int resourceId)
            throws TextureException {
        super(Type.ALPHA_MASK, name);
        setTextureDataFromResourceId(context, resourceId);
    }

    /**
     * Constructs a new {@link AlphaMaskTexture2D} with the provided data.
     *
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     */
    public AlphaMaskTexture2D(String name, TextureDataReference data) {
        super(Type.ALPHA_MASK, name, data);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AlphaMaskTexture2D clone() {
        try {
            return new AlphaMaskTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    /**
     * Sets the alpha masking threshold. Pixels which end up with a red channel value (from this texture) less than
     * {@code threshold} will be discarded by the material.
     *
     * @param threshold {@code float} The masking threshold.
     */
    public void setAlphaMaskingThreshold(@FloatRange(from = 0, to = 1) float threshold) {
        this.threshold = threshold;
    }

    /**
     * Fetches the alpha masking threshold. Pixels which end up with a red channel value (from this texture) less than
     * this will be discarded by the material.
     *
     * @return
     */
    @FloatRange(from = 0, to = 1)
    public float getAlphaMaskingThreshold() {
        return threshold;
    }
}
