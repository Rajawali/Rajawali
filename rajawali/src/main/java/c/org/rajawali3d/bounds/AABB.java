package c.org.rajawali3d.bounds;

import android.support.annotation.NonNull;
import org.rajawali3d.math.vector.Vector3;

/**
 * Interface defining methods for an Axis Aligned Bounding Box (AABB).
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface AABB {

    /**
     * Retrieves the position of the +X/+Y/+Z coordinates of this box.
     *
     * @return {@link Vector3} the +X/+Y/+Z corner coordinates.
     */
    @NonNull
    Vector3 getMaxBound();

    /**
     * Retrieves the position of the -X/-Y/-Z coordinates of this box.
     *
     * @return {@link Vector3} the -X/-Y/-Z corner coordinates.
     */
    @NonNull
    Vector3 getMinBound();
}
