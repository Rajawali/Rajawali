package c.org.rajawali3d.scene.graph;

import android.support.annotation.Nullable;
import c.org.rajawali3d.bounds.AABB;

/**
 * Interface to be implemented by classes which will be attached to {@link SceneNode}s. These could be 3D render
 * objects, cameras, lights, etc.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface NodeMember extends AABB {

    /**
     * Sets the {@link NodeParent} of this {@link NodeMember}.
     *
     * @param parent {@link NodeParent} implementation. Can be null.
     * @throws InterruptedException Thrown if the calling thread was interrupted while waiting for lock acquisition.
     */
    void setParent(@Nullable NodeParent parent) throws InterruptedException;
}
