/**
 * Copyright 2017 Dennis Ippel
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
package c.org.rajawali3d.sceneview.render;

import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.object.Object3D;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.RenderSceneView;
import c.org.rajawali3d.sceneview.SceneView;

/**
 * <p>
 * A {@link FrameRender} is a top-level root {@link RenderComponent} for a {@link SceneView}, and contains at least
 * one nested {@link RenderPassChain}.
 *
 * The flattened in-order list of a FrameRender's nested components results in a sequence
 * of Subpasses that map the visible {@link Object3D}s determined by a SceneView's {@link Scene} and {@link Camera}
 * to a final (on-screen or off-screen) target, so the first Subpass must render from Scene Object3Ds, while the
 * final Subpass produces the final FrameRender output.
 * </p>
 * <p>
 * Every SceneView has one FrameRender that maps its Scene's Object3Ds to its on-screen viewport, and may also
 * have one or more additional FrameRenders that map its objects to off-screen FramebufferObjects to support other
 * per-frame functions such as object picking, debugging, creating bitmaps, etc.
 * </p>
 * @author Randy Picolet
 */

public abstract class FrameRender extends CompositeRender<RenderPassChain> {

    //
    // Construction
    //

    protected FrameRender(RenderSceneView renderSceneView) {
        super(renderSceneView);
    }

    //
    // Initialization
    //

    /**
     * For use by BaseSceneView only...
     * @param onScreen
     */
    public void setOnScreen(boolean onScreen) {
        setRendersToScreen(onScreen);
    }
}
