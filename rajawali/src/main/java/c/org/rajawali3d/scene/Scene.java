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
import c.org.rajawali3d.textures.ATexture;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.renderer.FrameTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

/**
 * A {@link Scene} is a self contained, renderable world. {@link Scene}s are responsible for managing all aspects of
 * what is rendered - objects, cameras, lights, and materials. All draw operations are managed by a scene and objects
 * cannot be shared across renderables. Unless otherwise specified, the default behavior is to use a {@link FlatTree}
 * scene graph.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class Scene implements Renderable {

    private static final String TAG = "Scene";

    @GuardedBy("preCallbacks")
    private final List<SceneFrameCallback> preCallbacks;

    @GuardedBy("preDrawCallbacks")
    private final List<SceneFrameCallback> preDrawCallbacks; //TODO: Are these necessary anymore?

    @GuardedBy("postCallbacks")
    private final List<SceneFrameCallback> postCallbacks;

    @GuardedBy("frameTaskQueue")
    private final Queue<FrameTask> frameTaskQueue;

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
        this(new FlatTree());
    }

    public Scene(@NonNull SceneGraph graph) {
        sceneGraph = graph;
        preCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        preDrawCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        postCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        frameTaskQueue = new LinkedList<>();
        initialize();
    }

    @GLThread
    @Override
    public void setRenderer(@Nullable Renderer renderer) {
        if (renderer != null) {
            onRendererSet(renderer);
        } else {
            onRendererCleared();
        }
    }

    @Override
    public void onRenderSurfaceSizeChanged() throws IllegalStateException {
        if (renderer == null) {
            throw new IllegalStateException("Scene registered to an unknown renderer implementation.");
        }
        final int wViewport = overrideViewportWidth > -1 ? overrideViewportWidth : renderer.getDefaultViewportWidth();
        final int hViewport = overrideViewportHeight > -1 ? overrideViewportHeight
                                                          : renderer.getDefaultViewportHeight();
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

    @Override
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
     *
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

    /**
     * Register a frame callback for this scene.
     *
     * @param callback {@link SceneFrameCallback} to be registered.
     */
    public void registerFrameCallback(@NonNull SceneFrameCallback callback) {
        if (callback.callPreFrame()) {
            preCallbacks.add(callback);
        }
        if (callback.callPreDraw()) {
            preDrawCallbacks.add(callback);
        }
        if (callback.callPostFrame()) {
            postCallbacks.add(callback);
        }
    }

    /**
     * Remove a frame callback. If the callback is not a member of the scene, nothing will happen.
     *
     * @param callback {@link SceneFrameCallback} to be unregistered.
     */
    public void unregisterFrameCallback(@NonNull SceneFrameCallback callback) {
        if (callback.callPreFrame()) {
            preCallbacks.remove(callback);
        }
        if (callback.callPreDraw()) {
            preDrawCallbacks.remove(callback);
        }
        if (callback.callPostFrame()) {
            postCallbacks.remove(callback);
        }
    }

    /**
     * Adds a texture to this scene. This can be called from any thread. If the calling thread is the GL thread, this
     * will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link ATexture} to be added.
     */
    public void addTexture(@NonNull final ATexture texture) {
        if (renderer == null) {
            // The renderer is null, so we need to queue the texture manager add first
            internalOfferTask(new FrameTask() {
                @Override
                protected void doTask() throws Exception {
                    final FrameTask task = renderer.getTextureManager().addTexture(texture);
                    // Now queue the GL addition task
                    internalOfferTask(task);
                }
            });
        } else {
            // The renderer is not null, so we can add the texture directly now
            final FrameTask task = renderer.getTextureManager().addTexture(texture);
            // Now queue the GL addition task
            internalOfferTask(task);
        }
    }

    /**
     * Removes a texture to this scene. This can be called from any thread. If the calling thread is the GL thread, this
     * will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link ATexture} to be removed.
     */
    public void removeTexture(@NonNull final ATexture texture) {
        if (renderer == null) {
            // The renderer is null, so we need to queue the texture manager remove first. This is an edge case that
            // is unlikely to happen. This occurrence will mean someone has either decided to remove a texture from a
            // scene that is not currently attached. The texture manager will handle cases of this not being
            // necessary, but we need to assume it is to prevent leaks.
            internalOfferTask(new FrameTask() {
                @Override
                protected void doTask() throws Exception {
                    final FrameTask task = renderer.getTextureManager().removeTexture(texture);
                    // Now queue the GL removal task
                    internalOfferTask(task);
                }
            });
        } else {
            // The renderer is not null, so we can remove the texture directly now
            final FrameTask task = renderer.getTextureManager().removeTexture(texture);
            // Now queue the GL removal task
            internalOfferTask(task);
        }
    }

    /**
     * Removes all {@link SceneFrameCallback} objects from the scene.
     */
    public void clearFrameCallbacks() {
        preCallbacks.clear();
        preDrawCallbacks.clear();
        postCallbacks.clear();
    }

    /**
     * Initializes the scene.
     */
    protected void initialize() {
        overrideViewportWidth = -1;
        overrideViewportHeight = -1;
    }

    /**
     * Sets the GL Viewport used. User code is free to override this method, so long as the viewport is set somewhere
     * (and the projection matrix updated).
     *
     * @param width  {@code int} The viewport width in pixels.
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
        // Execute onPreFrame callbacks
        // We explicitly break out the steps here to help the compiler optimize
        final int preCount = preCallbacks.size();
        if (preCount > 0) {
            synchronized (preCallbacks) {
                for (int i = 0; i < preCount; ++i) {
                    preCallbacks.get(i).onPreFrame(ellapsedRealtime, deltaTime);
                }
            }
        }

        // Determine which objects we will be rendering

        // Execute onPreDraw callbacks
        // We explicitly break out the steps here to help the compiler optimize
        final int preDrawCount = preDrawCallbacks.size();
        if (preDrawCount > 0) {
            synchronized (preDrawCallbacks) {
                for (int i = 0; i < preDrawCount; ++i) {
                    preDrawCallbacks.get(i).onPreDraw(ellapsedRealtime, deltaTime);
                }
            }
        }

        // Execute onPostFrame callbacks
        // We explicitly break out the steps here to help the compiler optimize
        final int postCount = postCallbacks.size();
        if (postCount > 0) {
            synchronized (postCallbacks) {
                for (int i = 0; i < postCount; ++i) {
                    postCallbacks.get(i).onPostFrame(ellapsedRealtime, deltaTime);
                }
            }
        }
    }

    protected void onRendererSet(@NonNull Renderer renderer) {
        if (renderer != this.renderer) {
            // Set the new renderer
            this.renderer = renderer;
            // Adjust viewport if necessary
            onRenderSurfaceSizeChanged();
            // Reconfigure resources to new GL context
        }
    }

    protected void onRendererCleared() {

    }

    protected boolean internalOfferTask(FrameTask task) {
        if (renderer != null && renderer.isGLThread()) {
            // If we have a renderer and the calling thread is the GL thread, do the task now.
            task.run();
            return true;
        } else {
            // This cant be run now and must be queued.
            synchronized (frameTaskQueue) {
                return frameTaskQueue.offer(task);
            }
        }
    }
}
