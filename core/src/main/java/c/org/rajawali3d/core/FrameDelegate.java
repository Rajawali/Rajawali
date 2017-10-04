package c.org.rajawali3d.core;

import android.support.annotation.NonNull;

import org.rajawali3d.animation.Animation;

import java.util.Collection;
import org.rajawali3d.animation.Animation;

/**
 * Client interface extensions for {@link CoreComponent}s responsible for receipt and handling of lifecycle and
 * frame events from the {@link RenderControl}; common interface for {@link SceneDelegate} and {@link SceneViewDelegate}
 *
 * @author Randy Picolet
 */

interface FrameDelegate extends CoreComponent {

    /**
     * Sets whether this FrameDelegate responds to events delegated from the {@link RenderControl}
     *
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Indicates whether this FrameDelegate responds to events delegated from the {@link RenderControl}
     *
     * @return
     */
    boolean isEnabled();

    /**
     * Adds a client {@link FrameCallback} for propagation from this FrameDelegate using a
     * {@link RenderTask}. Duplicates are not filtered.
     *
     * @param callback {@link FrameCallback} to be added.
     * @return boolean True if the add task was run or queued successfully.
     */
    void addFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes (the first occurrence of) a client {@link FrameCallback} from propagation by this
     * FrameDelegate using a {@link RenderTask}. If the callback has bot been added, nothing will happen.
     *
     * @param callback {@link FrameCallback} to be removed.
     * @return boolean True if the remove task was run or queued successfully.
     */
    void removeFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes all client {@link FrameCallback}s from propagation by this FrameDelegate using a
     * {@link RenderTask}.
     *
     * @return boolean True if the clear task was run or queued successfully.
     */
    void clearFrameCallbacks();

    /**
     * Adds an animation to be updated each frame by this FrameDelegate, using a {@link RenderTask}.
     * Duplicates are not filtered.
     *
     * @param animation {@link Animation} to be added.
     * @return boolean True if the add task was run or queued successfully.
     */
    void addAnimation(@NonNull Animation animation);

    /**
     * Removes (the first occurrence of) an animation from frame updates by this FrameDelegate, using a
     * {@link RenderTask}. If the animation has not been added, nothing will happen.
     *
     * @param animation {@link Animation} to be removed.
     * @return boolean True if the removal task was run or queued successfully.
     */
    void removeAnimation(@NonNull Animation animation);

    /**
     * Replaces an {@link Animation} previously added to this FrameDelegate with a new one, using a
     * {@link RenderTask}.
     *
     * @param oldAnim {@link Animation} the old animation.
     * @param newAnim {@link Animation} the new animation.
     * @return boolean True if the replacement task was run or queued successfully.
     */
    void replaceAnimation(@NonNull final Animation oldAnim, @NonNull final Animation newAnim);

    /**
     * Adds a {@link Collection} of {@link Animation} objects to this FrameDelegate, using a {@link RenderTask}.
     * Duplicates are not filtered.
     *
     * @param animations {@link Collection} containing the {@link Animation} objects to be added.
     * @return boolean True if the addition task was run or queued successfully.
     */
    void addAnimations(@NonNull final Collection<Animation> animations);

    /**
     * Removes all {@link Animation} objects from frame updates by this FrameDelegate, using a {@link RenderTask}.
     *
     * @return boolean True if the clear task was run or queued successfully.
     */
    void clearAnimations();
}
