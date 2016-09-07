package c.org.rajawali3d.scene;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.renderer.Renderable;
import c.org.rajawali3d.renderer.Renderer;
import c.org.rajawali3d.scene.graph.FlatTree;
import c.org.rajawali3d.scene.graph.SceneGraph;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.locks.Lock;

/**
 * A {@link Scene} is a self contained, renderable world. {@link Scene}s are responsible for managing all aspects of
 * what is rendered - objects, cameras, lights, and materials. All draw operations are managed by a scene and objects
 * cannot be shared across scenes. Unless otherwise specified, the default behavior is to use a {@link FlatTree}
 * scene graph.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class Scene implements Renderable {

    @Nullable
    private Renderer renderer;

    @NonNull
    private SceneGraph sceneGraph;

    @Nullable Lock currentlyHeldWriteLock;
    @Nullable Lock currentlyHeldReadLock;

    protected int currentViewportWidth;
    protected int currentViewportHeight; // The current width and height of the GL viewport
    protected int overrideViewportWidth;
    protected int overrideViewportHeight; // The overridden width and height of the GL viewport

    public Scene() {
        sceneGraph = new FlatTree();
        initialize();
    }

    public Scene(@NonNull SceneGraph graph) {
        sceneGraph = graph;
        initialize();
    }

    @GLThread
    @Override
    public void setRenderer(@Nullable Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void onRenderSurfaceSizeChanged(int width, int height) throws IllegalStateException {
        if (renderer == null) {
            throw new IllegalStateException("Scene registered to an unknown renderer implementation.");
        }
        final int wViewport = overrideViewportWidth > -1 ? overrideViewportWidth : renderer.getDefaultViewportWidth();
        final int hViewport = overrideViewportHeight > -1 ? overrideViewportHeight : renderer.getDefaultViewportHeight();
        setViewPort(wViewport, hViewport);
    }

    @Override
    public void clearOverrideViewportDimensions() {
        overrideViewportWidth = -1;
        overrideViewportHeight = -1;
        if (renderer != null) {
            setViewPort(renderer.getDefaultViewportWidth(), renderer.getDefaultViewportHeight());
        }
    }

    @Override
    public void setOverrideViewportDimensions(int width, int height) {
        overrideViewportWidth = width;
        overrideViewportHeight = height;
        setViewPort(overrideViewportWidth, overrideViewportHeight);
    }

    @Override
    public int getOverrideViewportWidth() {
        return overrideViewportWidth;
    }

    public int getOverrideViewportHeight() {
        return overrideViewportHeight;
    }

    @Override
    public int getViewportWidth() {
        return currentViewportWidth;
    }

    @Override
    public int getViewportHeight() {
        return currentViewportHeight;
    }

    @GLThread
    @Override
    public void render(final long ellapsedRealtime, final double deltaTime) throws InterruptedException {
        currentlyHeldReadLock = sceneGraph.acquireReadLock();
        try {
            internalRender(ellapsedRealtime, deltaTime);
        } finally {
            if (currentlyHeldReadLock != null) {
                currentlyHeldReadLock.unlock();
            }
        }
    }

    /**
     * Requests thread safe access to modify this {@link Scene}. This is useful if you need to make a number of
     * changes, allowing you to batch them into a single lock acquisition rather than acquiring the lock for each
     * modification.
     *
     * @param modifier {@link SceneModifier} instance which will be called when the lock has been acquired.
     * @throws InterruptedException Thrown if the requesting thread is interrupted while waiting for lock acquisition.
     */
    public void requestModifyScene(@NonNull SceneModifier modifier) throws InterruptedException {
        currentlyHeldWriteLock = sceneGraph.acquireWriteLock();
        try {
            modifier.doModifications(sceneGraph);
        } finally {
            if (currentlyHeldWriteLock != null) {
                currentlyHeldWriteLock.unlock();
            }
        }
    }

    protected void initialize() {
        overrideViewportWidth = -1;
        overrideViewportHeight = -1;
    }

    /**
     * Sets the GL Viewport used. User code is free to override this method, so long as the viewport is set somewhere
     * (and the projection matrix updated).
     *
     * @param width {@code int} The viewport width in pixels.
     * @param height {@code int} The viewport height in pixels.
     */
    @GLThread
    protected void setViewPort(int width, int height) {
        if (width != currentViewportWidth || height != currentViewportHeight) {
            currentViewportWidth = width;
            currentViewportHeight = height;
            // TODO: Update projection matrix
            //updateProjectionMatrix(width, height);
            GLES20.glViewport(0, 0, width, height);
        }
    }

    @RequiresReadLock
    @GLThread
    protected void internalRender(final long ellapsedRealtime, final double deltaTime) {

        // Determine which objects we will be rendering

        // Update the model matrix of all these objects
    }
}
