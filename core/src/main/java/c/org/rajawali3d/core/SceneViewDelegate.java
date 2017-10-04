package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.sceneview.SceneViewControl;

/**
 * @author Randy Picolet
 */

public interface SceneViewDelegate extends FrameDelegate {

    /**
     * Called when this SceneViewDelegate is added to the {@link RenderControl}. This is an opportunity for the
     * implementation to prepare for the next frame by allocating and initializing the needed resources.
     *
     * NOTE: if a large amount of work (e.g. loading and parsing large model files) is entailed, consider doing
     * so asynchronously on a different thread to avoid noticeable delay of the next frame
     *
     * @param sceneViewControl a handle to the current {@link SceneViewControl}
     */
    @RenderThread
    void onAddToSceneViewControl(SceneViewControl sceneViewControl);

    /**
     * Called when this {@link SceneViewDelegate} is removed from the {@link RenderControl}; the implementation should
     * deallocate all render context-specific resources (such as buffers) currently allocated for this
     * {@link SceneViewDelegate}.
     */
    @RenderThread
    void onRemoveFromSceneViewControl();

    /**
     * Indicates whether this {@link SceneViewDelegate} has been added to - and not yet removed from - the
     * {@link RenderControl}, and so receives the delegated render events.
     *
     * @return {@code true} if attached to the {@link SceneViewControl}, and receives render events
     */
    boolean isAttachedToSceneViewControl();
}
