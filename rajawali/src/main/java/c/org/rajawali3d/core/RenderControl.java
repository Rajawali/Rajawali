package c.org.rajawali3d.core;

import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.surface.SurfaceView;

import android.support.annotation.NonNull;

/**
 * API for external clients to manage the interaction between the {@link SurfaceView} rendering context and all
 * {@link Scene}s and {@link SceneView}s associated with it. This interface is available only when a viable
 * render environment (thread, surface, context, and api) is in place.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface RenderControl extends RenderStatus {

    /**
     *  Convenience flag for use of the display refresh rate as the render frame rate
     */
    double USE_DISPLAY_REFRESH_RATE = -1.0d;

    /**
     * Convenience flag for use of continuous rendering as fast as possible, rather than a periodic frame rate
     */
    double USE_CONTINUOUS_RENDERING = 0d;

    /**
     * Sets the current target frameRate.
     *
     * @param frameRate {@code double} target frame frameRate; < 0 flags use of window refresh rate, 0 flags
     *                                continuous rendering as fast as possible, >0 sets target frames per second
     */
    void setFrameRate(double frameRate);

    /**
     * Adds a {@link Scene} to frame event delegation, using a {@link RenderTask}. The {@link Scene}
     * will be notified via {@link Scene#onAddToRenderControl(RenderStatus)}, and should take any needed actions.
     *
     * @param scene A {@link Scene} object to add; duplicates are quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean addScene(@NonNull Scene scene);

    /**
     * Removes a {@link Scene} from frame event delegation, using a {@link RenderTask}. The {@link Scene}
     * will be notified via {@link Scene#onRemoveFromRenderControl()}, and should take any needed actions.
     *
     * @param scene The {@link Scene} object to remove; if not found, it is quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean removeScene(@NonNull Scene scene);

    /**
     * Adds a {@link SceneView} to frame event delegation, using a {@link RenderTask}. The {@link SceneView}
     * will be notified via {@link SceneView#onAddToRenderControl(RenderStatus)}, and should take any needed actions.
     \     *
     * @param sceneView The {@link SceneView} object to add; duplicates are quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean addSceneView(@NonNull SceneView sceneView);

    /**
     * Adds a {@link SceneView} to frame event delegation, using a {@link RenderTask}. The {@link SceneView}
     * will be notified via {@link SceneView#onAddToRenderControl(RenderStatus)}, and should take any needed actions.
     *
     * @param sceneView The {@link SceneView} object to add; duplicates are quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean addSceneView(@NonNull SceneView sceneView, int depthOrder);

    /**
     * Removes a {@link SceneView} from frame event delegation, using a {@link RenderTask}. The {@link SceneView}
     * will be notified via {@link SceneView#onRemoveFromRenderControl()}, and should take any needed actions.
     *
     * @param sceneView The {@link SceneView} object to remove; if not found, it is quietly ignored by the task.
     * @return {@code true} if the task was run or queued successfully.
     */
    boolean removeSceneView(@NonNull SceneView sceneView);
}
