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

import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.postprocessing.passes.EffectPass;

public class SobelPass extends EffectPass {
    private static final String PARAM_SOBEL = "uSobel";
    private float gX = 1;
    private float gY = 1;

    public SobelPass(float x, float y)
    {
        super();
        if(x > 0) gX = x;
        if(y > 0) gY = y;

        mVertexShader = new SobelVertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();

        mFragmentShader = new SobelFragmentShader();
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        createMaterial(mVertexShader, mFragmentShader);
    }

    @Override
    public void setShaderParams() {
        super.setShaderParams();
        setGrid(gX, gY);
    }

    public void setGrid(float x, float y) {
        float[] sobel = { gX, gY };
        mVertexShader.setUniform2fv(PARAM_SOBEL, sobel);
    }

    private class SobelVertexShader extends VertexShader {

        @Override
        public void initialize() {
            super.initialize();
            addUniform(PARAM_SOBEL, DataType.VEC2);
        }

        @Override
        public void main() {
            super.main();
        }
    }

    private class SobelFragmentShader extends FragmentShader
    {
        private RVec2 uSobel;
        private RSampler2D uTexture;
        private RVec2 vTextureCoord;

        @Override
        public void initialize() {
            super.initialize();
            uTexture = (RSampler2D) addUniform(PARAM_TEXTURE, DataType.SAMPLER2D);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
            uSobel = (RVec2) addUniform(PARAM_SOBEL, DataType.VEC2);
        }

        @Override
        public void main() {
            RVec2 pos = new RVec2("pos");

            RVec4 horizEdge = new RVec4("horizEdge");
            horizEdge.assign(0,0,0,0);
            pos.assign("vec2( vTextureCoord.x - uSobel.x, vTextureCoord.y - uSobel.y )");
            horizEdge.assignSubtract(texture2D( uTexture,  pos).multiply(1));
            pos.assign("vec2( vTextureCoord.x - uSobel.x, vTextureCoord.y     )");
            horizEdge.assignSubtract(texture2D( uTexture,  pos).multiply(2));
            pos.assign("vec2( vTextureCoord.x - uSobel.x, vTextureCoord.y + uSobel.y )");
            horizEdge.assignSubtract(texture2D( uTexture,  pos).multiply(1));
            pos.assign("vec2( vTextureCoord.x + uSobel.x, vTextureCoord.y - uSobel.y )");
            horizEdge.assignAdd(texture2D( uTexture,  pos).multiply(1));
            pos.assign("vec2( vTextureCoord.x + uSobel.x, vTextureCoord.y     )");
            horizEdge.assignAdd(texture2D( uTexture,  pos).multiply(2));
            pos.assign("vec2( vTextureCoord.x + uSobel.x, vTextureCoord.y + uSobel.y )");
            horizEdge.assignAdd(texture2D( uTexture,  pos).multiply(1));

            RVec4 vertEdge = new RVec4("vertEdge");
            vertEdge.assign(0,0,0,0);
            pos.assign("vec2( vTextureCoord.x - uSobel.x, vTextureCoord.y - uSobel.y )");
            vertEdge.assignSubtract(texture2D( uTexture,  pos).multiply(1));
            pos.assign("vec2( vTextureCoord.x    , vTextureCoord.y - uSobel.y )");
            vertEdge.assignSubtract(texture2D( uTexture,  pos).multiply(2));
            pos.assign("vec2( vTextureCoord.x + uSobel.x, vTextureCoord.y - uSobel.y )");
            vertEdge.assignSubtract(texture2D( uTexture,  pos).multiply(1));
            pos.assign("vec2( vTextureCoord.x - uSobel.x, vTextureCoord.y + uSobel.y )");
            vertEdge.assignAdd(texture2D( uTexture,  pos).multiply(1));
            pos.assign("vec2( vTextureCoord.x    , vTextureCoord.y + uSobel.y )");
            vertEdge.assignAdd(texture2D( uTexture,  pos).multiply(2));
            pos.assign("vec2( vTextureCoord.x + uSobel.x, vTextureCoord.y + uSobel.y )");
            vertEdge.assignAdd(texture2D( uTexture,  pos).multiply(1));

            RVec3 edge = new RVec3("edge");
            edge.assign(sqrt((horizEdge.rgb().multiply(horizEdge.rgb()).add(vertEdge.rgb().multiply(vertEdge.rgb())))));

            GL_FRAG_COLOR.assign("vec4(edge,1)");
        }
    }
}

