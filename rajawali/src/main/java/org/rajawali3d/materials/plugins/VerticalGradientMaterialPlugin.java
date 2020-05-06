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
package org.rajawali3d.materials.plugins;

import android.graphics.Color;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class VerticalGradientMaterialPlugin implements IMaterialPlugin {
    int color;

    public VerticalGradientMaterialPlugin() {
        color = Color.CYAN;
    }

    public VerticalGradientMaterialPlugin(int color) {
        this.color = color;
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_LIGHTING;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return null;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return new VerticalGradientFragmentShaderFragment(color);
    }

    @Override
    public void bindTextures(int i) {

    }

    @Override
    public void unbindTextures() {

    }

    class VerticalGradientFragmentShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "VERTICAL_GRADIENT_FRAGMENT_SHADER_FRAGMENT";
        float[] mTint = new float[4];
        RVec4 muTint;
        int muTintHandle;

        VerticalGradientFragmentShaderFragment() {
            setTint(Color.CYAN);
            setNeedsBuild(true);
            initialize();
        }

        VerticalGradientFragmentShaderFragment(int tint) {
            setTint(tint);
            setNeedsBuild(true);
            initialize();
        }

        public void setTint(int tint) {
            mTint[0] = Color.red(tint)/255f;
            mTint[1] = Color.green(tint)/255f;
            mTint[2] = Color.blue(tint)/255f;
            mTint[3] = Color.alpha(tint)/255f;
            applyParams();
        }

        @Override
        public void initialize() {
            super.initialize();
            muTint = (RVec4) addUniform("uTint", DataType.VEC4);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muTintHandle = getUniformLocation(programHandle, "uTint");
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform4f(muTintHandle, mTint[0], mTint[1], mTint[2], mTint[3]);
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.POST_TRANSFORM;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }


        @Override
        public void bindTextures(int i) {

        }

        @Override
        public void unbindTextures() {

        }

        // overlay color is transparent at the bottom and screened over background at the top.
        //
        // essentially (1-(1-a*(1-v))*(1-b*v)),
        //
        // where a is the existing material, b is the overlay, 
        // and v is the v axis value premultiplied by b opacity.
        @Override
        public void main() {
            RVec4 color = (RVec4) getGlobal(AShaderBase.DefaultShaderVar.G_COLOR);
            RFloat v = new RFloat("v");
            v.assign("(1.-gTextureCoord.y)*uTint.a");
            color.assign("1.-(1.-gColor*(1.-v))*(1.-uTint*(v))");
        }
    }
}
