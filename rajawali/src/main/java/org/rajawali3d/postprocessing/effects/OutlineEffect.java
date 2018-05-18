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
package org.rajawali3d.postprocessing.effects;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.postprocessing.APostProcessingEffect;
import org.rajawali3d.postprocessing.passes.BlendPass;
import org.rajawali3d.postprocessing.passes.CopyToNewRenderTargetPass;
import org.rajawali3d.postprocessing.passes.RenderPass;
import org.rajawali3d.postprocessing.passes.SobelPass;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

public class OutlineEffect extends APostProcessingEffect {
    private Scene     mScene;
    private Camera    mCamera;
    private int       mWidth;
    private int       mHeight;
    private float     mThickness;
    private BlendPass.BlendMode mBlendMode;

    /**
     * The outline effect outlines areas of constant color. 
     * Implemented by subtracting a Sobel Filter.
     *
     * @param scene
     * @param camera
     * @param width
     * @param height
     * @param thickness
     * @param blendMode
     */
    public OutlineEffect(Scene scene, Camera camera, int width, int height, float thickness, BlendPass.BlendMode blendMode) {
        super();
        mScene = scene;
        mCamera = camera;
        mWidth = width;
        mHeight = height;
        mThickness = thickness;
        mBlendMode = blendMode;
    }

    public void initialize(Renderer renderer)
    {
        addPass(new SobelPass(mThickness/mWidth, mThickness/mHeight));
        CopyToNewRenderTargetPass copyPass = new CopyToNewRenderTargetPass("outlinePassTarget", renderer, mWidth, mHeight);
        addPass(copyPass);
        addPass(new RenderPass(mScene, mCamera, mScene.getBackgroundColor()));
        addPass(new BlendPass(mBlendMode, copyPass.getRenderTarget().getTexture()));
    }
}

