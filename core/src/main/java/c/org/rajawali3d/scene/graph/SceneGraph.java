package c.org.rajawali3d.scene.graph;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderTask;
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

    @RenderTask
    boolean add(@NonNull SceneNode node);

    @RenderTask
    boolean addAll(@NonNull Collection<? extends SceneNode> collection);

    @RenderTask
    void clear();

    @RenderTask
    boolean contains(@NonNull SceneNode node);

    @RenderTask
    boolean containsAll(@NonNull Collection<? extends SceneNode> collection);

    @RenderTask
    boolean isEmpty();

    @RenderTask
    boolean remove(@NonNull SceneNode node);

    @RenderTask
    boolean removeAll(@NonNull Collection<? extends SceneNode> collection);

    @RenderTask
    boolean retainAll(@NonNull Collection<? extends SceneNode> collection);

    @RenderTask
    int size();
}
