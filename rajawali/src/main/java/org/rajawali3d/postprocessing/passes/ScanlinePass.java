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
package org.rajawali3d.postprocessing.passes;
import org.rajawali3d.R;

public class ScanlinePass extends EffectPass {
    protected float[] mResolution;
    protected float mRadius;

    public ScanlinePass(float opacity, float radius, int screenWidth, int screenHeight) {
        super();
        setSize(screenWidth, screenHeight);

        setOpacity(opacity);
        setRadius(radius);

        createMaterial(R.raw.minimal_vertex_shader, R.raw.scanline_fragment_shader);
    }

    public void setShaderParams()
    {
        super.setShaderParams();
        mFragmentShader.setUniform1f("uRadius", mRadius);
        mFragmentShader.setUniform2fv("uResolution", mResolution);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        mResolution = new float[] { width, height };
    }

    // set the radius of our scanline
    public void setRadius(float radius)
    {
        mRadius = Math.abs(radius);
    }
}
