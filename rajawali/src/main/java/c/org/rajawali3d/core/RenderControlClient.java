package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.surface.SurfaceSize;

/**
 * API for the external client to be notified of {@link RenderControl} events
 *
 * @author Randy Picolet
 */
public interface RenderControlClient {

    /**
     * Notifies the client that the {@link RenderControl} is available, which means the render thread, surface, context,
     * and interface are all ready for use. This provides an opportunity for the client to populate the initial
     * RenderModels and RenderViews TODO how to handle lost contexts?
     *
     * @param renderControl
     * @param surfaceSize the initial {@link SurfaceSize}
     */
    @RenderThread
    void onRenderControlAvailable(RenderControl renderControl, SurfaceSize surfaceSize);

    /**
     * Notifies the client that the render surface dimensions have been changed after their initial setting.
     * This provides an opportunity for the client to reconfigure SceneView viewports
     *
     * @param surfaceSize - a {@link SurfaceSize} instance with the newly defined dimensions
     */
    @RenderThread
    void onSurfaceSizeChanged(SurfaceSize surfaceSize);
}
