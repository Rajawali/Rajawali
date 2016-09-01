package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;

/**
 * A {@link SceneGraph} implementation which stores all children in a single flat structure.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class FlatTree extends ASceneGraph {

    @NonNull
    @Override
    protected SceneGraph createChildNode() {
        // There is no such thing as a child node for a flat tree.
        return this;
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
