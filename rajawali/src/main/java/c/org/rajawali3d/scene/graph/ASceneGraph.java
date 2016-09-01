package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract implementation of {@link SceneGraph} primarily responsible for thread safety management.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class ASceneGraph implements SceneGraph {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new child node for this graph node.
     *
     * @return A new concrete {@link SceneGraph} object to be used as a child node to this node.
     */
    @NonNull
    protected abstract SceneGraph createChildNode();

    @NonNull
    @Override
    public Lock acquireWriteLock() throws InterruptedException {
        final Lock writeLock = lock.writeLock();
        writeLock.lockInterruptibly();
        return writeLock;
    }

    @NonNull
    @Override
    public Lock acquireReadLock() throws InterruptedException {
        final Lock readLock = lock.readLock();
        readLock.lockInterruptibly();
        return readLock;
    }
}
