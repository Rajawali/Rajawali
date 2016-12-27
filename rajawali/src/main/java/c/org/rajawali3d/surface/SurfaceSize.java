package c.org.rajawali3d.surface;

import android.view.Surface;

/**
 * Immutable holder for the current size dimensions (in pixels) of the {@link Surface}/{@link SurfaceView}
 *
 * @author Randy Picolet
 */

public final class SurfaceSize {

    /**
     * The {@link Surface}/{@link SurfaceView} width in pixels
     */
    public final int width;

    /**
     * The {@link Surface}/{@link SurfaceView} height in pixels
     */
    public final int height;

    /**
     * Creates a new holder; for use by the {@link SurfaceView} only
     *
     * @param width  - width in pixels
     * @param height - height in pixels
     */
    public SurfaceSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
