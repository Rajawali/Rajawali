package c.org.rajawali3d.control;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresRenderTask;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.sceneview.Viewport;
import c.org.rajawali3d.surface.SurfaceSize;
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
public interface RenderControl {

    //
    // Render context and surface
    //

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

    //
    // Concurrency and synchronization
    //

    /**
     * Checks whether the calling thread is the render thread.
     *
     * @return {@code true} if the calling thread is the render thread.
     */
    boolean isRenderThread();

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

    //
    // Frame generation
    //

    /**
     *  Flag value indicating use of the display refresh rate as the render frame rate
     */
    double USE_DISPLAY_REFRESH_RATE = Double.NaN;

    /**
     * Flag value indicating use of continuous rendering as fast as possible, rather than a periodic frame rate
     */
    double USE_CONTINUOUS_RENDERING = 0d;

    /**
     * Gets the screen refresh rate for the default display in frames per second.
     *
     * @return {@code double} The display refresh rate.
     */
    double getDisplayRefreshRate();

    /**
     * Sets the current target frameRate.
     *
     * @param frameRate {@code double} target frame frameRate; Double.NaN flags use of window refresh rate, 0 flags
     *                                continuous rendering as fast as possible, >0 sets target frames per second
     */
    void setFrameRate(double frameRate);

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
    boolean areFramesActive();

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

    //
    // Scenes population
    //

    /**
     * Adds a {@link Scene} to the list of Scene frame delegates. The {@link Scene} will be notified via
     * {@link Scene#initialize()}, and should take any needed actions.
     *
     * @param scene A {@link Scene} to add; duplicates are quietly ignored.
     */
    @RequiresRenderTask
    void addScene(@NonNull Scene scene);

    /**
     * Removes a {@link Scene} from the list of Scene frame delegates. The {@link Scene} will be notified via
     * {@link Scene#destroy()}, and should take any needed actions.
     *
     * @param scene The {@link Scene} to remove; if not found, it is quietly ignored.
     */
    @RequiresRenderTask
    void removeScene(@NonNull Scene scene);

    //
    // SceneViews population/ordering
    //

    /**
     * Adds a {@link SceneView} to (the end of) the list of SceneView frame delegates. The {@link SceneView} will be
     * notified via {@link SceneView#onAddToSceneViewControl(RenderControlInternal)}, and should take any needed actions.
     * <p>
     * {@link SceneView}s are rendered in list order using a simple back-to-front painter's algorithm (index 0 is
     * backmost). The added {@link SceneView} will be rendered foremost.
     * </p>
     *
     * @param sceneView The {@link SceneView} to add; duplicates are quietly ignored.
     */
    @RequiresRenderTask
    void addSceneView(@NonNull SceneView sceneView);

    /**
     * Gets the depth order of this {@link SceneView}'s on-screen viewport relative to that of other {@link SceneView}s.
     * This is simply the index in the list of SceneView frame delegates, so 0 means back-most, and larger orders are
     * rendered in front of lower orders.
     *
     * @param sceneView
     * @return
     */
    @RequiresRenderTask
    @IntRange(from = 0)
    int getSceneViewDepthOrder(@NonNull SceneView sceneView);

    /**
     * Inserts a {@link SceneView} in the list of SceneView frame delegates at the specified depth order.
     * The {@link SceneView} will be notified via {@link SceneView#initialize()} ,
     * and should take any needed actions.
     * <p>
     * {@link SceneView}s are rendered in list order using a simple back-to-front painter's algorithm (index 0 is
     * backmost). The added {@link SceneView} will be rendered foremost.
     * </p>
     *
     * @param sceneView The {@link SceneView} to add; duplicates are quietly ignored.
     */
    @RequiresRenderTask
    void insertSceneView(@NonNull SceneView sceneView, int depthOrder);

    /**
     * Removes a {@link SceneView} from the list of SceneView frame delegates. The {@link SceneView} will be notified
     * via {@link SceneView#destroy()}, and should take any needed actions.
     *
     * @param sceneView The {@link SceneView} to remove; if not found, it is quietly ignored.
     */
    @RequiresRenderTask
    void removeSceneView(@NonNull SceneView sceneView);
}
