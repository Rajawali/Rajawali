package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import c.org.rajawali3d.bounds.AABB;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract implementation of {@link SceneGraph} primarily responsible for thread safety management.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class ASceneGraph implements SceneGraph {

    @NonNull
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @NonNull
    protected final Vector3 minBound = new Vector3();

    @NonNull
    protected final Vector3 maxBound = new Vector3();

    @NonNull
    protected final Vector3 scratchVector3 = new Vector3();

    @NonNull
    protected final Matrix4 worldMatrix = new Matrix4();

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

    @RequiresReadLock
    @Override
    public void setToModelMatrix(@NonNull Matrix4 matrix) {
        // TODO: Apply world axis transformation here?
    }

    @NonNull
    @Override
    public Matrix4 getWorldModelMatrix() {
        // TODO: Apply world axis transformation here?
        return worldMatrix;
    }

    @RequiresReadLock
    @NonNull
    @Override
    public Vector3 getMaxBound() {
        return scratchVector3.setAll(maxBound);
    }

    @RequiresReadLock
    @NonNull
    @Override
    public Vector3 getMinBound() {
        return scratchVector3.setAll(minBound);
    }

    @RequiresWriteLock
    @Override
    public void recalculateBounds() {
        recalculateBounds(false);
    }

    /**
     * Compares the minimum axis aligned bounds of this {@link SceneGraph} with those of the child and adjusts these
     * bounds to be the lesser of the two on each axis.
     *
     * @param child {@link AABB} The child who should be compared against.
     */
    @RequiresWriteLock
    protected void checkAndAdjustMinBounds(@NonNull AABB child) {
        // Pick the lesser value of each component between our bounds and the added bounds.
        final Vector3 addMin = child.getMinBound();
        minBound.setAll(Math.min(minBound.x, addMin.x),
                        Math.min(minBound.y, addMin.y),
                        Math.min(minBound.z, addMin.z));
    }

    /**
     * Compares the maximum axis aligned bounds of this {@link SceneGraph} with those of the child and adjusts these
     * bounds to be the greater of the two on each axis.
     *
     * @param child {@link AABB} The child who should be compared against.
     */
    @RequiresWriteLock
    protected void checkAndAdjustMaxBounds(@NonNull AABB child) {
        // Pick the larger value of each component between our bounds and the added bounds.
        final Vector3 addMax = child.getMaxBound();
        maxBound.setAll(Math.max(maxBound.x, addMax.x),
                        Math.max(maxBound.y, addMax.y),
                        Math.max(maxBound.z, addMax.z));
    }
}
