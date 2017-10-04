package c.org.rajawali3d.core;

import c.org.rajawali3d.surface.SurfaceSize;

import android.support.annotation.NonNull;

/**
 * API for external clients and {@link FrameDelegate}s for getting key property values of the render context and
 * its current control state.
 *
 * @author Randy Picolet
 */
public interface RenderStatus extends CoreComponent {

    /**
     * Gets the current RenderContext
     *
     * @return {@link RenderContext} - the current RenderContext enum instance
     */
    @NonNull
    RenderContext getCurrentRenderContext();

    /**
     * Gets the current overall render surface size in pixels.
     *
     * @return {@link SurfaceSize} instance containing current size dimensions
     */
    @NonNull
    SurfaceSize getSurfaceSize();

    /**
     * Gets the screen refresh rate for the default display in frames per second.
     *
     * @return {@code double} The display refresh rate.
     */
    double getDisplayRefreshRate();

    /**
     * Gets the current target frame rate in frames per second. Initial default is the display refresh rate.
     *
     * @return {@code double} The target frame rate.
     */
    double getFrameRate();

    /**
     * Checks whether frame processing is currently active (started but not yet stopped). This tracks the
     * (paused/resumed) state of the underlying render thread.
     *
     * @return {@code true} if frame processing is active, {@code false} if frame processing is inactive
     */
    boolean areFramesEnabled();

    /**
     * Gets the system time of the most recent start of frame processing (render thread resume);
     * 0 if frames are stopped (render thread paused)
     *
     * @return {@code long} System time in nanoseconds of most recent start of frame processing
     */
    long getFramesStartTime();

    /**
     * Gets the time elapsed since the most recent start of frame processing (render thread resume);
     * 0 if frame are stopped (render thread paused)
     *
     * @return {code long} nanoseconds elapsed since most recent start of frame processing
     */
    long getFramesElapsedTime();
}
