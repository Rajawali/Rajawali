package c.org.rajawali3d.scene;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.engine.Engine;
import c.org.rajawali3d.engine.RenderModel;
import c.org.rajawali3d.materials.MaterialManager;
import c.org.rajawali3d.scene.graph.FlatTree;
import c.org.rajawali3d.scene.graph.SceneGraph;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.materials.Material;
import org.rajawali3d.renderer.FrameTask;
import org.rajawali3d.textures.ATexture;
import org.rajawali3d.textures.TextureException;
import org.rajawali3d.textures.TextureManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

/**
 * A {@link Scene} is a self contained, renderable world model. {@link Scene}s are responsible for
 * managing all aspects of what is rendered - objects, lights, and materials. Objects, lights and
 * materials cannot be shared across models (TODO is this true?).
 *
 * Unless otherwise specified, the default behavior is to use a {@link FlatTree}scene graph.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class Scene implements RenderModel {

    private static final String TAG = "Scene";

    @GuardedBy("newFrameCallbacks")
    private final List<SceneFrameCallback> newFrameCallbacks;

    @GuardedBy("endFrameCallbacks")
    private final List<SceneFrameCallback> endFrameCallbacks;

    @GuardedBy("frameTaskQueue")
    private final Queue<FrameTask> frameTaskQueue;

    private final TextureManager  textureManager;
    private final MaterialManager materialManager;

    private volatile boolean needRestoreForNewContext = false;

    @Nullable
    private Engine engine;

    @NonNull
    private SceneGraph sceneGraph;

    @Nullable Lock currentlyHeldWriteLock;
    @Nullable Lock currentlyHeldReadLock;

    public Scene() {
        this(new FlatTree());
    }

    public Scene(@NonNull SceneGraph graph) {
        sceneGraph = graph;
        newFrameCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        endFrameCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        frameTaskQueue = new LinkedList<>();

        textureManager = new TextureManager();
        materialManager = new MaterialManager();
    }



    public SceneGraph getSceneGraph() {
        return sceneGraph;
    }

    /**
     * Called on registration boundaries
     * @param engine The active {@link Engine} or {@code null}.
     */
    @GLThread
    public void setEngine(@Nullable Engine engine) {
        if (engine != null) {
            onEngineSet(engine);
        } else {
            onEngineCleared();
        }
    }

    @Override
    public void onFrameStart(double deltaTime) throws InterruptedException {
        // TODO Scene-level frame tasks, animations, callback propagation to client
    }

    @Override
    public void onFrameEnd(double deltaTime) throws InterruptedException {
        // TODO callback propagation to client
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
     * @return The {@link TextureManager} for this {@link Engine}.
     */
    @NonNull
    public TextureManager getTextureManager() {
        return textureManager;
    }

    /**
     * Retrieves the {@link MaterialManager} associated with this {@link Scene}. Note that Renderers and GL
     * contexts are tied together.
     *
     * @return The {@link MaterialManager} for this {@link Engine}.
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
        if (callback.callFrameStart()) {
            newFrameCallbacks.add(callback);
        }
        if (callback.callFrameEnd()) {
            endFrameCallbacks.add(callback);
        }
    }

    /**
     * Remove a frame callback. If the callback is not a member of the scene, nothing will happen.
     *
     * @param callback {@link SceneFrameCallback} to be unregistered.
     */
    public void unregisterFrameCallback(@NonNull SceneFrameCallback callback) {
        if (callback.callFrameStart()) {
            newFrameCallbacks.remove(callback);
        }
        if (callback.callFrameEnd()) {
            endFrameCallbacks.remove(callback);
        }
    }

    /**
     * Adds a texture to this scene. This can be called from any thread. If the calling thread is the GL thread, this
     * will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link ATexture} to be added.
     */
    public void addTexture(@NonNull final ATexture texture) {
        // The engine is not null, so we can add the texture directly now
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
        // The engine is not null, so we can remove the texture directly now
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
        // The engine is not null, so we can replace the texture directly now
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

        // The engine is not null, so we can add the material directly now
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
        // The engine is not null, so we can remove the material directly now
        final FrameTask task = getMaterialManager().removeMaterial(material);
        // Now queue the GL removal task
        internalOfferTask(task);
    }

    /**
     * Removes all {@link SceneFrameCallback} objects from the scene.
     */
    public void clearFrameCallbacks() {
        newFrameCallbacks.clear();
        endFrameCallbacks.clear();
    }

    protected void onEngineSet(@NonNull Engine engine) {
        if (engine != this.engine) {
            // Set the new engine
            this.engine = engine;
            // Mark the context dirty
            needRestoreForNewContext = true;
        }
    }

    protected void onEngineCleared() {
    }

    protected boolean internalOfferTask(FrameTask task) {
        if (engine != null && engine.isGLThread()) {
            // If we have a engine and the calling thread is the GL thread, do the task now.
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
}
