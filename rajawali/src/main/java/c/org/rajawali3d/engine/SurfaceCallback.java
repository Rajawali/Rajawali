package c.org.rajawali3d.engine;

/**
 /**
 * Interface for engine clients to be notified of render surface events
 *
 * @author Randy Picolet
 */

public interface SurfaceCallback {

    /**
     * Notifies the client that the render surface dimensions have changed.
     *
     * @param width
     * @param height
     */
    void onSurfaceSizeChanged(int width, int height);
}
