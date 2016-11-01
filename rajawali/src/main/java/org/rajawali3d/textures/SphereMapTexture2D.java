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
package org.rajawali3d.textures;

import android.content.Context;
import android.support.annotation.NonNull;
import org.rajawali3d.textures.annotation.Type;
import org.rajawali3d.util.RajLog;

public class SphereMapTexture2D extends SingleTexture2D {
    private boolean mIsSkyTexture;
    private boolean mIsEnvironmentTexture;

    public SphereMapTexture2D(SphereMapTexture2D other) throws TextureException {
        super(other);
    }

    public SphereMapTexture2D(String textureName) {
        super(Type.SPHERE_MAP, textureName);
    }

    public SphereMapTexture2D(String textureName, @NonNull Context context, int resourceId) {
        super(Type.SPHERE_MAP, textureName);
        setTextureDataFromResourceId(context, resourceId);
    }

    public SphereMapTexture2D(String textureName, TextureDataReference textureData) {
        super(Type.SPHERE_MAP, textureName, textureData);
    }

    @Override
    public SphereMapTexture2D clone() {
        try {
            return new SphereMapTexture2D(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    public void isSkyTexture(boolean value) {
        mIsSkyTexture = value;
        mIsEnvironmentTexture = !value;
    }

    public boolean isSkyTexture() {
        return mIsSkyTexture;
    }

    public void isEnvironmentTexture(boolean value) {
        mIsEnvironmentTexture = value;
        mIsSkyTexture = !mIsEnvironmentTexture;
    }

    public boolean isEnvironmentTexture() {
        return mIsEnvironmentTexture;
    }
}
