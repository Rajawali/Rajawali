/**
 * Copyright 2015 Dennis Ippel
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

package org.rajawali3d.vr.materials;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.math.vector.Vector2;
import org.rajawali3d.vr.materials.shaders.HotspotFragmentShader;
import org.rajawali3d.R;

/**
 * @author dennis.ippel
 */
public class HotspotMaterial extends Material {
    private HotspotFragmentShader mHotspotShader;

    public HotspotMaterial(boolean useTexture) {
        this(useTexture, false);
    }

    public HotspotMaterial(boolean useTexture, boolean discardAlpha) {
        super(new VertexShader(R.raw.minimal_vertex_shader),
              new HotspotFragmentShader(useTexture, discardAlpha));
        mHotspotShader = (HotspotFragmentShader)mCustomFragmentShader;
    }

    public void setCircleCenter(Vector2 center) {
        mHotspotShader.setCircleCenter(center);
    }

    public void setTrackColor(int color) {
        mHotspotShader.setTrackColor(color);
    }

    public void setProgressColor(int color) {
        mHotspotShader.setProgressColor(color);
    }

    public void setCircleRadius(float circleRadius) {
        mHotspotShader.setCircleRadius(circleRadius);
    }

    public void setBorderThickness(float borderThickness) {
        mHotspotShader.setBorderThickness(borderThickness);
    }

    public void setTextureRotationSpeed(float textureRotationSpeed) {
        mHotspotShader.setTextureRotationSpeed(textureRotationSpeed);
    }

    public void setProgress(float progress) {
        mHotspotShader.setProgress(progress);
    }

    public float getProgress() {
        return mHotspotShader.getProgress();
    }
}
