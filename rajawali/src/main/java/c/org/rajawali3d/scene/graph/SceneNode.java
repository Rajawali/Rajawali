package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.transform.Transformable;
import c.org.rajawali3d.transform.Transformation;
import c.org.rajawali3d.transform.Transformer;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.math.Matrix4;
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
    private final Transformation transformation = new Transformation();

    @VisibleForTesting
    @NonNull
    final List<SceneNode> children = new ArrayList<>();

    @VisibleForTesting
    @NonNull
    final List<NodeMember> members = new ArrayList<>();

    @VisibleForTesting
    @NonNull
    final Vector3 maxBound = new Vector3();

    @VisibleForTesting
    @NonNull
    final Vector3 minBound = new Vector3();

    @Nullable
    protected NodeParent parent;

    @Nullable
    protected Lock currentlyHeldWriteLock;

    protected Transformation getTransformation() {
        return transformation;
    }

    @RequiresReadLock
    @NonNull
    @Override
    public Vector3 getMaxBound() {
        // TODO: This needs to take into account the local transformation
        return maxBound;
    }

    @RequiresReadLock
    @NonNull
    @Override
    public Vector3 getMinBound() {
        return minBound;
    }

    @RequiresWriteLock
    @Override
    public void recalculateBounds(boolean recursive) {
        // Reset the bounds to 0 to ensure we get accurate results.
        minBound.setAll(0d, 0d, 0d);
        maxBound.setAll(0d, 0d, 0d);
        NodeMember child;
        // For each child node, recalculate the bounds as if it was an addition one at a time.
        for (int i = 0, j = children.size(); i < j; ++i) {
            child = children.get(i);
            if (recursive) {
                // Recursively check all the children
                recalculateBoundsForAdd(child);
            } else {
                // Assume the child bounds are valid
                checkAndAdjustMinBounds(child);
                checkAndAdjustMaxBounds(child);
            }
        }
        // For each child member, recalculate the bounds as if it was an addition one at a time.
        for (int i = 0, j = members.size(); i < j; ++i) {
            child = members.get(i);
            if (recursive) {
                // Recursively check all the children
                recalculateBoundsForAdd(child);
            } else {
                // Assume the child bounds are valid
                checkAndAdjustMinBounds(child);
                checkAndAdjustMaxBounds(child);
            }
        }
    }

    @RequiresWriteLock
    @Override
    public void recalculateBoundsForAdd(@NonNull AABB added) {
        // Have the added node or child determine its bounds
        added.recalculateBounds(true);
        checkAndAdjustMinBounds(added);
        checkAndAdjustMaxBounds(added);
    }

    @Override
    public void requestTransformations(@NonNull Transformer transformer) throws InterruptedException {
        // Acquire write lock
        currentlyHeldWriteLock = acquireWriteLock();
        try {
            // Let the transformer do what it wants
            transformer.transform(getTransformation());
            // Update the scene graph to take account of any transformations we made.
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
        if (parent != null) {
            //noinspection ConstantConditions
            return parent.acquireWriteLock();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Lock acquireReadLock() throws InterruptedException {
        return null;
    }

    @Override
    public void setParent(@Nullable NodeParent parent) throws InterruptedException {
        currentlyHeldWriteLock = acquireWriteLock();
        try {
            this.parent = parent;
            // We don't update the graph here because it will happen during the add process.
        } finally {
            releaseWriteLock();
        }
    }

    @RequiresWriteLock
    @Override
    public void modelMatrixUpdated() {
        //TODO: Update bounds in world space
    }

    @RequiresWriteLock
    public void updateGraph() {
        final Matrix4 parentWorldModelMatrix = new Matrix4();
        if (parent != null) {
            parent.setToModelMatrix(parentWorldModelMatrix);
        }
        // Recursively recalculate our model matrices so that the graph update does not need to do any extra work
        recalculateModelMatrix(parentWorldModelMatrix);
        // Recursively recalculate our bounds so that the graph update does not need to do any extra work
        recalculateBounds(true);
        if (parent != null) {
            // Propagate the call up the chain of parents until we reach the graph
            parent.updateGraph();
        }
    }

    @RequiresReadLock
    @Override
    public void setToModelMatrix(@NonNull Matrix4 matrix) {
        matrix.leftMultiply(getTransformation().getLocalModelMatrix());
        if (parent != null) {
            parent.setToModelMatrix(matrix);
        }
    }

    /**
     * Adds a {@link NodeMember} to this {@link NodeParent}. If this parent (and any parents) are not yet part of a
     * {@link SceneGraph} no locking will be necessary and the operation will complete immediately.
     *
     * @param member The {@link NodeMember} to add.
     *
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
     * Removes a member {@link NodeMember} from this {@link NodeParent}. If this parent (and any parents) are not yet
     * part of a {@link SceneGraph} no locking will be necessary and the operation will complete immediately.
     *
     * @param member The {@link NodeMember} to remove.
     *
     * @return {@code true} if the data structure was modified by this operation.
     *
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
     * Adds a child {@link SceneNode} to this {@link SceneNode}. If this node (and any parents) are not yet part of a
     * {@link SceneGraph} no locking will be necessary and the operation will complete immediately.
     *
     * @param node The child {@link SceneNode} to add.
     *
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
     *
     * @return {@code true} if the data structure was modified by this operation.
     *
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    public boolean removeChildNode(@NonNull SceneNode node) throws InterruptedException {
        acquireWriteLock();
        boolean removed = false;
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
     * Traverses the scene graph and causes all {@link NodeParent}s which implement transformations to recalculate
     * their local model matrices, and also instruct their children to do the same. This is called after scene graph
     * updates or updates to an individual node to ensure that all model matrices in the tree are kept up to date
     * after modifications.
     *
     * @param parentWorldMatrix {@link Matrix4} The parent world space model matrix. If no parent exists, this should
     *                                         be an identity matrix.
     */
    @RequiresWriteLock
    protected void recalculateModelMatrix(@NonNull Matrix4 parentWorldMatrix) {
        final Transformation localTransformation = getTransformation();
        // Recalculate the local and world model matrices
        localTransformation.calculateLocalModelMatrix();
        localTransformation.calculateWorldModelMatrix(parentWorldMatrix);
        // Take any actions needed for matrix update
        modelMatrixUpdated();
        // For each child member, notify that the model matrix has been updated.
        for (int i = 0, j = members.size(); i < j; ++i) {
            members.get(i).modelMatrixUpdated();
        }
        // For each child node, recalculate the model matrix.
        for (int i = 0, j = children.size(); i < j; ++i) {
            children.get(i).recalculateModelMatrix(localTransformation.getWorldModelMatrix());
        }
    }

    /**
     * Releases any write lock this node might be holding.
     */
    protected void releaseWriteLock() {
        // We do this from the lock directly because our parent may have become null and we need to be sure we
        // release the lock we hold.
        if (currentlyHeldWriteLock != null) {
            try {
                currentlyHeldWriteLock.unlock();
            } catch (IllegalMonitorStateException e) {
                // We did not hold a valid lock here
                currentlyHeldWriteLock = null;
            }
        }
    }

    /**
     * Compares the minimum axis aligned bounds of this {@link SceneNode} with those of the child and adjusts these
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
     * Compares the maximum axis aligned bounds of this {@link SceneNode} with those of the child and adjusts these
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
