package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;

import android.support.annotation.NonNull;

import org.rajawali3d.animation.Animation;

import java.util.Collection;

/**
 * Client interface extensions for {@link CoreComponent}s responsible for receipt and handling of frame events from
 * a {@link RenderControl} implementation; shared interface of {@link Scene} and {@link SceneView}.
 *
 * @author Randy Picolet
 */
public interface FrameDelegate extends CoreComponent {

    /**
     * Called when this {@link FrameDelegate} is added to the {@link RenderControl}. This is an opportunity for the
     * implementation to prepare for the next frame by allocating and initializing the needed resources.
     *
     * NOTE: if a large amount of work (e.g. loading and parsing large model files) is entailed, consider doing
     * so asynchronously on a different thread to avoid noticeable delay of the next frame.
     *
     * @param renderStatus a handle to the current {@link RenderStatus} for the {@link RenderControl}
     */
    @RenderThread
    void onAddToRenderControl(RenderStatus renderStatus);

    /**
     * Called when this {@link FrameDelegate} is removed from the {@link RenderControl}; the implementation should
     * deallocate all render context-specific resources (such as buffers) previously allocated for this {@link Scene}.
     */
    @RenderThread
    void onRemoveFromRenderControl();

    /**
     * Indicates whether this {@link FrameDelegate} has been added to - and not yet removed from - the
     * {@link RenderControl}, and so receives the delegated render events.
     *
     * @return {@code true} if attached to the {@link RenderControl}, and receives render events
     */
    boolean isAttachedToRenderControl();

    /**
     * Sets whether this {@link FrameDelegate} responds to events delegated from the {@link RenderControl}.
     *
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Indicates whether this {@link FrameDelegate} responds to events delegated from the {@link RenderControl}.
     *
     * @return
     */
    boolean isEnabled();

    /**
     * Adds a client {@link FrameCallback} for propagation from this {@link FrameDelegate} using a
     * {@link RenderTask}. Duplicates are not filtered.
     *
     * @param callback {@link FrameCallback} to be added.
     * @return boolean True if the add task was run or queued successfully.
     */
    boolean addFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes (the first occurrence of) a client {@link FrameCallback} from propagation by this
     * {@link FrameDelegate} using a {@link RenderTask}. If the callback has bot been added, nothing will happen.
     *
     * @param callback {@link FrameCallback} to be removed.
     * @return boolean True if the remove task was run or queued successfully.
     */
    boolean removeFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes all client {@link FrameCallback}s from propagation by this {@link FrameDelegate} using a
     * {@link RenderTask}.
     *
     * @return boolean True if the clear task was run or queued successfully.
     */
    boolean clearFrameCallbacks();

    /**
     * Adds an animation to be updated each frame by this {@link FrameDelegate} using a {@link RenderTask}.
     * Duplicates are not filtered.
     *
     * @param animation {@link Animation} to be added.
     * @return boolean True if the add task was run or queued successfully.
     */
    boolean addAnimation(@NonNull Animation animation);

    /**
     * Removes (the first occurrence of) an animation from frame updates by this {@link FrameDelegate} using a
     * {@link RenderTask}. If the animation has not been added, nothing will happen.
     *
     * @param animation {@link Animation} to be removed.
     * @return boolean True if the removal task was run or queued successfully.
     */
    boolean removeAnimation(@NonNull Animation animation);

    /**
     * Replaces an {@link Animation} previously added to this {@link FrameDelegate} with a new one using a
     * {@link RenderTask}.
     *
     * @param oldAnim {@link Animation} the old animation.
     * @param newAnim {@link Animation} the new animation.
     * @return boolean True if the replacement task was run or queued successfully.
     */
    boolean replaceAnimation(@NonNull final Animation oldAnim, @NonNull final Animation newAnim);

    /**
     * Adds a {@link Collection} of {@link Animation} objects to this {@link FrameDelegate} using a {@link RenderTask}.
     * Duplicates are not filtered.
     *
     * @param animations {@link Collection} containing the {@link Animation} objects to be added.
     * @return boolean True if the addition task was run or queued successfully.
     */
    boolean addAnimations(@NonNull final Collection<Animation> animations);

    /**
     * Removes all {@link Animation} objects from frame updates by this {@link FrameDelegate} using a
     * {@link RenderTask}.
     *
     * @return boolean True if the clear task was run or queued successfully.
     */
    boolean clearAnimations();
}
