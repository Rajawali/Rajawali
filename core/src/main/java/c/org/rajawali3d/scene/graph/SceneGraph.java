package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresRenderTask;
import c.org.rajawali3d.bounds.AABB;

import java.util.Collection;

/**
 * Interface defining methods common to all {@link SceneGraph} implementations. Implementations are expected to be
 * thread safe and protected via a Reentrant Read-Write system.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 *
 * //TODO: I doubt this needs to be its own interface.
 */
public interface SceneGraph extends NodeParent, AABB {

    @RequiresRenderTask
    boolean add(@NonNull SceneNode node);

    @RequiresRenderTask
    boolean addAll(@NonNull Collection<? extends SceneNode> collection);

    @RequiresRenderTask
    void clear();

    @RequiresRenderTask
    boolean contains(@NonNull SceneNode node);

    @RequiresRenderTask
    boolean containsAll(@NonNull Collection<? extends SceneNode> collection);

    @RequiresRenderTask
    boolean isEmpty();

    @RequiresRenderTask
    boolean remove(@NonNull SceneNode node);

    @RequiresRenderTask
    boolean removeAll(@NonNull Collection<? extends SceneNode> collection);

    @RequiresRenderTask
    boolean retainAll(@NonNull Collection<? extends SceneNode> collection);

    @RequiresRenderTask
    int size();
}
