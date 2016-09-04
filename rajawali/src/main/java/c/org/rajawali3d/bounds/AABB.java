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
    @NonNull Vector3 getMaxBound();

    /**
     * Retrieves the position of the -X/-Y/-Z coordinates of this box.
     *
     * @return {@link Vector3} the -X/-Y/-Z corner coordinates.
     */
    @NonNull Vector3 getMinBound();

    /**
     * Causes a recalculation of the min/max coordinates.
     *
     * @param recursive If {@code boolean}, the calculation will be made recursively across all children. If {@code
     *                  false} the child bounds will be assumed to be unchanged.
     */
    void recalculateBounds(boolean recursive);

    /**
     * Causes a recalculation of the min/max coordinates, optimized for the case of a single expansion data point.
     *
     * @param added {@link AABB} implementation which was added.
     */
    void recalculateBoundsForAdd(@NonNull AABB added);
}
