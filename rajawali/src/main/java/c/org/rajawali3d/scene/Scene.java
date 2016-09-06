package c.org.rajawali3d.scene;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
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
public class Scene {

    @NonNull
    private SceneGraph sceneGraph;

    @Nullable Lock currentlyHeldWriteLock;
    @Nullable Lock currentlyHeldReadLock;

    public Scene() {
        sceneGraph = new FlatTree();
    }

    public Scene(@NonNull SceneGraph graph) {
        sceneGraph = graph;
    }

    @GLThread
    public void render() throws InterruptedException {
        currentlyHeldReadLock = sceneGraph.acquireReadLock();
        try {
            internalRender();
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

    @RequiresReadLock
    @GLThread
    protected void internalRender() {

        // Determine which objects we will be rendering

        // Update the model matrix of all these objects
    }
}
