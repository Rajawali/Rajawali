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

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.postprocessing.passes.EffectPass;

public class BlendPass extends EffectPass {
    private final ATexture mBlendTexture;

    public enum BlendMode {
        ADD, BURN, DARKEN, HARD_LIGHT, LIGHTEN, MULTIPLY, OVERLAY, SCREEN, SOFT_LIGHT, SUBTRACT
    }

    public BlendPass(BlendMode mode, ATexture blendTexture) {
        super();
        mBlendTexture = blendTexture;

        mVertexShader = new BlendingVertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== vertex shader ===\n" + mVertexShader.getShaderString());

        mFragmentShader = new BlendingFragmentShader(mode);
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== fragment shader ===\n" + mFragmentShader.getShaderString());

        createMaterial(mVertexShader,mFragmentShader);
    }

    @Override
    public void setShaderParams() {
        super.setShaderParams();
        mMaterial.bindTextureByName(PARAM_BLEND_TEXTURE, 1, mBlendTexture);
    }

    @Override
    public void setMaterial(Material material) {
        super.setMaterial(material);
        material.setTextureHandleForName(PARAM_BLEND_TEXTURE);
    }

    private static class BlendingVertexShader extends VertexShader
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

    private class BlendingFragmentShader extends FragmentShader
    {
        private final BlendMode mBlendMode;
        private RSampler2D uTexture;
        private RSampler2D uBlendTexture;
        private RVec2 vTextureCoord;

        BlendingFragmentShader(BlendMode mode) {
            mBlendMode = mode;
        }

        @Override
        public void initialize() {
            super.initialize();
            addUniform(PARAM_OPACITY, DataType.FLOAT);
            uTexture = (RSampler2D) addUniform(PARAM_TEXTURE, DataType.SAMPLER2D);
            uBlendTexture = (RSampler2D) addUniform(PARAM_BLEND_TEXTURE, DataType.SAMPLER2D);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
        }

        @Override
        public void main() {
            RVec4 src = new RVec4("src");
            RVec4 dst = new RVec4("dst");
            src.assign(texture2D(uTexture, vTextureCoord));
            dst.assign(texture2D(uBlendTexture, vTextureCoord));

            switch (mBlendMode) {
                case BURN:
                    GL_FRAG_COLOR.assign("vec4(" +
                            "(src.rgb+dst.rgb-vec3(1.))," +
                            "src.a * uOpacity)");
                    break;
                case DARKEN:
                    GL_FRAG_COLOR.assign("vec4(" +
                            "min(src.r,dst.r)," +
                            "min(src.g,dst.g)," +
                            "min(src.b,dst.b)," +
                            "src.a * uOpacity)");
                    break;
                case HARD_LIGHT:
                    GL_FRAG_COLOR.assign("vec4(" +
                            "dst.r<0.5?(2.0*dst.r*src.r):(1.0-2.0*(1.0-dst.r)*(1.0-src.r))," +
                            "dst.g<0.5?(2.0*dst.g*src.g):(1.0-2.0*(1.0-dst.g)*(1.0-src.g))," +
                            "dst.b<0.5?(2.0*dst.b*src.b):(1.0-2.0*(1.0-dst.b)*(1.0-src.b))," +
                            "src.a * uOpacity)");
                    break;
                case LIGHTEN:
                    GL_FRAG_COLOR.assign("vec4(" +
                            "max(src.r,dst.r)," +
                            "max(src.g,dst.g)," +
                            "max(src.b,dst.b)," +
                            "src.a * uOpacity)");
                    break;
                case MULTIPLY:
                    GL_FRAG_COLOR.assign("vec4(src.rgb * dst.rgb, src.a * uOpacity)");
                    break;
                case OVERLAY:
                    GL_FRAG_COLOR.assign("vec4(" +
                            "src.r<0.5?(2.0*src.r*dst.r):(1.0-2.0*(1.0-src.r)*(1.0-dst.r))," +
                            "src.g<0.5?(2.0*src.g*dst.g):(1.0-2.0*(1.0-src.g)*(1.0-dst.g))," +
                            "src.b<0.5?(2.0*src.b*dst.b):(1.0-2.0*(1.0-src.b)*(1.0-dst.b))," +
                            "src.a * uOpacity)");
                    break;
                case SCREEN:
                    GL_FRAG_COLOR.assign("vec4(src.rgb + dst.rgb - src.rgb * dst.rgb, src.a * uOpacity)");
                    break;
                case SOFT_LIGHT:
                    GL_FRAG_COLOR.assign("vec4(" +
                            "(dst.r<0.5)?(2.0*src.r*dst.r+src.r*src.r*(1.0-2.0*dst.r)):(sqrt(src.r)*(2.0*dst.r-1.0)+2.0*src.r*(1.0-dst.r))," +
                            "(dst.g<0.5)?(2.0*src.g*dst.g+src.g*src.g*(1.0-2.0*dst.g)):(sqrt(src.g)*(2.0*dst.g-1.0)+2.0*src.g*(1.0-dst.g))," +
                            "(dst.b<0.5)?(2.0*src.b*dst.b+src.r*src.b*(1.0-2.0*dst.b)):(sqrt(src.b)*(2.0*dst.b-1.0)+2.0*src.b*(1.0-dst.b))," +
                            "src.a * uOpacity)");
                    break;
                case SUBTRACT:
                    GL_FRAG_COLOR.assign("vec4(src.rgb - dst.rgb, src.a * uOpacity)");
                    break;
                case ADD:
                default:
                    GL_FRAG_COLOR.assign("vec4(src.rgb + dst.rgb, src.a * uOpacity)");
                    break;
            }
        }
    }
}

