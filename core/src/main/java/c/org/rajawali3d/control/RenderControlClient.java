package c.org.rajawali3d.control;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.surface.SurfaceSize;

/**
 * Interface to be implemented by the external client for notification of {@link RenderControl} events.
 *
 * @author Randy Picolet
 */
public interface RenderControlClient {

    /**
     * Notifies the client that an {@link RenderControl} is available, which means the underlying render thread,
     * surface, context, and interface are all ready for use. This callback provides an opportunity to register any
     * initial {@link Scene}s and {@link SceneView}s needed for rendering.
     *
     * @param renderControl the {@link RenderControl} instance
     * @param surfaceSize the initial {@link SurfaceSize} instance
     */
    @RenderThread
    void onRenderControlAvailable(@NonNull RenderControl renderControl, @NonNull SurfaceSize surfaceSize);

    /**
     * Notifies the client that the render surface dimensions have been changed after their initial setting.
     * This provides an opportunity for the client to reconfigure SceneView Viewports
     *
     * @param surfaceSize - a {@link SurfaceSize} instance with the newly defined dimensions
     */
    @RenderThread
    void onSurfaceSizeChanged(@NonNull SurfaceSize surfaceSize);

    /**
     * Notifies the client that the {@link RenderControl} instance is about to go away, which means the underlying
     * render context and interface are no longer usable, and after this call the render thread and surface will not
     * be usable either. This callback provides an opportunity to clean up any resources associated with any
     * {@link Scene}s or {@link SceneView}s that may have been populated.
     */
    @RenderThread
    void onRenderControlUnavailable();
}
