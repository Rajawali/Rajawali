package c.org.rajawali3d.scene.graph;

import static c.org.rajawali3d.intersection.Intersector.INSIDE;
import static c.org.rajawali3d.intersection.Intersector.INTERSECT;
import static c.org.rajawali3d.intersection.Intersector.OUTSIDE;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import c.org.rajawali3d.camera.Camera;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.math.vector.Vector3;
import c.org.rajawali3d.intersection.Intersector.Intersection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link SceneGraph} implementation which stores all children in a single flat structure.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public class FlatTree extends ASceneGraph {

    protected final ArrayList<SceneNode> children = new ArrayList<>();

    @NonNull
    @Override
    protected SceneGraph createChildNode() {
        // There is no such thing as a child node for a flat tree.
        throw new UnsupportedOperationException("FlatTree scene graphs do not have child nodes.");
    }

    @RequiresWriteLock
    @Override
    public void updateGraph() {
        // For a FlatTree, the only thing we need to worry about is updating our scene bounds and we can assume all
        // nodes currently have valid bounds.
        recalculateBounds(false);
    }

    @RequiresWriteLock
    @Override
    public void recalculateBounds(boolean recursive) {
        // Reset the bounds to 0 to ensure we get accurate results.
        minBound.setAll(0d, 0d, 0d);
        maxBound.setAll(0d, 0d, 0d);
        SceneNode child;
        // For each child, recalculate the bounds as if it was an addition one at a time.
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
    }

    @RequiresWriteLock
    @Override
    public void recalculateBoundsForAdd(@NonNull SceneNode added) {
        // Have the added node determine its bounds
        added.recalculateBounds(true);
        checkAndAdjustMinBounds(added);
        checkAndAdjustMaxBounds(added);
    }

    @RequiresReadLock
    @NonNull
    @Override
    public List<NodeMember> intersection(@NonNull Camera camera) {
        final LinkedList<NodeMember> list = new LinkedList<>();
        @Intersection int intersection;
        SceneNode child;
        for (int i = 0, j = children.size(); i < j; ++i) {
            child = children.get(i);
            Log.i("INTER", "Intersecting child with bounds: " + child.getMinBound() + "/" + child.getMaxBound()
                           + " with camera.");
            intersection = camera.intersectBounds(child);
            Log.i("INTER", "Result: " + intersection);
            switch (intersection) {
                case INSIDE:
                case INTERSECT:
                    list.add(child);
                    break;
                case OUTSIDE:
                default:
            }
        }
        return list;
    }

    @RequiresWriteLock
    @Override
    public boolean add(@NonNull SceneNode node) {
        try {
            final boolean retval = children.add(node);
            // Do a bounds recalculation with just this added node
            recalculateBoundsForAdd(node);
            return retval;
        } catch (Exception e) {
            // Required per the Collection contract
            throw new IllegalArgumentException(e);
        }
    }

    @RequiresWriteLock
    @Override
    public boolean addAll(@NonNull Collection<? extends SceneNode> nodes) {
        final boolean retval = children.addAll(nodes);
        // Do a bounds recalculation with all added nodes
        for (SceneNode node : nodes) {
            // Try-catch not necessary here because this method handles it
            recalculateBoundsForAdd(node);
        }
        return retval;
    }

    @RequiresWriteLock
    @Override
    public void clear() {
        children.clear();
        // If we have no children, our bounds are 0
        minBound.setAll(0d, 0d, 0d);
        maxBound.setAll(0d, 0d, 0d);
    }

    @RequiresReadLock
    @Override
    public boolean contains(@Nullable SceneNode node) {
        return children.contains(node);
    }

    @RequiresReadLock
    @Override
    public boolean containsAll(@NonNull Collection<? extends SceneNode> collection) {
        return children.containsAll(collection);
    }

    @RequiresReadLock
    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @RequiresWriteLock
    @Override
    public boolean remove(@NonNull SceneNode node) {
        final boolean retval = children.remove(node);
        if (retval) {
            // Check if we can skip bounds recalculation
            final Vector3 removedMin = node.getMinBound();
            if (minBound.x < removedMin.y && minBound.y < removedMin.y && minBound.z < removedMin.z) {
                // The parent min bounds exceed the removed min bounds for other reasons
                final Vector3 removedMax = node.getMaxBound();
                if (maxBound.x > removedMax.x) {
                    if (maxBound.y > removedMax.y) {
                        if (maxBound.z > removedMax.z) {
                            // The parent max bounds exceed the removed max bounds for other reasons
                            // We don't need to do a bounds recalculation.
                            return true;
                        }
                    }
                }
            }
            // At least one component of the min/max bounds might be determined by the removed node, do a full
            // bounds check, but skip recursively calling children.
            recalculateBounds(false);
        }
        return retval;
    }

    @RequiresWriteLock
    @Override
    public boolean removeAll(@NonNull Collection<? extends SceneNode> collection) {
        boolean retval = false;
        for (SceneNode node : collection) {
            retval |= remove(node);
        }
        return retval;
    }

    @RequiresWriteLock
    @Override
    public boolean retainAll(@NonNull Collection<? extends SceneNode> collection) {
        clear();
        boolean retval = false;
        for (SceneNode node : collection) {
            retval |= add(node);
        }
        return retval;
    }

    @RequiresReadLock
    @Override
    public int size() {
        return children.size();
    }
}
