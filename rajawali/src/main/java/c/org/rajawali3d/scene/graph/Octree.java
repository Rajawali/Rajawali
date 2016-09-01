package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;

/**
 * {@link SceneGraph} implementation which stores children in a 3D Octree. This is useful when objects will be
 * loosely structured in a full 3 degree of freedom environment and their relationship to each other on all 3 axes
 * matters.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Octree extends ASceneGraph {

    @NonNull
    @Override
    protected Octree createChildNode() {
        return new Octree();
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
