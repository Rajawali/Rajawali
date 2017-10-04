package c.org.rajawali3d.scene;

import c.org.rajawali3d.core.SceneDelegate;
import c.org.rajawali3d.scene.graph.SceneGraph;
import c.org.rajawali3d.sceneview.SceneView;

import android.support.annotation.NonNull;

import java.util.concurrent.locks.Lock;

/**
 * Client interface for models that are to be presented by the RenderControl.
 *
 * Author: Randy Picolet
 */

public interface Scene extends SceneDelegate {

    /**
     * Acquires a read lock on the {@link Scene}. This is necessary when you need a self-consistent view of the
     * model from one thread while allowing (locked) modifications on another.
     *
     * Note the calling thread may be blocked if a write lock is already held on another thread.
     *
     * You must call {@link Lock#unlock()} on the returned lock instance when finished, or modifications using a
     * write lock will be blocked.
     *
     * TODO example code for try-finally idiom
     *
     * @return the read {@link Lock} instance
     * @throws InterruptedException Thrown if the calling thread is interrupted while waiting for lock acquisition.
     */
    //@NonNull
    //Lock acquireReadLock() throws InterruptedException;

    /**
     * TODO should this method be exposed to the client at all?  Do we want to wrap all the write locking in higher
     * order functions?
     *
     * Acquires a write lock on the {@link Scene}. This is necessary when you need to make a change and want to
     * prevent corruption of any model observers the rendering of any dependent {@link SceneView}s, or worse.
     *
     * Note this could block or delay frame updates if there are any dependent {@link SceneView}s; likewise,
     * the calling thread may be blocked while a dependent {@link SceneView} is being rendered or if you are holding
     * a lock on another thread.
     *
     * You must call {@link Lock#unlock()} on the returned lock instance when finished, or rendering may halt.
     *
     * TODO example code for try-finally idiom

     * @return the write {@link Lock} instance
     * @throws InterruptedException Thrown if the calling thread is interrupted while waiting for lock acquisition.
     */
    //@NonNull
    //Lock acquireWriteLock() throws InterruptedException;

    /**
     * Requests modifications to this {@link Scene} via the provided {@link SceneModifier}. The
     * modifications will all be performed under a single lock acquisition. This is useful when you need to make
     * muiltiple changes and want to avoid acquiring the write lock for each.
     *
     * Note this will block rendering if any dependent {@link SceneView}s are encountered during a frame; likewise,
     * the calling thread may be blocked while a dependent {@link SceneView} is being rendered.
     *
     * The lock is automatically released.
     *
     * TODO example code for try-finally idiom

     * @param sceneModifier {@link SceneModifier} instance which will be called when the lock has been acquired.*
     * @throws InterruptedException Thrown if the calling thread is interrupted while waiting for lock acquisition.
     */
    //void requestModifications(@NonNull SceneModifier sceneModifier) throws InterruptedException;

    SceneGraph getSceneGraph();
}
