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
package c.org.rajawali3d.sceneview;

import android.graphics.Bitmap;
import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.core.SceneViewDelegate;
import c.org.rajawali3d.sceneview.render.DefaultRender;
import c.org.rajawali3d.sceneview.render.FrameRender;
import c.org.rajawali3d.scene.Scene;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import c.org.rajawali3d.textures.TextureException;

/**
 * Client interface for views managed by the RenderControl. A SceneView defines a viewportRect within the bounds of
 * the SurfaceView, and implements the rendering of its Scene content each frame
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */
public interface SceneView extends SceneViewDelegate {

    //
    // Scene
    //

    /**
     * Gets the Scene defining the content to be displayed by the SceneView
     *
     * @return the {@link Scene} instance presented in this view
     */
    @NonNull
    Scene getScene();

    //
    // Background color
    //

    /**
     * Sets the background color of the sceneView.
     *
     * @param red float red component (0-1.0f).
     * @param green float green component (0-1.0f).
     * @param blue float blue component (0-1.0f).
     * @param alpha float alpha component (0-1.0f).
     */
    void setBackgroundColor(float red, float green, float blue, float alpha);

    /**
     * Sets the background color of the renderScene.
     *
     * @param color Android color integer.
     */
    void setBackgroundColor(int color);

    /**
     * Retrieves the background color of the renderScene.
     *
     * @return Android color integer.
     */
    int getBackgroundColor();

    //
    // Skybox
    //

    /**
     * Creates a skybox with the specified single texture.
     *
     * @param resourceId int Resouce id of the skybox texture.
     * @throws TextureException
     *
     * @return {@code boolean} True if the clear task was queued successfully.
     */
    public boolean setSkybox(int resourceId) throws TextureException;

    /**
     * Creates and applies a skybox with the specified 6 textures.
     *
     * TODO specify faces relative to cube-local coordinate axes
     *
     * @param posX int Resource id for the TBD face.
     * @param negX int Resource id for the TBD face.
     * @param posY int Resource id for the TBD face.
     * @param negY int Resource id for the TBD face.
     * @param posZ int Resource id for the TBD face.
     * @param negZ int Resource id for the TBD face.
     *
     * @return
     *
     * @throws TextureException
     */
    boolean setSkybox(int posX, int negX, int posY, int negY, int posZ, int negZ) throws TextureException;

    /**
     * Creates a skybox with the specified 6 {@link Bitmap} textures.
     *
     * @param bitmaps {@link Bitmap} array containing the cube map textures.
     *
     * @return
     *
     */
    boolean setSkybox(Bitmap[] bitmaps);

    /**
     *
     * @return
     */
    Skybox getSkybox();

    //
    // Camera
    //

    /**
     * Sets the Camera for this SceneView via a RenderTask
     *
     * @param camera
     * @return
     */
    boolean setCamera(@NonNull Camera camera);

    /**
     * Gets the current Camera for this SceneView
     * @return
     */
    @NonNull Camera getCamera();

    //
    // Viewport
    //

    /**
     *
     * @param viewport
     */
    void setViewport(Viewport viewport);

    /**
     *
     * @return
     */
    Viewport getViewport();

    /**
     * Sets the primary on-screen viewport {@link FrameRender} for the SceneView; one is always defined, and
     * setting a new instance replaces the previous instance; initially a {@link DefaultRender}.
     *
     * @param onScreenRender
     */
    void setOnScreenRender(@NonNull FrameRender onScreenRender);

    /**
     * Gets the current on-screen {@link FrameRender} for the SceneView
     *
     * @return
     */
    @NonNull
    FrameRender getOnScreenRender();

    /**
     * Sets whether the on-screen FrameRender is updated each frame. Disabling does not affect RenderTasks,
     * RenderFrameCallbacks, animations, or off-screen FrameRenders. Also, when disabling, whether the content of
     * the on-screen viewport is replaced or updated
     *
     *
     * @param onScreenRenderEnabled {@code true}
     */
    //void setOnScreenRenderEnabled(boolean onScreenRenderEnabled);

    /**
     * Checks if the on-screen FrameRender is enabled
     *
     * @return
     */
    //boolean onScreenRenderEnabled();

    /**
     *
     * @param offScreenRender
     * @return
     */
    boolean addOffScreenRender(@NonNull FrameRender offScreenRender);

    /**
     *
     * @param offScreenRender
     * @return
     */
    boolean removeOffScreenRender(@NonNull FrameRender offScreenRender);
}
