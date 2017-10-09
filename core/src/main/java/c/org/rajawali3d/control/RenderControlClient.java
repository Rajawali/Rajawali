package c.org.rajawali3d.control;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.surface.SurfaceSize;

/**
 * API for the external client to be notified of {@link RenderControl} events.
 *
 * @author Randy Picolet
 */
public interface RenderControlClient {

    /**
     * Notifies the client that the {@link RenderControl} is available, which means the render thread, surface, context,
     * and interface are all ready for use. This provides an opportunity for the client to populate the initial
     * {@link Scene}(s) and {@link SceneView}(s) TODO how to handle lost contexts?
     *
     * @param renderControl
     * @param surfaceSize the initial {@link SurfaceSize}
     */
    @RenderThread
    void onRenderControlAvailable(@NonNull RenderControl renderControl, @NonNull SurfaceSize surfaceSize);

    /**
     * Notifies the client that the render surface dimensions have been changed after their initial setting.
     * This provides an opportunity for the client to reconfigure SceneView viewports
     *
     * @param surfaceSize - a {@link SurfaceSize} instance with the newly defined dimensions
     */
    @RenderThread
    void onSurfaceSizeChanged(@NonNull SurfaceSize surfaceSize);

    /**
     *
     */
    @RenderThread
    void onRenderControlLost();
}
