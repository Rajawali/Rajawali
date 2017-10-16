package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import c.org.rajawali3d.sceneview.camera.Camera;
import c.org.rajawali3d.object.RenderableObject;

import java.util.Collection;
import java.util.List;

/**
 * {@link SceneGraph} implementation which stores children in a 3D Octree. This is useful when objects will be
 * loosely structured in a full 3 degree of freedom environment and their relationship to each other on all 3 axes
 * matters.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Octree extends BaseSceneGraph {

    @NonNull
    @Override
    protected Octree createChildNode() {
        return new Octree();
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

    @NonNull
    @Override
    public List<RenderableObject> visibleObjectIntersection(@NonNull Camera camera) {
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
    @Override
    public boolean isEmpty() {
        return false;
    }

    @RequiresWriteLock
    @Override
    public boolean remove(@NonNull SceneNode node) {
        return false;
    }

    @RequiresWriteLock
    @Override
    public boolean removeAll(@NonNull Collection<? extends SceneNode> collection) {
        return false;
    }

    @RequiresWriteLock
    @Override
    public boolean retainAll(@NonNull Collection<? extends SceneNode> collection) {
        return false;
    }

    @RequiresReadLock
    @Override
    public int size() {
        return 0;
    }
}
