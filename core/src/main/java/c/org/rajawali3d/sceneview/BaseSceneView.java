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
import c.org.rajawali3d.sceneview.camera.Camera;
import c.org.rajawali3d.control.BaseControlDelegate;
import c.org.rajawali3d.control.RenderContext;
import c.org.rajawali3d.control.RenderTask;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.BaseScene;
import c.org.rajawali3d.scene.RenderScene;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.render.DefaultRender;
import c.org.rajawali3d.sceneview.render.FrameRender;
import c.org.rajawali3d.sceneview.sky.Skybox;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.textures.TextureException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.util.RajLog;

/**
 * Abstract base for all SceneView implementations; adds the actual onRenderFrame() callback
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */

public class BaseSceneView extends BaseControlDelegate implements RenderSceneView {

    // The current RenderContext
    @NonNull
    protected RenderContext renderContext;
    // The RenderScene presented by this BaseSceneView
    @NonNull
    protected RenderScene renderScene;
    // The Scene's graph
    @NonNull
    protected RenderSceneGraph renderSceneGraph;
    // Background color components for this BaseSceneView
    protected float red, blue, green, alpha;
    // The Skybox for this BaseSceneView
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

    // The active SceneViewControl to which this SceneViewDelegate has been added
    @Nullable
    protected SceneViewControl sceneViewControl;

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

    //
    @Nullable
    Lock currentlyHeldReadLock;

    /**
     * Use the default (whole-Surface) viewport and a DefaultRender
     * @param scene
     * @param camera
     * @return
     */
    @RenderThread
    public static SceneView create(@NonNull BaseScene scene, Camera camera) {
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
    public static SceneView create(@NonNull BaseScene scene, Camera camera, FrameRender onScreenFrameRender) {
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
    public static SceneView create(@NonNull BaseScene scene, Camera camera, Viewport viewport) {
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
    public static SceneView create(@NonNull RenderScene renderScene, @NonNull Camera camera, @NonNull Viewport viewport,
                                   @Nullable FrameRender onScreenRender) {

        BaseSceneView sceneView = new BaseSceneView();
        sceneView.renderScene = renderScene;
        sceneView.renderSceneGraph = renderScene.getRenderSceneGraph();
        sceneView.camera = camera;
        sceneView.viewport = viewport;

        // Set the onScreenRender
        sceneView.setOnScreenRender(onScreenRender == null ? new DefaultRender(sceneView) : onScreenRender);

        return sceneView;
    }

    // Prevent default construction
    private BaseSceneView() {}

    //
    // SceneView interface
    //

    @Override
    @NonNull
    public Scene getScene() {
        return renderScene;
    }

    @Override
    public void setBackgroundColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public void setBackgroundColor(int color) {
        setBackgroundColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f,
                Color.alpha(color) / 255f);
    }

    @Override
    public int getBackgroundColor() {
        return Color.argb((int) (alpha *255f), (int) (red *255f), (int) (green *255f), (int) (blue *255f));
    }

    @Override
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

    @Override
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

    @Override
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

    public Skybox getSkybox() {
        return skybox;
    }

    //
    // Camera
    //

    @Override
    public boolean setCamera(@NonNull final Camera camera) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                syncCamera(camera);
            }
        };
        return executeRenderTask(task);
    }

    // Intentionally package private for performance
    @RenderThread
    void syncCamera(Camera camera) {
        if (!camera.equals(this.camera)) {
            RajLog.d("Switching from camera: " + this.camera + " to camera: " + camera);
            this.camera = camera;
        }
    }

    // TODO thread safety/sync?
    @Override
    public @NonNull Camera getCamera() {
        return camera;
    }

    //
    // Viewport
    //

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public Viewport getViewport() {
        return viewport;
    }

    //
    // FrameRenders
    //

    @Override
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

    @NonNull
    @Override
    public FrameRender getOnScreenRender() {
        return onScreenRender;
    }

    @Override
    public boolean addOffScreenRender(@NonNull FrameRender offScreenRender) {

        return false;
    }

    @Override
    public boolean removeOffScreenRender(@NonNull FrameRender offScreenRender) {
        return false;
    }

    //
    // RenderSceneView interface
    //

    public @NonNull
    RenderScene getRenderScene() {
        return renderScene;
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
    public void onAddToSceneViewControl(@NonNull SceneViewControl sceneViewControl) {
        // This method does not override the parent BaseControlDelegate method, but it still needs to be called
        super.onAddToRenderControl(sceneViewControl);

        this.sceneViewControl = sceneViewControl;
        // An empty viewportRect defaults to the whole surface
        if (viewportRect.isEmpty()) {
            SurfaceSize surfaceSize = renderStatus.getSurfaceSize();
            viewportRect.set(0, 0, surfaceSize.width, surfaceSize.height);
        }
    }

    @Override
    public void onRemoveFromRenderControl() {
        // TODO can remove if nothing else is needed
        super.onRemoveFromRenderControl();
        sceneViewControl = null;
    }
}


