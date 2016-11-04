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
import org.rajawali3d.renderer.FrameTask;
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

    @Nullable private Engine engine;

    @NonNull private SceneGraph sceneGraph;

    @Nullable Lock currentlyHeldWriteLock;
    @Nullable Lock currentlyHeldReadLock;

    /**
     * Constructs a new {@link Scene} using the default ({@link FlatTree}) scene graph implementation.
     */
    public Scene() {
        this(new FlatTree());
    }

    /**
     * Constructs a new {@link Scene} using the provided {@link SceneGraph} implementation.
     *
     * @param graph The {@link SceneGraph} instance which should be used.
     */
    public Scene(@NonNull SceneGraph graph) {
        sceneGraph = graph;
        newFrameCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        endFrameCallbacks = Collections.synchronizedList(new ArrayList<SceneFrameCallback>());
        frameTaskQueue = new LinkedList<>();

        textureManager = new TextureManager(this);
        materialManager = new MaterialManager(this);

    }

    public SceneGraph getSceneGraph() {
        return sceneGraph;
    }

    /**
     * Called on registration boundaries
     *
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
     * Adds a {@link FrameTask} to the internal queue to be executed. If the calling thread is the render thread, the
     * task will be executed now, otherwise it will be queued for execution at the next frame start.
     *
     * @param task {@link FrameTask} to execute.
     * @return {@code true} If the task was successfully accepted for execution.
     */
    public boolean offerTask(FrameTask task) {
        if (engine != null && engine.isRenderThread()) {
            // If we have a renderer and the calling thread is the render thread, do the task now.
            task.run();
            return true;
        } else {
            // This cant be run now and must be queued.
            synchronized (frameTaskQueue) {
                return frameTaskQueue.offer(task);
            }
        }
    }

    /**
     * Removes all {@link SceneFrameCallback} objects from the scene.
     */
    public void clearFrameCallbacks() {
        newFrameCallbacks.clear();
        endFrameCallbacks.clear();
    }

    /**
     * Called when a {@link Engine} has been set on this scene. This is an opportunity to prepare for the frame
     * request about to come in. Subclasses should be sure to call {@code super.onRendererSet(renderer)} to ensure
     * proper behavior.
     *
     * @param engine The {@link Engine} this {@link Scene} is now attached to.
     */
    @SuppressWarnings("WeakerAccess")
    protected void onEngineSet(@NonNull Engine engine) {
        if (engine != this.engine) {
            // Set the new engine
            this.engine = engine;
            // Mark the context dirty
            needRestoreForNewContext = true;
        }
    }

    /**
     * Called when the {@link Engine} has been cleared from this scene. Subclasses should be sure to call
     * {@code super.onRendererSet(renderer)} to ensure proper behavior.
     */
    @SuppressWarnings("WeakerAccess")
    protected void onEngineCleared() {
    }

    /**
     * Executes all queued {@link FrameTask}s.
     */
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
