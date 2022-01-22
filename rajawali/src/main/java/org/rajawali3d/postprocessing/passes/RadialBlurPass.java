/**
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

import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.postprocessing.passes.EffectPass;

public class RadialBlurPass extends EffectPass {
    public RadialBlurPass() {
        super();

        mVertexShader = new RadialBlurVertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== vertex shader ===\n" + mVertexShader.getShaderString());

        mFragmentShader = new RadialBlurFragmentShader();
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        //Log.d(getClass().getSimpleName(), "=== fragment shader ===\n" + mFragmentShader.getShaderString());
        createMaterial(mVertexShader, mFragmentShader);
    }

    public enum RadialShaderVar implements AShaderBase.IGlobalShaderVar {
        U_EXTENT("uRadialExtent", AShaderBase.DataType.FLOAT),
        U_INCREMENT("uRadialIncrement", AShaderBase.DataType.FLOAT),
        U_STRENGTH("uRadialStrength", AShaderBase.DataType.FLOAT);

        private final String mVarString;
        private final AShaderBase.DataType mDataType;

        RadialShaderVar(String varString, AShaderBase.DataType dataType) {
            mVarString = varString;
            mDataType = dataType;
        }

        public String getVarString() {
            return mVarString;
        }

        public AShaderBase.DataType getDataType() {
            return mDataType;
        }
    }

    private static class RadialBlurVertexShader extends VertexShader
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

    private class RadialBlurFragmentShader extends FragmentShader {
        private RSampler2D uTexture;
        private RVec2 vTextureCoord;
        private RFloat uExtent;
        private RFloat uIncrement;
        private RFloat uStrength;

        private int muExtentHandle;
        private int muIncrementHandle;
        private int muStrengthHandle;

        private float muExtent;
        private float muIncrement;
        private float muStrength;

        RadialBlurFragmentShader() {
            this(0.3f,0.03f,2.2f);
        }

        RadialBlurFragmentShader(float extent, float increment, float strength) {
            muExtent = extent;
            muIncrement = increment;
            muStrength = strength;
        }

        public void setExtent(float extent)
        {
            muExtent = extent;
        }

        public void setIncrement(float increment)
        {
            muIncrement = increment;
        }

        public void setStrength(float strength)
        {
            muStrength = strength;
        }

        public void initialize() {
            super.initialize();
            uTexture = (RSampler2D) addUniform(PARAM_TEXTURE, DataType.SAMPLER2D);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
            uExtent = (RFloat) addUniform(RadialShaderVar.U_EXTENT);
            uIncrement = (RFloat) addUniform(RadialShaderVar.U_INCREMENT);
            uStrength = (RFloat) addUniform(RadialShaderVar.U_STRENGTH);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muExtentHandle = getUniformLocation(programHandle, RadialShaderVar.U_EXTENT);
            muIncrementHandle = getUniformLocation(programHandle, RadialShaderVar.U_INCREMENT);
            muStrengthHandle = getUniformLocation(programHandle, RadialShaderVar.U_STRENGTH);
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muExtentHandle, muExtent);
            GLES20.glUniform1f(muIncrementHandle, muIncrement);
            GLES20.glUniform1f(muStrengthHandle, muStrength);
        }

        public void main() {
            RVec2 dir = new RVec2("dir", castVec2("0.5 - vTextureCoord"));
            RFloat dist = new RFloat("dist", length(dir));
            RVec4 color = new RVec4("color", texture2D(uTexture,vTextureCoord));
            RVec4 sum = new RVec4("sum", color);
            RFloat count = new RFloat("count", castFloat(0));

            RFloat i = new RFloat("i", castFloat("-uRadialExtent"));
            mShaderSB.append("while(i < uRadialExtent) {\n");
                      RVec2 offset = new RVec2("offset", castVec2("dir/dist * i * i"));
                      sum.assignAdd(texture2D(uTexture, vTextureCoord.add(offset)));
                      count.assignAdd(1);
                      i.assignAdd(uIncrement);
            mShaderSB.append("}\n");

            sum.assignDivide(count);
            RFloat a = new RFloat("a", clamp(dist.multiply(uStrength),0,1));
            GL_FRAG_COLOR.assign(mix( color, sum, a ));
        }
    }
}

