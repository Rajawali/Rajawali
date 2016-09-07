package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import org.rajawali3d.math.Matrix4;

import java.util.concurrent.locks.Lock;

/**
 * Interface which must be implemented by any container object in a scene such as {@link SceneNode} or
 * {@link SceneGraph}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface NodeParent {

    /**
     * Acquires a write lock for the scene graph. This method will block until a lock is acquired. Code using this
     * block must be properly protected with a {@code try-finally} block to ensure that the lock is released even in
     * the event of exceptions. Client code should avoid using this method or risk thread safety problems.
     *
     * @return The {@link Lock} which was acquired so that utilizing code may later release it.
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    @Nullable Lock acquireWriteLock() throws InterruptedException;

    /**
     * Acquires a read lock for the scene graph. This method will block until a lock is acquired. Code using this
     * block must be properly protected with a {@code try-finally} block to ensure that the lock is released even in
     * the event of exceptions. Client code should avoid using this method or risk thread safety problems.
     *
     * @return The {@link Lock} which was acquired so that utilizing code may later release it.
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    @Nullable Lock acquireReadLock() throws InterruptedException;

    /**
     * Initiates a restructure of the scene graph to accommodate any changes made due to transformations. It is
     * assumed that a write lock on the scene graph is held at this point acquired via the
     * {@link #acquireWriteLock()} method. Client code should avoid using this method or risk thread safety problems.
     */
    @RequiresWriteLock void updateGraph();

    /**
     * Traverses the scene graph and sets the provided {@link Matrix4} to be the combination of all transformations
     * resulting in the world space position of this parent node.
     *
     * @param matrix {@link Matrix4} instance which should be set to the world space transformation. Assumed to be
     *                              set to identity.
     */
    @RequiresReadLock void setToModelMatrix(@NonNull Matrix4 matrix);
}
