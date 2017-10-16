package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import c.org.rajawali3d.sceneview.camera.Camera;
import c.org.rajawali3d.object.RenderableObject;
import org.rajawali3d.math.vector.Vector3.Axis;

import java.util.Collection;
import java.util.List;

/**
 * {@link SceneGraph} implementation which stores children in a 2D Quad-Tree. This is useful for situations where
 * objects either have a common position in 1 dimension or the value of a single dimension is irrelevant to scene
 * structure.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class QuadTree extends BaseSceneGraph {

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

    @RequiresWriteLock
    @Override
    public void recalculateBounds(boolean recursive) {

    }

    @RequiresWriteLock
    @Override
    public void recalculateBoundsForAdd(@NonNull SceneNode added) {

    }

    @Override
    public void updateGraph() {

    }

    @RequiresReadLock
    @NonNull
    @Override
    public List<NodeMember> intersection(@NonNull Camera camera) {
        return null;
    }

    @NonNull @Override public List<RenderableObject> visibleObjectIntersection(@NonNull Camera camera) {
        return null;
    }

    @RequiresWriteLock
    @Override
    public boolean add(@NonNull SceneNode node) {
        return false;
    }

    @RequiresWriteLock
    @Override
    public boolean addAll(@NonNull Collection<? extends SceneNode> collection) {
        return false;
    }

    @RequiresWriteLock
    @Override
    public void clear() {

    }

    @RequiresReadLock
    @Override
    public boolean contains(@NonNull SceneNode node) {
        return false;
    }

    @RequiresReadLock
    @Override
    public boolean containsAll(@NonNull Collection<? extends SceneNode> collection) {
        return false;
    }

    @RequiresReadLock
    @Override public boolean isEmpty() {
        return false;
    }

    @RequiresWriteLock
    @Override public boolean remove(@NonNull SceneNode node) {
        return false;
    }

    @RequiresWriteLock
    @Override public boolean removeAll(@NonNull Collection<? extends SceneNode> collection) {
        return false;
    }

    @RequiresWriteLock
    @Override public boolean retainAll(@NonNull Collection<? extends SceneNode> collection) {
        return false;
    }

    @RequiresReadLock
    @Override public int size() {
        return 0;
    }
}
