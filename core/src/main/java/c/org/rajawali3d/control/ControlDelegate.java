package c.org.rajawali3d.control;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderTask;
import org.rajawali3d.animation.Animation;

import java.util.Collection;

/**
 * Client interface extensions for {@link ControlComponent}s that are responsible for receipt and handling of
 * lifecycle and frame events from the {@link RenderControl}; common interface for {@link SceneDelegate} and
 * {@link SceneViewDelegate}
 *
 * @author Randy Picolet
 */

interface ControlDelegate extends ControlComponent {

    /**
     * Sets whether this ControlDelegate responds to events delegated from the {@link RenderControl}
     *
     * @param enabled
     */
    @RenderTask
    void setEnabled(boolean enabled);

    /**
     * Indicates whether this ControlDelegate responds to events delegated from the {@link RenderControl}
     *
     * @return
     */
    boolean isEnabled();

    /**
     * Adds a client {@link FrameCallback} for propagation from this ControlDelegate. Duplicates are not filtered.
     *
     * @param callback {@link FrameCallback} to be added.
     */
    @RenderTask
    void addFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes (the first occurrence of) a client {@link FrameCallback} from propagation by this
     * ControlDelegate. If the callback has bot been added, nothing will happen.
     *
     * @param callback {@link FrameCallback} to be removed.
     */
    @RenderTask
    void removeFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes all client {@link FrameCallback}s from propagation by this ControlDelegate.
     */
    @RenderTask
    void clearFrameCallbacks();

    /**
     * Adds an animation to be updated each frame by this ControlDelegate. Duplicates are not filtered.
     *
     * @param animation {@link Animation} to be added.
     */
    @RenderTask
    void addAnimation(@NonNull Animation animation);

    /**
     * Removes (the first occurrence of) an animation from frame updates by this ControlDelegate. If the a
     * nimation has not been added, nothing will happen.
     *
     * @param animation {@link Animation} to be removed.
     */
    @RenderTask
    void removeAnimation(@NonNull Animation animation);

    /**
     * Replaces an {@link Animation} previously added to this ControlDelegate with a new one.
     *
     * @param oldAnim {@link Animation} the old animation.
     * @param newAnim {@link Animation} the new animation.
     */
    @RenderTask
    void replaceAnimation(@NonNull final Animation oldAnim, @NonNull final Animation newAnim);

    /**
     * Adds a {@link Collection} of {@link Animation} objects to this ControlDelegate.
     * Duplicates are not filtered.
     *
     * @param animations {@link Collection} containing the {@link Animation} objects to be added.
     */
    @RenderTask
    void addAnimations(@NonNull final Collection<Animation> animations);

    /**
     * Removes all {@link Animation} objects from frame updates by this ControlDelegate.
     */
    @RenderTask
    void clearAnimations();
}
