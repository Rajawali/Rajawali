/**
 * Copyright 2013 Dennis Ippel
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.postprocessing.passes;

import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import androidx.annotation.NonNull;

import org.rajawali3d.R;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.materials.textures.ATexture.FilterType;
import org.rajawali3d.materials.textures.ATexture.WrapType;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

public class CopyToNewRenderTargetPass extends EffectPass {

    private RenderTarget mRenderTarget;

    public CopyToNewRenderTargetPass(String name, Renderer renderer, int width, int height) {
        super();
        mNeedsSwap = false;
        mRenderTarget = new RenderTarget(name, width, height, 0, 0,
                false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888,
                FilterType.LINEAR, WrapType.CLAMP);
        renderer.addRenderTarget(mRenderTarget);

        mVertexShader = new CopyVertexShader();
        mVertexShader.initialize();
        mVertexShader.buildShader();

        mFragmentShader = new CopyFragmentShader();
        mFragmentShader.initialize();
        mFragmentShader.buildShader();
        createMaterial(mVertexShader, mFragmentShader);
    }

    public RenderTarget getRenderTarget() {
        return mRenderTarget;
    }

    public void render(@NonNull Scene scene, @NonNull Renderer renderer, @NonNull ScreenQuad screenQuad, @NonNull RenderTarget writeTarget, @NonNull RenderTarget readTarget, long elapsedTime, double deltaTime) {
        super.render(scene, renderer, screenQuad, mRenderTarget, readTarget, elapsedTime, deltaTime);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        mRenderTarget.setWidth(width);
        mRenderTarget.setHeight(height);
    }

    private static class CopyVertexShader extends VertexShader
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

    class CopyFragmentShader extends FragmentShader {
        private RFloat uOpacity;
        private RSampler2D uTexture;
        private RVec2 vTextureCoord;

        @Override
        public void initialize() {
            super.initialize();
            uOpacity = (RFloat) addUniform(PARAM_OPACITY, DataType.FLOAT);
            uTexture = (RSampler2D) addUniform(PARAM_TEXTURE, DataType.SAMPLER2D);
            vTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
        }

        @Override
        public void main() {
            GL_FRAG_COLOR.assign(uOpacity.multiply(texture2D(uTexture, vTextureCoord)));
        }
    }
}
