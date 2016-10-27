package c.org.rajawali3d.sceneview;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.engine.Engine;
import c.org.rajawali3d.engine.RenderModel;
import c.org.rajawali3d.engine.RenderView;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.scene.graph.NodeMember;
import java.util.List;
import java.util.concurrent.locks.Lock;
import net.jcip.annotations.GuardedBy;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.util.RajLog;

/**
 * Author: Randy Picolet
 */

public class SceneView implements RenderView {
    private static final String TAG = "SceneView";

    @Nullable
    private Engine engine;

    @NonNull
    private Scene scene;

    // TODO probably need a decorator Viewport class around the Rect to flag changes...
    @NonNull
    private Rect viewport;

    /**
     * The {@link Camera} currently being used to render the scene.
     */
    @GuardedBy("nextCameraLock")
    private Camera currentCamera;

    private Camera nextCamera; // The camera the scene should switch to on the next frame.
    private final Object nextCameraLock = new Object(); // Camera switching lock

    @NonNull
    @GLThread
    private Matrix4 viewProjectionMatrix = new Matrix4();

    @Nullable
    @GLThread
    private ObjectRenderer lastUsedRenderer; // Reference to the last used object engine

    @Nullable
    Lock currentlyHeldWriteLock;
    @Nullable
    Lock currentlyHeldReadLock;

    // TODO Not sure about volatile semantics here
    private volatile boolean isDisabled;

    public SceneView(@NonNull Rect viewport) {
        this.viewport = viewport;
    }

    public SceneView(@NonNull Scene scene) {
        this.viewport = new Rect();
        this.scene = scene;
    }

    public SceneView(@NonNull Rect viewport, @NonNull Scene scene) {
        this.viewport = new Rect(viewport);
    }

    @GLThread
    public void setEngine(@Nullable Engine engine) {
        if (engine != null) {
            onEngineSet(engine);
        } else {
            onEngineCleared();
        }
    }

    @Nullable
    @Override
    public RenderModel getRenderModel() {
        return scene;
    }

    @Override
    public void setViewport(Rect viewport) {
        this.viewport.set(viewport);
    }

    @Override
    public Rect getViewport() {
        return viewport;
    }

    @Override
    public int getViewportWidth() {
        return viewport.width();
    }

    @Override
    public int getViewportHeight() {
        return viewport.height();
    }

    @Override
    public void isDisbled(boolean disabled) {
        this.isDisabled = disabled;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }


    @GLThread
    @Override
    public void onFrameStart(final double deltaTime)
            throws InterruptedException {
        // TODO is a scene lock actually needed here?  This may all be independent of the Scene...
        // TODO is a sceneView lock needed here?
        //currentlyHeldReadLock = scene.acquireReadLock();
        try {
            // TODO frame tasks for changes to viewport and/or render passes
            // Execute frame tasks
            //performFrameTasks();

            // TODO view-specific frame callbacks could be useful, e.g. enabling viewport animations
        } finally {
            if (currentlyHeldReadLock != null) {
                currentlyHeldReadLock.unlock();
            }
        }
    }

    @GLThread
    @Override
    public void onRenderView() throws InterruptedException {
        // TODO Lock should probably be on the whole Scene, not just its Graph
        //currentlyHeldReadLock = scene.acquireReadLock();
        try {
            internalRender();
        } finally {
            if (currentlyHeldReadLock != null) {
                currentlyHeldReadLock.unlock();
            }
        }
    }

    @Override
    public void onFrameEnd(double deltaTime) throws InterruptedException {
        // TODO Anything?
    }

    @RequiresReadLock
    @GLThread
    protected void internalRender() throws IllegalStateException {

        // TODO Probably need to mimic the nextCameraLock pattern for viewport changes?
        //updateProjectionMatrix(width, height);
        //GLES20.glViewport(0, 0, width, height);

        synchronized (nextCameraLock) {
            // Check if we need to switch the camera, and if so, do it.
            if (nextCamera != null) {
                switchCamera(nextCamera);
                nextCamera = null;
            }
        }

        // Prepare the camera matrices
        final Matrix4 viewMatrix = currentCamera.getViewMatrix();
        final Matrix4 projectionMatrix = currentCamera.getProjectionMatrix();

        if (projectionMatrix == null) {
            throw new IllegalStateException("Cannot render while current camera has a null projection matrix.");
        }

        viewProjectionMatrix.setAll(projectionMatrix).multiply(viewMatrix);

        // Determine which objects we will be rendering
        final List<NodeMember> intersectedNodes = scene.getSceneGraph().intersection(currentCamera);

        // still TODO: This will be an interaction point with the render pass manager. We don't want to check the
        // intersection with the camera multiple times. One possible exception would be for shadow mapping. Probably
        // a loop

        // Fetch the current render type
        int type = 0;

        // Loop each node and draw
        for (NodeMember member : intersectedNodes) {
            lastUsedRenderer = member.render(type, lastUsedRenderer, viewMatrix, projectionMatrix, viewProjectionMatrix);
        }
    }

    protected void onEngineSet(@NonNull Engine engine) {
        if (engine != this.engine) {
            // Set the new engine
            this.engine = engine;
            // TODO not sure what's needed here...
            // Mark the context dirty
            // needRestoreForNewContext = true;
            // Default the viewport to the whole surface
            if (viewport.isEmpty()) {
                viewport.set(0, 0, engine.getSurfaceWidth(), engine.getSurfaceHeight());
            }
        }
    }

    protected void onEngineCleared() {
    }

    @GuardedBy("nextCameraLock")
    @GLThread
    void switchCamera(@NonNull Camera nextCamera) {
        RajLog.d("Switching from camera: " + currentCamera + " to camera: " + nextCamera);
        currentCamera = nextCamera;
    }
}
