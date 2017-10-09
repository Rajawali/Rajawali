package c.org.rajawali3d.control;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.scene.SceneControl;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.sceneview.Viewport;
import c.org.rajawali3d.sceneview.SceneViewControl;
import c.org.rajawali3d.surface.SurfaceView;

/**
 * API for external clients to manage the interaction between the {@link SurfaceView} rendering context and all
 * {@link Scene}s and {@link SceneView}s associated with it. This interface is available only when a viable
 * render environment (thread, surface, context, and api) is in place.
 *
 * Methods are provided for setting the frame rate, adding/removing Scenes and SceneViews to/from lists for
 * delegated frame operation, and for depth ordering of SceneView on-screen {@link Viewport}s.
 *
 * <p>
 * In each frame, {@link SceneView}s are rendered in list order resulting in a simple back-to-front painter's
 * algorithm for their on-screen {@link Viewport}s (index 0 is back-most). There is no culling; even if a viewport is
 * completely obscured behind another, it is still rendered as if fully visible.
 * </p>
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface RenderControl extends RenderStatus {

    // Concurrency and synchronization

    /**
     *
     * @param runnable
     */
    void queueToMainThread(Runnable runnable);

    /**
     *
     * @param runnable
     */
    void queueToRenderThread(Runnable runnable);

    /**
     *
     * @param renderTask
     */
    void queueRenderTask(@NonNull RenderTask renderTask);

    /**
     * Checks whether the calling thread is the render thread.
     *
     * @return {@code true} if the calling thread is the render thread.
     */
    boolean isRenderThread();

    //
    // Frame generation
    //

    /**
     *  Convenience flag for use of the display refresh rate as the render frame rate
     */
    double USE_DISPLAY_REFRESH_RATE = Double.NaN;

    /**
     * Convenience flag for use of continuous rendering as fast as possible, rather than a periodic frame rate
     */
    double USE_CONTINUOUS_RENDERING = 0d;

    /**
     * Sets the current target frameRate.
     *
     * @param frameRate {@code double} target frame frameRate; Double.NaN flags use of window refresh rate, 0 flags
     *                                continuous rendering as fast as possible, >0 sets target frames per second
     */
    void setFrameRate(double frameRate);

    //
    // Scenes population
    //

    /**
     * Adds a {@link Scene} to the list of Scene frame delegates, using a {@link RenderTask}. The {@link Scene}
     * will be notified via {@link Scene#onAddToSceneControl(SceneControl)}, and should take any needed actions.
     *
     * @param scene A {@link Scene} to add; duplicates are quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean addScene(@NonNull Scene scene);

    /**
     * Removes a {@link Scene} from the list of Scene frame delegates, using a {@link RenderTask}. The {@link Scene}
     * will be notified via {@link Scene#onRemoveFromSceneControl()}, and should take any needed actions.
     *
     * @param scene The {@link Scene} to remove; if not found, it is quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean removeScene(@NonNull Scene scene);

    //
    // SceneViews population/ordering
    //

    /**
     * Adds a {@link SceneView} to (the end of) the list of SceneView frame delegates using a {@link RenderTask}.
     * The {@link SceneView} will be notified via {@link SceneView#onAddToSceneViewControl(SceneViewControl)},
     * and should take any needed actions.
     * <p>
     * {@link SceneView}s are rendered in list order using a simple back-to-front painter's algorithm (index 0 is
     * backmost). The added {@link SceneView} will be rendered foremost.
     * </p>
     *
     * @param sceneView The {@link SceneView} to add; duplicates are quietly ignored.
     * @return {@code true} if the task was run or queued successfully; does not indicate task success.
     */
    boolean addSceneView(@NonNull SceneView sceneView);

    /**
     * Gets the depth order of this {@link SceneView}'s on-screen viewport relative to that of other {@link SceneView}s.
     * This is simply the index in the list of SceneView frame delegates, so 0 means back-most, and larger orders are
     * rendered in front of lower orders.
     *
     * @param sceneView
     * @return
     */
    @IntRange(from = 0)
    int getSceneViewDepthOrder(@NonNull SceneView sceneView);

    /**
     * Inserts a {@link SceneView} in the list of SceneView frame delegates at the specified depth order
     * using a {@link RenderTask}.
     * The {@link SceneView} will be notified via {@link SceneViewDelegate#onAddToSceneViewControl(SceneViewControl)},
     * and should take any needed actions.
     * <p>
     * {@link SceneView}s are rendered in list order using a simple back-to-front painter's algorithm (index 0 is
     * backmost). The added {@link SceneView} will be rendered foremost.
     * </p>
     *
     * @param sceneView The {@link SceneView} to add; duplicates are quietly ignored.
     * @return {@code true} if the task was run or queued successfully; does not indicate task success.
     */
    boolean insertSceneView(@NonNull SceneView sceneView, int depthOrder);

    /**
     * Removes a {@link SceneView} from the list of SceneView frame delegates using a {@link RenderTask}.
     * The {@link SceneView} will be notified via {@link SceneView#onRemoveFromSceneViewControl()}, and should take
     * any needed actions.
     *
     * @param sceneView The {@link SceneView} to remove; if not found, it is quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean removeSceneView(@NonNull SceneView sceneView);
}
