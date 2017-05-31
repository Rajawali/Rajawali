package c.org.rajawali3d.surface;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.core.ARenderControl;
import c.org.rajawali3d.core.RenderContextType;
import c.org.rajawali3d.core.RenderSurfaceView;

/**
 * Interface defining the methods provided by {@link ARenderControl} to enable control by a
 * {@link RenderSurfaceView} implementation and/or a child of {@link ARenderControl} that implements the renderer
 * for that {@link RenderSurfaceView}.
 *
 * @author Randy Picolet
 */

public interface SurfaceRenderer {

    /**
     * Called when the render context is first fully created, or re-created after its loss.
     *
     * @param renderContextType
     * @param renderContextMajorVersion
     * @param renderContextMinorVersion
     */
    @RenderThread
    void onRenderContextAcquired(RenderContextType renderContextType,
                                 int renderContextMajorVersion, int renderContextMinorVersion);

    /**
     * Called when the size of the render surface is initiallly defined, and whenever it is changed after that.
     *
     * @param width
     * @param height
     */
    @RenderThread
    void onSurfaceSizeChanged(int width, int height);

    /**
     * Called when a new frame should be processed (whether on-request or continuously).
     */
    @RenderThread
    void onRenderFrame();

    /**
     * Called when the render thread is pausing
     */
    void onRenderThreadPause();

    /**
     * Called when the render thread is resuming
     */
    void onRenderThreadResume();

    /**
     * Called when the render context has been lost (and the surface has been destroyed), such as when the view is
     * detached from the window.
     */
    @RenderThread
    void onRenderContextLost();
}
