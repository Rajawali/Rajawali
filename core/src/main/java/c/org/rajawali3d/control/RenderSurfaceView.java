package c.org.rajawali3d.control;

/**
 * Internal interface to be supplied by a renderable SurfaceView and used by its associated {@link RenderControl}
 * implementation. The goal is to define a minimal interface that can reasonably be expected to be supportable by
 * current (GLES) and future (Vulkan?) graphics systems.
 *
 * @author Randy Picolet
 */
public interface RenderSurfaceView {

    /**
     *
     * @return
     */
    boolean isTransparent();

    /**
     * Sets whether frames are rendered only on request (and when the surface is first created), or continuously
     *
     * @param onRequest {@code boolean} true to render on request, false to render continuously
     */
    void setRenderFramesOnRequest(boolean onRequest);

    /**
     * Request rendering of the next complete frame for the SurfaceView.
     */
    void requestFrameRender();

    /**
     * Queue a Runnable task to the render thread
     *
     * @param runnable
     */
    void queueToRenderThread(Runnable runnable);

    /**
     * Queue a Runnable task to the main/UI thread
     *
     * @param runnable
     */
    void queueToMainThread(Runnable runnable);
}
