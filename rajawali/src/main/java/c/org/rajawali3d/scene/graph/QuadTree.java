package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.bounds.AABB;
import org.rajawali3d.math.vector.Vector3.Axis;

import java.util.Collection;
import java.util.Iterator;

/**
 * {@link SceneGraph} implementation which stores children in a 2D Quad-Tree. This is useful for situations where
 * objects either have a common position in 1 dimension or the value of a single dimension is irrelevant to scene
 * structure.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class QuadTree extends ASceneGraph {

    /**
     * The {@link Axis} which will be ignored during sorting.
     */
    private final Axis ignoredAxis;

    /**
     * Constructs a new {@link QuadTree} which will ignore the Y-axis in its sorting.
     */
    public QuadTree() {
        this(Axis.Y);
    }

    /**
     * Constructs a new {@link QuadTree} which will ignore the specified {@link Axis} in its sorting.
     *
     * @param ignoredAxis {@link Axis} to be ignored.
     */
    public QuadTree(@NonNull Axis ignoredAxis) {
        this.ignoredAxis = ignoredAxis;
    }

    @NonNull
    @Override
    protected QuadTree createChildNode() {
        return new QuadTree(ignoredAxis);
    }

    @Override
    public void recalculateBounds(boolean recursive) {

    }

    @Override
    public void recalculateBoundsForAdd(@NonNull AABB added) {

    }

    @Override
    public boolean add(SceneNode object) {
        return false;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends SceneNode> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(@Nullable Object object) {
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @NonNull
    @Override
    public Iterator<SceneNode> iterator() {
        return null;
    }

    @Override
    public boolean remove(@Nullable Object object) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        return null;
    }
}
