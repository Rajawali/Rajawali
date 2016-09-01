package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.scene.graph.SceneNode.NodeMember;
import c.org.rajawali3d.transform.Transformable;
import c.org.rajawali3d.transform.Transformation;
import c.org.rajawali3d.transform.Transformer;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Container class for items which are added to a scene. Scene objects which need to be treated as a group can all be
 * added to a single {@link SceneNode}. Except where noted, this class is thread safe and protected via a Reentrant
 * Read-Write system.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class SceneNode implements NodeParent, NodeMember, Transformable {

    @NonNull
    private final Object parentLock = new Object();

    @NonNull
    private final Transformation transformation = new Transformation();

    @NonNull
    private final List<SceneNode> children = new ArrayList<>();

    @NonNull
    private final List<NodeMember> members = new ArrayList<>();

    @NonNull
    private final Vector3 maxBound = new Vector3();

    @NonNull
    private final Vector3 minBound = new Vector3();

    /**
     * This is volatile and guarded by {@link #parentLock} because multiple threads can touch it and we always need
     * to be sure of what the most up to date parent is.
     */
    @GuardedBy("parentLock")
    @Nullable
    protected volatile NodeParent parent;

    @Nullable
    protected Lock currentlyHeldWriteLock;

    @NonNull
    @Override
    public Vector3 getMaxBound() {
        return maxBound;
    }

    @NonNull
    @Override
    public Vector3 getMinBound() {
        return minBound;
    }

    @Override
    public void requestTransformations(@NonNull Transformer transformer) throws InterruptedException {
        // Acquire write lock
        currentlyHeldWriteLock = acquireWriteLock();
        try {
            // Let the transformer do what it wants
            transformer.transform(transformation);
            // Update the scene graph
            updateGraph();
        } finally {
            // Release write lock
            releaseWriteLock();
        }
    }

    @Nullable
    @Override
    public Lock acquireWriteLock() throws InterruptedException {
        // If this node has been added to a graph, we need to ask for a lock, otherwise we can continue.
        synchronized (parentLock) {
            if (parent != null) {
                //noinspection ConstantConditions
                return parent.acquireWriteLock();
            } else {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public Lock acquireReadLock() throws InterruptedException {
        return null;
    }

    @Override
    public void setParent(@Nullable NodeParent parent) throws InterruptedException {
        acquireWriteLock();
        try {
            synchronized (parentLock) {
                this.parent = parent;
            }
        } finally {
            releaseWriteLock();
        }
    }

    /**
     * Adds a child {@link SceneNode} to this {@link SceneNode}. If this node (and any parents) are not yet part of a
     * {@link SceneGraph} no locking will be necessary and the operation will complete immediately.
     *
     * @param node The child {@link SceneNode} to add.
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    public void addChildNode(@NonNull SceneNode node) throws InterruptedException {
        acquireWriteLock();
        try {
            children.add(node);
            node.setParent(this);
            updateGraph();
        } finally {
            releaseWriteLock();
        }
    }

    /**
     * Removes a child {@link SceneNode} from this {@link SceneNode}. If this node (and any parents) are not yet part
     * of a {@link SceneGraph} no locking will be necessary and the operation will complete immediately.
     *
     * @param node The child {@link SceneNode} to remove.
     * @return {@code true} if the data structure was modified by this operation.
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    public boolean removeChildNode(@NonNull SceneNode node) throws InterruptedException {
        acquireWriteLock();
        boolean removed;
        try {
            removed = children.remove(node);
            node.setParent(null);
            updateGraph();
        } finally {
            releaseWriteLock();
        }
        return removed;
    }

    /**
     * Adds a {@link NodeMember} to this {@link SceneNode}. If this node (and any parents) are not yet part of a
     * {@link SceneGraph} no locking will be necessary and the operation will complete immediately.
     *
     * @param member The {@link NodeMember} to add.
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    public void addNodeMember(@NonNull NodeMember member) throws InterruptedException {
        acquireWriteLock();
        try {
            members.add(member);
            member.setParent(this);
            updateGraph();
        } finally {
            releaseWriteLock();
        }
    }

    /**
     * Removes a member {@link NodeMember} from this {@link SceneNode}. If this node (and any parents) are not yet part
     * of a {@link SceneGraph} no locking will be necessary and the operation will complete immediately.
     *
     * @param member The {@link NodeMember} to remove.
     * @return {@code true} if the data structure was modified by this operation.
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    public boolean removeNodeMember(@NonNull NodeMember member) throws InterruptedException {
        acquireWriteLock();
        boolean removed;
        try {
            removed = members.remove(member);
            member.setParent(null);
            updateGraph();
        } finally {
            releaseWriteLock();
        }
        return removed;
    }

    /**
     *  Releases any write lock this node might be holding.
     */
    protected void releaseWriteLock() {
        // We do this from the lock directly because our parent may have become null and we need to be sure we
        // release the lock we hold.
        if (currentlyHeldWriteLock != null) {
            currentlyHeldWriteLock.unlock();
        }
    }

    /**
     * Initiates a restructure of the scene graph to accommodate any changes made due to transformations. It
     * is assumed that a write lock on the scene graph is held at this point acquired via the
     * {@link #acquireWriteLock()} method. Client code should avoid using this method or risk thread safety problems.
     */
    protected void updateGraph() {
        recalculateBounds();
        synchronized (parentLock) {
            if (parent != null) {
                // Instruct the graph to rebuild.
            }
        }
    }

    /**
     * Recalculates the bounds of this {@link SceneNode}, taking into account the bounds of any children.
     */
    protected void recalculateBounds() {

    }

    /**
     * Interface to be implemented by classes which will be attached to {@link SceneNode}s. These could be 3D render
     * objects, cameras, lights, etc.
     */
    public interface NodeMember extends AABB {

        /**
         * Sets the {@link NodeParent} of this {@link NodeMember}.
         *
         * @param parent {@link NodeParent} implementation. Can be null.
         * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
         */
        void setParent(@Nullable NodeParent parent) throws InterruptedException;
    }
}
