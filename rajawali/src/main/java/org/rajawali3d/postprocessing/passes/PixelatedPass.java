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
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;

/* for square pixels, try this:
 * 
 * float ratio = getViewportWidth()/(float)getViewportHeight();
 * float split = (1+ratio)/2;
 * PixelatedPass pixelatedPass = new PixelatedPass(64*split, 64/split);
 */
public class PixelatedPass extends EffectPass {
    private static final String PARAM_GRID = "uGrid";
    private float rows = 1;
    private float cols = 1;

    public PixelatedPass(float rows, float cols)
    {
        super();
        if(rows > 0) this.rows = rows;
        if(cols > 0) this.cols = cols;

        mVertexShader = new PixelatedVertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== vertex shader ===\n" + mVertexShader.getShaderString());

        mFragmentShader = new PixelatedFragmentShader();
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== fragment shader ===\n" + mFragmentShader.getShaderString());
        createMaterial(mVertexShader, mFragmentShader);
    }

    @Override
    public void setShaderParams() {
        super.setShaderParams();
        setGrid(rows, cols);
    }

    public void setGrid(float rows, float cols) {
        if(rows > 0) this.rows = rows;
        if(cols > 0) this.cols = cols;
        float[] grid = {1/this.rows, 1/this.cols};
        mVertexShader.setUniform2fv(PARAM_GRID, grid);
    }

    private class PixelatedVertexShader extends VertexShader {

        @Override
        public void initialize() {
            super.initialize();
            addUniform(PARAM_GRID, DataType.VEC2);
        }

        @Override
        public void main() {
            super.main();
        }
    }

    private class PixelatedFragmentShader extends FragmentShader
    {
        private RVec2 uGrid;
        private RSampler2D uTexture;
        private RVec2 vTextureCoord;

        @Override
        public void initialize() {
            super.initialize();
            uTexture = (RSampler2D) addUniform(PARAM_TEXTURE, DataType.SAMPLER2D);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
            uGrid = (RVec2) addUniform(PARAM_GRID, DataType.VEC2);
        }

        @Override
        public void main() {
            RVec2 pos = new RVec2("pos");
            pos.assign(vTextureCoord);
            pos.x().assignSubtract(mod(pos.x(), uGrid.x()));
            pos.y().assignSubtract(mod(pos.y(), uGrid.y()));

            RVec4 srcColor = new RVec4("srcColor");
            srcColor.assign(texture2D(uTexture, pos));
            GL_FRAG_COLOR.assign(srcColor);
        }
    }
}
