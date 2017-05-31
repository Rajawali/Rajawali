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

public class VignettePass extends EffectPass {
    protected float[] mResolution;
    protected float mRadius;
    protected float mSoftness;

    public VignettePass(float opacity, float radius, float softness, int screenWidth, int screenHeight) {
        super();
        setSize(screenWidth, screenHeight);

        setOpacity(opacity);
        setRadius(radius);
        setSoftness(softness);

        createMaterial(R.raw.minimal_vertex_shader, R.raw.vignette_fragment_shader);
    }

    public void setShaderParams()
    {
        super.setShaderParams();
        mFragmentShader.setUniform1f("uRadius", mRadius);
        mFragmentShader.setUniform1f("uSoftness", mSoftness);
        mFragmentShader.setUniform2fv("uResolution", mResolution);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        mResolution = new float[] { width, height };
    }

    // set the radius of our vignette, where 0.5 results in a circle fitting the screen
    public void setRadius(float radius)
    {
        if (radius > 1) mRadius = 1;
        else if (radius < 0) mRadius = 0;
        else
        {
            mRadius = radius;
        }
    }

    // set the softness of our vignette, between 0.0 and 1.0
    public void setSoftness(float softness)
    {
        if (softness > 1) mSoftness = 1;
        else if (softness < 0) mSoftness = 0;
        else
        {
            mSoftness = softness;
        }
    }
}
