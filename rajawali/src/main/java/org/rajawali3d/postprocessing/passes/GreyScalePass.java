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

public class GreyScalePass extends EffectPass {
    public enum Desaturation {
        INTENSITY,
        LIGHTNESS,
        LUMA,
        VALUE
    }

    public GreyScalePass() {
        this(Desaturation.INTENSITY);
    }

    public GreyScalePass(Desaturation mode) {
        super();

        mVertexShader = new GreyScaleVertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== vertex shader ===\n" + mVertexShader.getShaderString());

        mFragmentShader = new GreyScaleFragmentShader(mode);
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== fragment shader ===\n" + mFragmentShader.getShaderString());

        createMaterial(mVertexShader,mFragmentShader);
    }

    private class GreyScaleVertexShader extends VertexShader
    {
        private RVec4 aPosition;
        private RVec2 aTextureCoord;
        private RVec2 vTextureCoord;
        private RMat4 uMVPMatrix;

        @Override
        public void initialize() {
            super.initialize();
            aPosition = (RVec4) addAttribute(DefaultShaderVar.A_POSITION);
            aTextureCoord = (RVec2) addAttribute(DefaultShaderVar.A_TEXTURE_COORD);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
            uMVPMatrix = (RMat4) addUniform(DefaultShaderVar.U_MVP_MATRIX);
        }

        @Override
        public void main() {
            vTextureCoord.assign(aTextureCoord);
            GL_POSITION.assign(uMVPMatrix.multiply(aPosition));
        }
    }

    private class GreyScaleFragmentShader extends FragmentShader
    {
        private final Desaturation mMode;
        private RFloat uOpacity;
        private RSampler2D uTexture;
        private RVec2 vTextureCoord;

        GreyScaleFragmentShader(Desaturation mode) {
            mMode = mode;
        }

        @Override
        public void initialize() {
            super.initialize();
            uOpacity = (RFloat) addUniform(PARAM_OPACITY, DataType.FLOAT);
            uTexture = (RSampler2D) addUniform(PARAM_TEXTURE, DataType.SAMPLER2D);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
        }

        @Override
        public void main() {
            RVec4 srcColor = new RVec4("srcColor");
            srcColor.assign(texture2D(uTexture, vTextureCoord));

            switch (mMode) {
                case LIGHTNESS:
                    RFloat least = new RFloat("least");
                    least.assign(min(srcColor.g(),min(srcColor.r(), srcColor.b())));
                    RFloat most = new RFloat("most");
                    most.assign(max(srcColor.g(),max(srcColor.r(), srcColor.b())));
                    RFloat lightness = new RFloat("lightness");
                    lightness.assign("(least + most) / 2.0");
                    GL_FRAG_COLOR.assign(uOpacity.multiply(castVec4("lightness, lightness, lightness, srcColor.a")));
                    break;

                case LUMA:
                    RFloat luma = new RFloat("luma");
                    luma.assign(0);
                    luma.assignAdd(srcColor.r().multiply(0.2126f));
                    luma.assignAdd(srcColor.g().multiply(0.7152f));
                    luma.assignAdd(srcColor.b().multiply(0.0722f));
                    GL_FRAG_COLOR.assign(uOpacity.multiply(castVec4("luma, luma, luma, srcColor.a")));
                    break;

                case VALUE:
                    RFloat value = new RFloat("value");
                    value.assign(max(srcColor.g(),max(srcColor.r(), srcColor.b())));
                    GL_FRAG_COLOR.assign(uOpacity.multiply(castVec4("value, value, value, srcColor.a")));
                    break;

                default:
                    RFloat average = new RFloat("average");
                    average.assign("(srcColor.r + srcColor.g + srcColor.b) / 3.0");
                    GL_FRAG_COLOR.assign(uOpacity.multiply(castVec4("average, average, average, srcColor.a")));
                    break;
            }
        }
    }
}

