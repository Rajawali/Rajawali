package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.SceneControl;

/**
 * @author Randy Picolet
 */

public interface SceneDelegate extends FrameDelegate {

    /**
     * Called when this {@link SceneDelegate} is added to the {@link SceneControl}. This is an opportunity for the
     * implementation to prepare for the next frame by allocating and initializing the needed resources.
     *
     * NOTE: if a large amount of work (e.g. loading and parsing large model files) is entailed, consider doing
     * so asynchronously on a different thread to avoid noticeable delay of the next frame
     *
     * @param sceneControl a handle to the current {@link SceneControl}
     */
    @RenderThread
    void onAddToSceneControl(SceneControl sceneControl);

    /**
     * Called when this {@link SceneDelegate} is removed from the {@link RenderControl}; the implementation should
     * deallocate all render context-specific resources (such as buffers) previously allocated for this
     * {@link SceneDelegate}.
     */
    @RenderThread
    void onRemoveFromSceneControl();

    /**
     * Indicates whether this {@link SceneDelegate} has been added to - and not yet removed from - the
     * {@link RenderControl}, and so receives the delegated render events.
     *
     * @return {@code true} if attached to the {@link SceneControl}, and receives render events
     */
    boolean isAttachedToSceneControl();
}
