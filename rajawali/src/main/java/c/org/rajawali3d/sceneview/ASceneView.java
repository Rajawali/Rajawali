package c.org.rajawali3d.sceneview;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.core.AFrameDelegate;
import c.org.rajawali3d.core.RenderStatus;
import c.org.rajawali3d.core.RenderContext;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.core.RenderTask;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.AScene;
import c.org.rajawali3d.scene.graph.SceneGraph;
import c.org.rajawali3d.surface.SurfaceSize;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.util.RajLog;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Abstract base for all SceneView implementations; adds the actual onRender() callback
 *
 * TODO remove indirect dependencies here on GLES (via Camera->frustum Planes->IndexedGeometry)
 * TODO add factory methods (here? in RenderControl?) that instantiate the concrete type per the RenderContextType
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */

public abstract class ASceneView extends AFrameDelegate implements SceneView {

    private static final String TAG = "ASceneView";

    // The Scene presented by this ASceneView
    @NonNull protected Scene scene;
    // The Scene's graph
    @NonNull protected SceneGraph sceneGraph;
    // The Camera for this ASceneView
    @NonNull protected Camera camera;
    // The viewportRect for this ASceneView
    @NonNull protected Rect viewportRect;
    //
    volatile boolean viewportVisible;
    //
    @NonNull protected Matrix4 viewMatrix;
    //
    @NonNull protected Matrix4 projectionMatrix;
    //
    @NonNull protected Matrix4 viewProjectionMatrix = new Matrix4();
    //
    @Nullable
    Lock currentlyHeldReadLock;

    @RenderThread
    protected @Nullable
    ObjectRenderer lastUsedRenderer; // Reference to the last used object renderControl

    public static SceneView create(@NonNull AScene scene, Camera camera) {
        // Start with an empty viewportRect to flag whole-surface
        return create(scene, camera, new Rect());
    }

    public static SceneView create(@NonNull AScene scene, Camera camera, Rect viewportRect) {

        ASceneView sceneView = null;
        switch(RenderContext.getType()) {
            case OPEN_GL_ES:
                sceneView = new GLESSceneView();
                break;
            default:
                // TODO log, exeption, or both?
        }
        sceneView.scene = scene;
        sceneView.camera = camera;
        sceneView.viewportRect = viewportRect;

        return sceneView;
    }

    protected ASceneView() {
    }

    public @NonNull
    Scene getScene() {
        return scene;
    }

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

    @Override
    public boolean setViewportRect(@NonNull final Rect viewport) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                syncViewportRect(viewport);
            }
        };
        return executeRenderTask(task);
    }

    // Intentionally package private for performance
    @RenderThread
    void syncViewportRect(Rect viewport) {
        if (!viewport.equals(this.viewportRect)) {
            this.viewportRect.set(viewport);
        }
    }

    @Override
    public @NonNull Rect getViewportRect() {
        return viewportRect;
    }

    @Override
    public boolean setViewportDepthOrder(@IntRange(from = 0) int depthOrder) {
        // TODO
        return false;
    }

    @Override
    public int getViewportDepthOrder() {
        // TODO
        return 0;
    }

    @Override
    public void setViewportVisible(boolean viewportVisible) {
        this.viewportVisible = viewportVisible;
    }

    @Override
    public boolean isViewportVisible() {
        return viewportVisible;
    }

    @RenderThread
    public void onRender() throws InterruptedException {
        currentlyHeldReadLock = scene.acquireReadLock();
        try {
            // Prepare the camera matrices
            viewMatrix = camera.getViewMatrix();
            projectionMatrix = camera.getProjectionMatrix();
            if (projectionMatrix == null) {
                throw new IllegalStateException("Cannot render while current camera has a null projection matrix.");
            }
            viewProjectionMatrix.setAll(projectionMatrix).multiply(viewMatrix);

            // Render the visible intersected objects
            renderObjects(sceneGraph.visibleObjectIntersection(camera));
        } finally {
            if (currentlyHeldReadLock != null) {
                currentlyHeldReadLock.unlock();
            }
        }
    }

    protected abstract void renderObjects(List<RenderableObject> renderableObjects);

    @Override
    public void onAddToRenderControl(@NonNull RenderStatus renderStatus) {
        super.onAddToRenderControl(renderStatus);
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
    }
}
