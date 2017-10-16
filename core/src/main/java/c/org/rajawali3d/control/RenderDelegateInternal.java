package c.org.rajawali3d.control;

import android.support.annotation.FloatRange;
import c.org.rajawali3d.annotations.RenderThread;

/**
 * Internal interface shared by {@link SceneInternal} and {@link SceneViewInternal}
 *
 * @author Randy Picolet
 */

interface RenderDelegateInternal {

    /**
     * Called when this {@link RenderDelegateInternal} is added to the {@link RenderControl}. This is an opportunity
     * for the implementation to prepare for the next frame by allocating and initializing the needed resources.
     *
     * NOTE: if a large amount of work (e.g. loading and parsing large model files) is entailed, consider doing
     * so asynchronously on a different thread to avoid noticeable delay of the next frame
     *
     * @param renderControlInternal a handle to the current {@link (RenderControlInternal}
     */
    @RenderThread
    void onAddToRenderControl(RenderControlInternal renderControlInternal);

    /**
     * Called when this {@link RenderDelegateInternal} is removed from the {@link RenderControl}; the implementation
     * should deallocate all render context-specific resources (such as buffers) previously allocated for this
     * {@link RenderDelegateInternal}.
     */
    @RenderThread
    void onRemoveFromRenderControl();

    /**
     * Hook method called from {@link BaseRenderControl#onRenderFrame()} prior to actual rendering
     *
     * @param deltaTime
     */
    @RenderThread
    void onFrameStart(@FloatRange(from = 0.0) final double deltaTime);

    /**
     * Hook method called from {@link BaseRenderControl#onRenderFrame()} after rendering is completed
     *
     * @param deltaTime
     */
    @RenderThread
    void onFrameEnd(@FloatRange(from = 0.0) final double deltaTime);

}
