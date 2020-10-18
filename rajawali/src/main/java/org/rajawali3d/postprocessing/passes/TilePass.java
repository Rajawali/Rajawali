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

/* for square tiles, try this:
 *
 * float ratio = (float)Math.sqrt(getViewportWidth()/(float)getViewportHeight());
 * TilePass tiledPass = new TilePass(3*ratio, 3/ratio);
 */

public class TilePass extends EffectPass {
    private static final String PARAM_GRID = "uGrid";
    private float rows = 1;
    private float cols = 1;

    public TilePass(float rows, float cols)
    {
        super();
        if(rows > 1) this.rows = rows;
        if(cols > 1) this.cols = cols;

        mVertexShader = new TileVertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();

        mFragmentShader = new TileFragmentShader();
        mFragmentShader.initialize();
        mFragmentShader.buildShader();

        createMaterial(mVertexShader, mFragmentShader);
    }

    @Override
    public void setShaderParams() {
        super.setShaderParams();
        setGrid(rows, cols);
    }

    public void setGrid(float rows, float cols) {
        if(rows > 1) this.rows = rows;
        if(cols > 1) this.cols = cols;
        float[] grid = {this.rows, this.cols};
        mVertexShader.setUniform2fv(PARAM_GRID, grid);
    }

    private class TileVertexShader extends VertexShader {

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

    private class TileFragmentShader extends FragmentShader
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
            pos.x().assign(mod(vTextureCoord.x().multiply(uGrid.x()), "1."));
            pos.y().assign(mod(vTextureCoord.y().multiply(uGrid.y()), "1."));

            RVec4 srcColor = new RVec4("srcColor");
            srcColor.assign(texture2D(uTexture, pos));
            GL_FRAG_COLOR.assign(srcColor);
        }
    }
}
