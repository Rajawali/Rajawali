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

import android.opengl.GLES20;

import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.postprocessing.passes.EffectPass;

public class KaleidoscopePass extends EffectPass {

    public KaleidoscopePass() {
        mVertexShader = new VertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();
        mFragmentShader = new KaleidoscopeFragmentShader();
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        createMaterial(mVertexShader, mFragmentShader);
    }

    public void setSegments(float segments) {
        if(mFragmentShader instanceof KaleidoscopeFragmentShader) {
            ((KaleidoscopeFragmentShader) mFragmentShader).setSegments(segments);
        }
    }

    private class KaleidoscopeFragmentShader extends FragmentShader {
        private RSampler2D uTexture;
        private RVec2 vTextureCoord;

        private RFloat uSegments;
        private int muSegmentsHandle;
        private float muSegments;


        KaleidoscopeFragmentShader() {
            muSegments = 3f;
        }

        KaleidoscopeFragmentShader(float segments) {
            muSegments = segments;
        }


        public void setSegments(float segments)
        {
            muSegments = segments;
            applyParams();
        }

        @Override
        public void initialize() {
            super.initialize();
            uTexture = (RSampler2D) addUniform(PARAM_TEXTURE, DataType.SAMPLER2D);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
            uSegments = (RFloat) addUniform("uSegments", DataType.FLOAT);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muSegmentsHandle = getUniformLocation(programHandle, "uSegments");
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muSegmentsHandle, muSegments);
        }

        public void main() {
            startif(new Condition(uSegments, Operator.GREATER_THAN, 0f));
            {
                // translate from texture to orthagonal coordinates
                RVec2 delta = new RVec2("delta");
                delta.assign(vTextureCoord.multiply(2f).subtract(1f));

                // multiply the mapping angle to compress the image angularly,
                // while maintaining vertical symmetry
                // wraparound implements the kaleidoscope repetition
                RFloat theta = new RFloat("theta");
                theta.assign(atan(delta.x(),delta.y())); // x,y are reversed for vertical symmetry
                startif(new Condition(theta, Operator.LESS_THAN, 0));
                theta.assignMultiply(-1);
                endif();
                theta.assignMultiply(uSegments);

                // map theta and length back to orthagonal coordinates
                RVec2 uv = new RVec2("uv");
                uv.assign(castVec2(cos(theta), sin(theta)));
                uv.assignMultiply(length(delta));

                // translate back to texture coordiantes
                uv.assignAdd(1f);
                uv.assignMultiply(0.5f);
                GL_FRAG_COLOR.assign(texture2D(uTexture, uv));
            }
            ifelse();
            {
                GL_FRAG_COLOR.assign(texture2D(uTexture,vTextureCoord));
            }
            endif();
        }
    }

}
