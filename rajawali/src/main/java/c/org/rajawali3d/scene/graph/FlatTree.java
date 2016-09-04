package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.bounds.AABB;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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

    protected void checkAndAdjustMinBounds(@NonNull AABB child) {
        // Pick the lesser value of each component between our bounds and the added bounds.
        final Vector3 addMin = child.getMinBound();
        minBound.setAll(Math.min(minBound.x, addMin.x),
                        Math.min(minBound.y, addMin.y),
                        Math.min(minBound.z, addMin.z));
    }

    protected void checkAndAdjustMaxBounds(@NonNull AABB child) {
        // Pick the larger value of each component between our bounds and the added bounds.
        final Vector3 addMax = child.getMaxBound();
        maxBound.setAll(Math.max(maxBound.x, addMax.x),
                        Math.max(maxBound.y, addMax.y),
                        Math.max(maxBound.z, addMax.z));
    }

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

    @Override
    public void recalculateBoundsForAdd(@NonNull AABB added) {
        // Have the added node determine its bounds
        added.recalculateBounds(true);
        checkAndAdjustMinBounds(added);
        checkAndAdjustMaxBounds(added);
    }

    @Override
    public boolean add(SceneNode node) {
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

    @Override
    public void clear() {
        children.clear();
        // If we have no children, our bounds are 0
        minBound.setAll(0d, 0d, 0d);
        maxBound.setAll(0d, 0d, 0d);
    }

    @Override
    public boolean contains(@Nullable Object object) {
        return children.contains(object);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return children.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<SceneNode> iterator() {
        return children.iterator();
    }

    @Override
    public boolean remove(@Nullable Object object) {
        final boolean retval = children.remove(object);
        if (object != null && retval) {
            // Check if we can skip bounds recalculation
            final SceneNode child = (SceneNode) object;
            final Vector3 removedMin = child.getMinBound();
            if (minBound.x < removedMin.y && minBound.y < removedMin.y && minBound.z < removedMin.z) {
                // The parent min bounds exceed the removed min bounds for other reasons
                final Vector3 removedMax = child.getMaxBound();
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
            // At least one component of the min/max bounds might be determined by the removed object, do a full
            // bounds check, but skip recursively calling children.
            recalculateBounds(false);
        }
        return retval;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        boolean retval = false;
        for (Object object : collection) {
            retval |= remove(object);
        }
        return retval;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        clear();
        boolean retval = false;
        for (Object object : collection) {
            retval |= add((SceneNode) object);
        }
        return retval;
    }

    @Override
    public int size() {
        return children.size();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return children.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        return children.toArray(array);
    }
}
