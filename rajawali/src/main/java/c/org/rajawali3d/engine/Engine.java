package c.org.rajawali3d.engine;

import android.support.annotation.NonNull;

/**
 * Interface to be implemented for managing the interaction between the rendering context and all
 * {@link RenderModel}s and {@link RenderView}s associated with it. Rarely, if ever, should user
 * code implement this interface.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Engine {

    /**
     * Fetches the Open GL ES major version of the EGL surface.
     *
     * @return {@code int} containing the major version number.
     */
    int getGLMajorVersion();

    /**
     * Fetches the Open GL ES minor version of the EGL surface.
     *
     * @return {@code int} containing the minor version number.
     */
    int getGLMinorVersion();

    /**
     * Checks if the calling thread is the GL thread this {@link Engine} is associated with.
     *
     * @return {@code true} if the calling thread is the GL thread.
     */
    boolean isGLThread();

    /**
     * Initiates engine frame processing.
     * TODO What if alrteady started?
     */
    void startFrames();

    /**
     * @return time in nanoseconds of most recent startFrames()
     */
    long getFramesStartTime();

    /**
     * @return nanoseconds elapsed since most recent startFrames()
     */
    long getFramesElapsedTime();

    /**
     * Stops engine frame processing.
     *
     * @return {@code true} if frames were started/running,
     *          {@code false} if already stopped (no action taken)
     */
    boolean stopFrames();

    /**
     *
     */
    interface SurfaceCallback {

        /**
         * Notifies the client that the render surface dimensions have changed.
         *
         * @param width
         * @param height
         */
        void onSurfaceSizeChanged(int width, int height);
    }

    void addSurfaceCallback(SurfaceCallback callback);

    void removeSurfaceCallback(SurfaceCallback callback);

    /**
     * Retrieves the overall surface width in pixels.
     *
     * @return {@code int} The surface width in pixels.
     */
    int getSurfaceWidth();

    /**
     * Retrieves the overall surface height in pixels.
     *
     * @return {@code int} The surface height in pixels.
     */
    int getSurfaceHeight();

    /**
     * Registers a {@link RenderView} with the {@link Engine}. The {@link RenderView} will be
     * notified of the new context and is expected to take any actions it needs to. There is no
     * guarantee this notification will occur on the GL thread so GL context tasks must be queued
     * by the {@link RenderView}.
     *
     * @param renderModel The {@link RenderModel} object to register.
     */
    void addRenderModel(@NonNull RenderModel renderModel);

    /**
     * Unregisters a {@link RenderView} from the {@link Engine}. The {@link RenderView} will be
     * notified of the removal and is expected to take any actions it needs to. There is no
     * guarantee this notification will occur on the GL thread so GL context tasks must be queued
     * by the {@link RenderView}.
     *
     * @param renderModel The {@link RenderModel} object to unregister.
     */
    void removeRenderModel(@NonNull RenderModel renderModel);

    /**
     * Registers a {@link RenderView} with the {@link Engine}. The {@link RenderView} will be
     * notified of the new context and is expected to take any actions it needs to. There is no
     * guarantee this notification will occur on the GL thread so GL context tasks must be queued
     * by the {@link RenderView}.
     *
     * @param renderView The {@link RenderView} object to register.
     */
    void addRenderView(@NonNull RenderView renderView);

    /**
     * Unregisters a {@link RenderView} from the {@link Engine}. The {@link RenderView} will be
     * notified of the removal and is expected to take any actions it needs to. There is no
     * guarantee this notification will occur on the GL thread so GL context tasks must be queued
     * by the {@link RenderView}.
     *
     * @param renderView The {@link RenderView} object to unregister.
     */
    void removeRenderView(@NonNull RenderView renderView);

}
