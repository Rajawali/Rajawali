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

import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.postprocessing.passes.EffectPass;

public class RadialBlurPass extends EffectPass {

    public RadialBlurPass() {
        mVertexShader = new VertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();
        mFragmentShader = new RadialBlurFragmentShader();
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        createMaterial(mVertexShader, mFragmentShader);
    }

    public void setShaderParams() {
        super.setShaderParams();
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
            muExtent = 0.3f;
            muIncrement = 0.03f;
            muStrength = 2.2f;
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
            uExtent = (RFloat) addUniform("uExtent", DataType.FLOAT);
            uIncrement = (RFloat) addUniform("uIncrement", DataType.FLOAT);
            uStrength = (RFloat) addUniform("uStrength", DataType.FLOAT);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muExtentHandle = getUniformLocation(programHandle, "uExtent");
            muIncrementHandle = getUniformLocation(programHandle, "uIncrement");
            muStrengthHandle = getUniformLocation(programHandle, "uStrength");
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muExtentHandle, muExtent);
            GLES20.glUniform1f(muIncrementHandle, muIncrement);
            GLES20.glUniform1f(muStrengthHandle, muStrength);
        }

        public void main() {
            RVec2 dir = new RVec2("dir");
            dir.assign("0.5 - vTextureCoord");

            RFloat dist = new RFloat("dist");
            dist.assign(sqrt(dir.x().multiply(dir.x()).add(dir.y().multiply(dir.y()))));

            RVec4 color = new RVec4("color");
            color.assign(texture2D(uTexture,vTextureCoord));

            RVec4 sum = new RVec4("sum");
            sum.assign(color);

            RFloat count = new RFloat("count");
            count.assign(0);

            mShaderSB.append(
                    "for (float i = -uExtent; i < uExtent; i+=uIncrement) {\n" +
                            "    sum += texture2D( uTexture, vTextureCoord + dir/dist * i * i );\n" +
                            "    count += 1.0;\n" +
                            "}\n"
            );

            RFloat t = new RFloat("t");
            t.assign(clamp(dist.multiply(uStrength),0,1));

            GL_FRAG_COLOR.assign("mix( color, sum/count, t )");
        }
    }
}
