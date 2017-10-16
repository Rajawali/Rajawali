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
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.annotations.RequiresRenderTask;
import c.org.rajawali3d.control.BaseRenderDelegate;
import c.org.rajawali3d.control.RenderControlInternal;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.camera.Camera;
import c.org.rajawali3d.control.RenderContext;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.SceneInternal;
import c.org.rajawali3d.sceneview.render.DefaultRender;
import c.org.rajawali3d.sceneview.render.FrameRender;
import c.org.rajawali3d.sceneview.sky.Skybox;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.textures.TextureException;
import java.util.ArrayList;
import java.util.List;
import org.rajawali3d.math.Matrix4;

/**
 * A SceneView defines a Viewport within the bounds of the SurfaceView, and implements the rendering of its
 * Scene content into that Viewport each frame
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */

public class SceneView extends BaseRenderDelegate implements SceneViewInternal {

    // The current RenderContext
    @NonNull
    protected RenderContext renderContext;
    // The SceneInternal presented by this SceneView
    @NonNull
    protected Scene scene;
    // The Scene's graph

    // Background color components for this SceneView
    protected float red, blue, green, alpha;
    // The Skybox for this SceneView
    @Nullable
    protected Skybox skybox;
    // The Camera for this SceneView
    @NonNull
    protected Camera camera;
    // The current Viewport for this SceneView
    @NonNull
    protected Viewport viewport;

    @NonNull
    protected Rect viewportRect;
    //
    volatile boolean onScreenRenderEnabled;

    // The active RenderControlInternal to which this SceneViewInternal has been added
    @Nullable
    protected RenderControlInternal renderControlInternal;

    //
    @Nullable
    protected FrameRender onScreenRender;
    //
    @NonNull
    protected List<FrameRender> offScreenRenders = new ArrayList<>();

    //
    @NonNull
    protected Matrix4 viewMatrix;
    //
    @NonNull
    protected Matrix4 projectionMatrix;
    //
    @NonNull
    protected Matrix4 viewProjectionMatrix = new Matrix4();

    //
    @NonNull
    protected List<RenderableObject> renderableSceneObjects;

    // The last used object renderer
    // TODO if shared across SceneViews and FrameRenders, could save a number of GL state changes per frame
    @RenderThread
    @Nullable
    protected ObjectRenderer lastUsedObjectRenderer;

    /**
     * Use the default (whole-Surface) viewport and a DefaultRender
     * @param scene
     * @param camera
     * @return
     */
    @RenderThread
    public static SceneView create(@NonNull Scene scene, Camera camera) {
        return create(scene, camera, new Viewport());
    }

    /**
     * Use the default (whole-surface) viewport
     *
     * @param scene
     * @param camera
     * @param onScreenFrameRender
     * @return
     */
    @RenderThread
    public static SceneView create(@NonNull Scene scene, Camera camera, FrameRender onScreenFrameRender) {
        return create(scene, camera, new Viewport(), onScreenFrameRender);
    }

    /**
     * Use a DefaultRender
     *
     * @param scene
     * @param camera
     * @param viewport
     * @return
     */
    @RenderThread
    public static SceneView create(@NonNull Scene scene, Camera camera, Viewport viewport) {
        return create(scene, camera, viewport, null);
    }

    /**
     *
     * @param renderScene
     * @param camera
     * @param viewport
     * @param onScreenRender
     * @return
     */
    @RenderThread
    public static SceneView create(@NonNull Scene scene, @NonNull Camera camera, @NonNull Viewport viewport,
                                   @Nullable FrameRender onScreenRender) {

        SceneView sceneView = new SceneView();
        sceneView.scene = scene;
        sceneView.camera = camera;
        sceneView.viewport = viewport;

        // Set the onScreenRender
        sceneView.setOnScreenRender(onScreenRender == null ? new DefaultRender(sceneView) : onScreenRender);

        return sceneView;
    }

    // Prevent default construction
    private SceneView() {}

    //
    // SceneView interface
    //

    //
    // Scene
    //

    /**
     * Gets the Scene defining the content to be displayed by the SceneView
     *
     * @return the {@link Scene} instance presented in this view
     */
    @NonNull
    public Scene getSceneInternal() {
        return scene;
    }

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
    public void setBackgroundColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    /**
     * Sets the background color of the scene.
     *
     * @param color Android color integer.
     */
    public void setBackgroundColor(int color) {
        setBackgroundColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f,
                Color.alpha(color) / 255f);
    }

    /**
     * Retrieves the background color of the scene.
     *
     * @return Android color integer.
     */
    public int getBackgroundColor() {
        return Color.argb((int) (alpha *255f), (int) (red *255f), (int) (green *255f), (int) (blue *255f));
    }

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
    public boolean setSkybox(int resourceId) throws TextureException {
        /*
        final FrameTask task = new FrameTask() {
            @Override
            protected void doTask() {
                for (int i = 0, j = mCameras.size(); i < j; ++i)
                    mCameras.get(i).setFarPlane(1000);
            }
        };
        synchronized (mNextSkyboxLock) {
            mNextSkybox = new Cube(700, true, false);
            mNextSkybox.setDoubleSided(true);
            mSkyboxTexture = new Texture2D("skybox", mRenderer.getContext(), resourceId);
            Material material = new Material();
            material.setColorInfluence(0);
            material.addTexture(mSkyboxTexture);
            mNextSkybox.setMaterial(material);
        }
        return internalOfferTask(task);
        */
        return false;
    }
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
    public boolean setSkybox(int posx, int negx, int posy, int negy, int posz, int negz) throws TextureException {
        /*
        final FrameTask task = new FrameTask() {
            @Override
            protected void doTask() {
                for (int i = 0, j = mCameras.size(); i < j; ++i)
                    mCameras.get(i).setFarPlane(1000);
            }
        };
        synchronized (mNextSkyboxLock) {
            mNextSkybox = new Cube(700, true);
            int[] resourceIds = new int[] { posx, negx, posy, negy, posz, negz };

            //mSkyboxTexture = new CubeMapTexture("skybox", resourceIds);
            ((CubeMapTexture)mSkyboxTexture).isSkyTexture(true);
            Material mat = new Material();
            mat.setColorInfluence(0);
            mat.addTexture(mSkyboxTexture);
            mNextSkybox.setMaterial(mat);
        }
        return internalOfferTask(task);
        */
        return false;
    }

    /**
     * Creates a skybox with the specified 6 {@link Bitmap} textures.
     *
     * @param bitmaps {@link Bitmap} array containing the cube map textures.
     *
     * @return
     *
     */
    public boolean setSkybox(Bitmap[] bitmaps) {
        /*
        final FrameTask task = new FrameTask() {
            @Override
            protected void doTask() {
                for (int i = 0, j = mCameras.size(); i < j; ++i)
                    mCameras.get(i).setFarPlane(1000);
            }
        };
        final Cube skybox = new Cube(700, true);
        final CubeMapTexture texture = new CubeMapTexture("bitmap_skybox", bitmaps);
        texture.isSkyTexture(true);
        final Material material = new Material();
        material.setColorInfluence(0);
        try {
            material.addTexture(texture);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
        }
        skybox.setMaterial(material);
        synchronized (mNextCameraLock) {
            mNextSkybox = skybox;
        }
        return internalOfferTask(task);
        */
        return false;
    }

    /**
     *
     * @return
     */
    public Skybox getSkybox() {
        return skybox;
    }

    //
    // Camera
    //

    /**
     * Sets the Camera for this SceneView
     *
     * @param camera
     * @return
     */
    @RequiresRenderTask
    public void setCamera(@NonNull final Camera camera) {
        debugAssertNonNull(camera, "camera");
        this.camera = camera;
    }

    /**
     * Gets the current Camera for this SceneView
     * @return
     */
    @RequiresRenderTask
    @NonNull
    public Camera getCamera() {
        return camera;
    }

    //
    // Viewport
    //

    @RequiresRenderTask
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public Viewport getViewport() {
        return viewport;
    }

    //
    // FrameRenders
    //

    /**
     * Sets the primary on-screen {@link FrameRender} for the SceneView; setting a new instance replaces any
     * previous instance; initially a {@link DefaultRender}.
     *
     * @param frameRender
     */
    @RequiresRenderTask
    public void setOnScreenRender(@NonNull FrameRender frameRender) {
        if (onScreenRender.equals(frameRender)) {
            throw new IllegalStateException("Specified FrameRender is already the current onScreenRender!");
        }
        if (onScreenRender != null) {
            // Replacing onScreenRender
            onScreenRender.setOnScreen(false);
        }
        if (!frameRender.renderableToScreen()) {
            throw new IllegalStateException("Specified FrameRender is not renderable-to-screen!");
        }
        frameRender.initialize();
        frameRender.setOnScreen(true);
        this.onScreenRender = frameRender;
    }

    public void removeOnScreenRender() {
        // TODO
    }

    /**
     * Gets the current on-screen {@link FrameRender} for the SceneView, if any
     *
     * @return
     */
    @Nullable
    public FrameRender getOnScreenRender() {
        return onScreenRender;
    }

    /**
     * Sets whether the on-screen FrameRender is updated each frame. Disabling does not affect RenderTasks,
     * RenderFrameCallbacks, or off-screen FrameRenders. Also, when disabling, whether the content of
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
    @RequiresRenderTask
    public void addOffScreenRender(@NonNull FrameRender offScreenRender) {
        // TODO
    }

    /**
     *
     * @param offScreenRender
     * @return
     */
    public void removeOffScreenRender(@NonNull FrameRender offScreenRender) {
        // TODO
    }

    //
    // SceneViewInternal interface
    //

    public @NonNull
    SceneInternal getSceneInternal() {
        return scene;
    }

    @RenderThread
    @NonNull
    public List<RenderableObject> getRenderableSceneObjects() {
        return renderableSceneObjects;
    }

    @Override
    @RenderThread
    @NonNull
    public Matrix4 getViewMatrix() {
        return viewMatrix;
    }

    @Override
    @RenderThread
    @NonNull
    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    @Override
    @RenderThread
    @NonNull
    public Matrix4 getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }

    @NonNull
    @Override
    public ObjectRenderer getLastUsedObjectRenderer() {
        return null;
    }

    //
    //
    //

    @Override
    @RenderThread
    public void onRenderFrame() throws InterruptedException {
        try {
            // Update the camera matrices
            viewMatrix = camera.getViewMatrix();
            projectionMatrix = camera.getProjectionMatrix();
            if (projectionMatrix == null) {
                throw new IllegalStateException("Cannot render while camera has a null projection matrix.");
            }
            viewProjectionMatrix.setAll(projectionMatrix).multiply(viewMatrix);

            // Populate the list of the Scene's camera-visible RenderableObjects
            renderableSceneObjects = renderSceneGraph.visibleObjectIntersection(camera);

            // Run the onScreenRender
            if (onScreenRenderEnabled) {
                onScreenRender.render();
            }

            // Run any registered offScreenRenders
            for (FrameRender offScreenRender : offScreenRenders) {
                offScreenRender.render();
            }

        } finally {
            if (currentlyHeldReadLock != null) {
                currentlyHeldReadLock.unlock();
            }
        }
    }

    @Override
    public void onAddToSceneViewControl(@NonNull RenderControlInternal renderControlInternal) {
        // This method does not override the parent BaseRenderDelegate method, but it still needs to be called
        super.onAddToRenderControl();

        this.renderControlInternal = renderControlInternal;
        // An empty viewportRect defaults to the whole surface
        if (viewportRect.isEmpty()) {
            SurfaceSize surfaceSize = renderControlInternal.getSurfaceSize();
            viewportRect.set(0, 0, surfaceSize.width, surfaceSize.height);
        }
    }

    @Override
    public void onRemoveFromRenderControl() {
        // TODO can remove if nothing else is needed
        super.onRemoveFromRenderControl();
        renderControlInternal = null;
    }
}


