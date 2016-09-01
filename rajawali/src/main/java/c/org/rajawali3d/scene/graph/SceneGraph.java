package c.org.rajawali3d.scene.graph;

import java.util.Collection;

/**
 * Interface defining methods common to all {@link SceneGraph} implementations. Implementations are expected to be
 * thread safe and protected via a Reentrant Read-Write system.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface SceneGraph extends NodeParent, Collection<SceneNode> {

    enum Type {
        FLAT, QUADTREE, OCTREE
    }
}
