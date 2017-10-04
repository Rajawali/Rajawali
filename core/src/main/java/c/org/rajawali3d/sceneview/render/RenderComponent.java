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

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.core.RenderContext;
import c.org.rajawali3d.sceneview.RenderSceneView;

/**
 * A {@link RenderComponent} is the base type for composing the rendering operations used by a
 * {@link RenderSceneView} to render its per-frame outputs. Each component maps input objects and/or textures to
 * either the SceneView's (on-screen) system framebuffer viewport or to a SceneView-specific (off-screen) framebuffer
 * object.
 * <p>
 * Leaf components in the pattern are implementations of {@link Subpass}, a configuration and use of the underlying
 * {@link RenderContext} pipeline to write to one or more logical target buffers within a framebuffer. Composite
 * components in the pattern are implementations of {@link RenderPass}, which maintains a list of child
 * RenderComponents in render order and that maps outputs of upstream components to inputs of downstream components
 * in that chain (for example, a post-processing effect); the mappings must form a logical directed acyclic graph.
 * Root composite components are implementations of {@link FrameRender}, a complete, stand-alone per-frame render function of a SceneView.
 * </p>
 * <p>
 * Every SceneView has one OnScreenFrameRender that renders its Scene objects into its on-screen viewport. Optional
 * off-screen FrameRenders may also be added to a SceneView for such operations as object picking, debugging,
 * bitmap creation, computations, and so on.
 * </p>
 *
 * @author Randy Picolet
 */
public abstract class RenderComponent {

    /**
     *
     */
    public static final boolean RENDERABLE_TO_SCREEN = true;

    /**
     *
     */
    public static final boolean TARGET_SIZE_TRACKS_VIEWPORT = true;

    /**
     *
     */
    @NonNull
    protected final RenderSceneView renderSceneView;

    /**
     *
     */
    protected RenderContext minVersionRenderContext;

    /**
     *
     */
    protected boolean renderableToScreen;

    /**
     *
     */
    protected boolean rendersToScreen;

    /**
     *
     */
    protected boolean targetSizeTracksViewport;

    //
    // Construction
    //

    /**
     *
     * @param renderSceneView
     */
    protected RenderComponent(@NonNull RenderSceneView renderSceneView) {
        this.renderSceneView = renderSceneView;
    }

    //
    // Access
    //

    /**
     *
     * @return
     */
    public RenderSceneView getRenderSceneView() {
        return renderSceneView;
    }

    /**
     *
     * @return
     */
    public final RenderContext getMinVersionRenderContext() {
        return minVersionRenderContext;
    }

    /**
     *
     * @return
     */
    public final boolean renderableToScreen() {
        return renderableToScreen;
    }

    /**
     *
     * @return
     */
    public final boolean rendersToScreen() {
        return rendersToScreen;
    }

    /**
     *
     * @return
     */
    public final boolean targetSizeTracksViewport() {
        return targetSizeTracksViewport;
    }

    //
    // Initialization
    //

    /**
     *
     */
    @RenderThread
    public abstract void initialize();

    /**
     *
     */
    @CallSuper
    protected void setRendersToScreen(boolean rendersToScreen) {
        if (rendersToScreen && !renderableToScreen) {
            throw new IllegalStateException("RenderComponent cannot be rendered to the screen!");
        }
        this.rendersToScreen = rendersToScreen;
    }

    //
    // Rendering
    //

    /**
     *
     */
    @RenderThread
    public abstract void render();


    //
    // Destruction
    //

    /**
     * Override as needed to release resources...
     */
    @RenderThread
    public void destroy() {};
}
