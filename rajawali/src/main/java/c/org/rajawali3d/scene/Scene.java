package c.org.rajawali3d.scene;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.materials.MaterialManager;
import c.org.rajawali3d.renderer.Renderable;
import c.org.rajawali3d.renderer.Renderer;
import c.org.rajawali3d.scene.graph.FlatTree;
import c.org.rajawali3d.scene.graph.NodeMember;
import c.org.rajawali3d.scene.graph.SceneGraph;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.materials.Material;
import org.rajawali3d.renderer.FrameTask;
import org.rajawali3d.textures.ATexture;
import org.rajawali3d.textures.TextureException;
import org.rajawali3d.textures.TextureManager;
import org.rajawali3d.util.RajLog;

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

    private final TextureManager  textureManager;
    private final MaterialManager materialManager;

    private volatile boolean needRestoreForNewContext = false;

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

    /**
     * The {@link Camera} currently being used to render the scene.
     */
    @GuardedBy("nextCameraLock")
    private Camera currentCamera;

    private Camera nextCamera; // The camera the scene should switch to on the next frame.
    private final Object nextCameraLock = new Object(); // Camera switching lock

    public Scene() {
        this(new FlatTree());
    }

    public Scene(@NonNull SceneGraph graph) {
        sceneGraph = graph;
        preCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        preDrawCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        postCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        frameTaskQueue = new LinkedList<>();

        textureManager = new TextureManager();
        materialManager = new MaterialManager();

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

    @GLThread
    @Override
    public void restoreForNewContextIfNeeded() {
        if (needRestoreForNewContext) {
            needRestoreForNewContext = false;
            textureManager.reloadTextures();
            // TODO: Restore materials
            // TODO: Restore VBOs
        }
    }

    /**
     * Retrieves the {@link TextureManager} associated with this {@link Scene}. Note that Renderers and GL
     * contexts are tied together.
     *
     * @return The {@link TextureManager} for this {@link Renderer}.
     */
    @NonNull
    public TextureManager getTextureManager() {
        return textureManager;
    }

    /**
     * Retrieves the {@link MaterialManager} associated with this {@link Scene}. Note that Renderers and GL
     * contexts are tied together.
     *
     * @return The {@link MaterialManager} for this {@link Renderer}.
     */
    @NonNull
    public MaterialManager getMaterialManager() {
        return materialManager;
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
        // The renderer is not null, so we can add the texture directly now
        final FrameTask task = getTextureManager().addTexture(texture);
        // Now queue the GL addition task
        internalOfferTask(task);
    }

    /**
     * Removes a texture from this scene. This can be called from any thread. If the calling thread is the GL thread,
     * this will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link ATexture} to be removed.
     */
    public void removeTexture(@NonNull final ATexture texture) {
        // The renderer is not null, so we can remove the texture directly now
        final FrameTask task = getTextureManager().removeTexture(texture);
        // Now queue the GL removal task
        internalOfferTask(task);
    }

    /**
     * Replaces a texture in this scene. This can be called from any thread. If the calling thread is the GL thread,
     * this will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link ATexture} to be replaced.
     */
    public void replaceTexture(@NonNull final ATexture texture) throws TextureException {
        // The renderer is not null, so we can replace the texture directly now
        final FrameTask task = getTextureManager().replaceTexture(texture);
        // Now queue the GL removal task
        internalOfferTask(task);
    }

    /**
     * Adds a material to this scene. This can be called from any thread. If the calling thread is the GL thread, this
     * will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param material {@link Material} to be added.
     */
    public void addMaterial(@NonNull final Material material) {

        // The renderer is not null, so we can add the material directly now
        final FrameTask task = getMaterialManager().addMaterial(material);
        // Now queue the GL addition task
        internalOfferTask(task);
    }

    /**
     * Removes a material from this scene. This can be called from any thread. If the calling thread is the GL thread,
     * this will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param material {@link ATexture} to be removed.
     */
    public void removeMaterial(@NonNull final Material material) {
        // The renderer is not null, so we can remove the material directly now
        final FrameTask task = getMaterialManager().removeMaterial(material);
        // Now queue the GL removal task
        internalOfferTask(task);
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
        // Execute frame tasks
        performFrameTasks();

        synchronized (nextCameraLock) {
            // Check if we need to switch the camera, and if so, do it.
            if (nextCamera != null) {
                switchCamera(nextCamera);
                nextCamera = null;
            }
        }

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
        final List<NodeMember> intersectedNodes = sceneGraph.intersection(currentCamera);

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

        //TODO: This will be an interaction point with the render pass manager. We don't want to check the
        // intersection with the camera multiple times. One possible exception would be for shadow mapping.

        // Loop each node and draw
        for (NodeMember member : intersectedNodes) {

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
            // Mark the context dirty
            needRestoreForNewContext = true;
            // Adjust viewport if necessary
            onRenderSurfaceSizeChanged();
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

    protected void performFrameTasks() {
        synchronized (frameTaskQueue) {
            //Fetch the first task
            FrameTask task = frameTaskQueue.poll();
            while (task != null) {
                task.run();
                //Retrieve the next task
                task = frameTaskQueue.poll();
            }
        }
    }

    @GuardedBy("nextCameraLock")
    @GLThread
    void switchCamera(@NonNull Camera nextCamera) {
        RajLog.d("Switching from camera: " + currentCamera + " to camera: " + nextCamera);
        currentCamera = nextCamera;
    }
}
