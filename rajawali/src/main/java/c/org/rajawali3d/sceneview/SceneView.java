package c.org.rajawali3d.sceneview;

import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.core.FrameDelegate;
import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.scene.Scene;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * Client interface for views managed by the RenderControl. A SceneView defines a viewportRect within the bounds of
 * the SurfaceView, and implements the rendering of its Scene content each frame
 *
 * TODO seems like as good place as any for render pass configuration (remember that?...) methods, or at least those
 * parts that are implementation independent;
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */
public interface SceneView extends FrameDelegate {

    /**
     * Gets the Scene defining the content to be displayed by the SceneView
     *
     * @return the {@link Scene} instance presented in this view
     */
    @NonNull
    Scene getScene();

    /**
     * Sets the Camera for this SceneView via a RenderTask
     *
     * @param camera
     * @return
     */
    boolean setCamera(@NonNull Camera camera);

    /**
     * Gets the current Camera for this SceneView
     * @return
     */
    @NonNull Camera getCamera();

    /**
     * Sets the viewport rectangle of this {@link SceneView} relative to the render SurfaceView via a RenderTask
     *
     * @param rect
     */
    boolean setViewportRect(@NonNull Rect rect);

    /**
     * Gets the viewport rectangle relative to the render SurfaceView
     *
     * @return
     */
    @NonNull Rect getViewportRect();

    /**
     * Sets the viewport depth order of this {@link SceneView} relative to that of other {@link SceneView}s via a
     * RenderTask. This is applied using a simple back-to-front painter's algorithm; viewports with larger depth
     * order values are placed in front of/above those with smaller valuss. There is no culling; even if a viewport is
     * completely obscured behind another, it is still rendered as if fully visible. Viewports at the same depth order
     * are rendered in the order their {@link SceneView}s were added to the {@link RenderControl}.
     *
     * @param depthOrder non-negative {@code int} depth order. 0 is the bottom/back, and the default.
     */
    boolean setViewportDepthOrder(@IntRange(from = 0) int depthOrder);

    /**
     * Gets the viewport depth order of this {@link SceneView} relative to that of other {@link SceneView}s.
     *
     * @return
     */
    int getViewportDepthOrder();

    /**
     * Sets whether the viewport is visible at all; if not, RenderTasks, RenderFrameCallbacks, and animations will
     * still be processed but no actual rendering will occur and the viewport will be hidden from view.
     *
     * TODO what if this is the only viewport? What should be displayed? Default background?
     *
     * @param visible {@code true}
     */
    void setViewportVisible(boolean visible);

    /**
     * Checks if the viewport is currently visible
     *
     * @return
     */
    boolean isViewportVisible();

}
