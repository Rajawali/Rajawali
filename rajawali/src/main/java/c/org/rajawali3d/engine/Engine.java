package c.org.rajawali3d.engine;

import android.support.annotation.NonNull;

/**
 * Interface to be implemented for managing the interaction between the surface-rendering context and all
 * {@link RenderModel}s and {@link RenderView}s associated with it. Rarely, if ever, should user code implement this
 * interface.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Engine {

    /**
     * Fetches the major version of the surface-rendering context.
     *
     * @return {@code int} containing the major version number.
     */
    int getRenderContextMajorVersion();

    /**
     * Fetches the minor version of the surface-rendering context.
     *
     * @return {@code int} containing the minor version number.
     */
    int getRenderContextMinorVersion();

    /**
     * Checks if the calling thread is the rendering thread this {@link Engine} is associated with.
     *
     * @return {@code true} if the calling thread is the rendering thread.
     */
    boolean isRenderThread();

    /**
     * Initiates engine frame processing.
     *
     * @return {@code true} if frames were stopped/not running, {@code false} if already started (no action taken)
     */
    boolean startFrames();

    /**
     * Stops engine frame processing.
     *
     * @return {@code true} if frames were started/running, {@code false} if already stopped (no action taken)
     */
    boolean stopFrames();


    /**
     * Indicates whether frame processing is active (started but not yet stopped)
     *
     * @return {@code true} if frame processing is active, {@code false} if frame processing is inactive
     */
    boolean areFramesRunning();

    /**
     * Gets the system time of the most recent successtul startFrames() call; is not reset when frames are stopped
     *
     * @return System time in nanoseconds of most recent successful startFrames() call
     */
    long getFramesStartTime();

    /**
     * Gets the time elapsed since the most recent successful startFrames() call; is not reset when frames are stopped
     *
     * @return nanoseconds elapsed since most recent successful startFrames() call
     */
    long getFramesElapsedTime();

    /**
     * Registers a SurfaceCallback with the Engine; duplicates will not be filtered
     *
     * @param callback
     */
    void addSurfaceCallback(@NonNull SurfaceCallback callback);

    /**
     * Unregisters a SurfaceCallback from the Engine; fails silently if the callback is not currently registered
     *
     * @param callback
     */
    void removeSurfaceCallback(@NonNull SurfaceCallback callback);

    /**
     * Retrieves the overall render surface width in pixels.
     *
     * @return {@code int} The render surface width in pixels.
     */
    int getSurfaceWidth();

    /**
     * Retrieves the overall render surface height in pixels.
     *
     * @return {@code int} The render surface height in pixels.
     */
    int getSurfaceHeight();

    /**
     * Registers a {@link RenderModel} with the {@link Engine}. The {@link RenderModel} will be notified of the new
     * context and is expected to take any actions it needs to. There is no guarantee this notification will occur on
     * the GL thread so GL context tasks must be queued by the {@link RenderModel}.
     *
     * @param renderModel The {@link RenderModel} object to register.
     */
    void addRenderModel(@NonNull RenderModel renderModel);

    /**
     * Unregisters a {@link RenderModel} from the {@link Engine}. The {@link RenderModel} will be notified of the
     * removal and is expected to take any actions it needs to. There is no guarantee this notification will occur on
     * the GL thread so GL context tasks must be queued by the {@link RenderModel}.
     *
     * @param renderModel The {@link RenderModel} object to unregister.
     */
    void removeRenderModel(@NonNull RenderModel renderModel);

    /**
     * Registers a {@link RenderView} with the {@link Engine}. The {@link RenderView} will be notified of the new
     * context and is expected to take any actions it needs to. There is no guarantee this notification will occur on
     * the GL thread so GL context tasks must be queued by the {@link RenderView}.
     *
     * @param renderView The {@link RenderView} object to register.
     */
    void addRenderView(@NonNull RenderView renderView);

    /**
     * Unregisters a {@link RenderView} from the {@link Engine}. The {@link RenderView} will be notified of the
     * removal and is expected to take any actions it needs to. There is no guarantee this notification will occur on
     * the GL thread so GL context tasks must be queued by the {@link RenderView}.
     *
     * @param renderView The {@link RenderView} object to unregister.
     */
    void removeRenderView(@NonNull RenderView renderView);

}
