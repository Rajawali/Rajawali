package c.org.rajawali3d.bounds;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import org.rajawali3d.math.vector.Vector3;

/**
 * Interface defining methods for an Axis Aligned Bounding Box (AABB).
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface AABB {

    /**
     * Retrieves the position of the +X/+Y/+Z coordinates of this box. Note that in the interests of efficiency,
     * implementations are not required to protect the the internal state of the returned {@link Vector3}, meaning by
     * contract the user is expected to not modify the returned object in any way, to include normalization. The
     * returned value is in world space coordinates unless otherwise noted.
     *
     * @return {@link Vector3} the +X/+Y/+Z corner coordinates.
     */
    @RequiresReadLock @NonNull Vector3 getMaxBound();

    /**
     * Retrieves the position of the -X/-Y/-Z coordinates of this box. Note that in the interests of efficiency,
     * implementations are not required to protect the the internal state of the returned {@link Vector3}, meaning by
     * contract the user is expected to not modify the returned object in any way, to include normalization. The
     * returned value is in world space coordinates unless otherwise noted.
     *
     * @return {@link Vector3} the -X/-Y/-Z corner coordinates.
     */
    @RequiresReadLock @NonNull Vector3 getMinBound();

    /**
     * Causes a recalculation of the min/max coordinates in local coordinate space.
     */
    @RequiresWriteLock void recalculateBounds();

    class Comparator {

        /**
         * Compares the minimum axis aligned bounds of the child and adjusts the value in bounds to be the lesser of the
         * two on each axis.
         *
         * @param bound {@link Vector3} The bound to adjust.
         * @param childMin {@link Vector3} The child minimum bound to be compared against.
         */
        @RequiresWriteLock
        public static void checkAndAdjustMinBounds(@NonNull Vector3 bound, @NonNull Vector3 childMin) {
            // Pick the lesser value of each component between our bounds and the added bounds.
            bound.setAll(Math.min(bound.x, childMin.x),
                         Math.min(bound.y, childMin.y),
                         Math.min(bound.z, childMin.z));
        }

        /**
         * Compares the maximum axis aligned bounds of the child and adjusts the value in bounds to be the greater of
         * the two on each axis.
         *
         * @param bound {@link Vector3} The bound to adjust.
         * @param childMax {@link Vector3} The child maximum bound be compared against.
         */
        @RequiresWriteLock
        public static void checkAndAdjustMaxBounds(@NonNull Vector3 bound, @NonNull Vector3 childMax) {
            // Pick the larger value of each component between our bounds and the added bounds.
            bound.setAll(Math.max(bound.x, childMax.x),
                         Math.max(bound.y, childMax.y),
                         Math.max(bound.z, childMax.z));
        }
    }
}
