package c.org.rajawali3d.control;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresRenderTask;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import org.rajawali3d.animation.Animation;

import java.util.Collection;

/**
 * API
 * Client interface provided by both {@link Scene}s and {@link SceneView}s
 *
 * @author Randy Picolet
 */
public interface RenderDelegate {

    /**
     * Indicates whether this {@link Scene} has been added to - and not yet removed from - the
     * {@link RenderControl}, and so is eligible to receive delegated render events.
     *
     * @return {@code true} if attached to the {@link RenderControl}, and receives render events
     */
    boolean isAttachedToRenderControl();

    /**
     * Sets whether this RenderDelegate responds to events delegated from the {@link RenderControl}
     *
     * @param enabled
     */
    @RequiresRenderTask
    void setEnabled(boolean enabled);

    /**
     * Indicates whether this RenderDelegate responds to events delegated from the {@link RenderControl}
     *
     * @return
     */
    boolean isEnabled();

    /**
     * Adds a client {@link FrameCallback} for propagation from this RenderDelegate. Duplicates are not filtered.
     *
     * @param callback {@link FrameCallback} to be added.
     */
    @RequiresRenderTask
    void addFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes (the first occurrence of) a client {@link FrameCallback} from propagation by this
     * RenderDelegate. If the callback has bot been added, nothing will happen.
     *
     * @param callback {@link FrameCallback} to be removed.
     */
    @RequiresRenderTask
    void removeFrameCallback(@NonNull FrameCallback callback);

    /**
     * Removes all client {@link FrameCallback}s from propagation by this RenderDelegate.
     */
    @RequiresRenderTask
    void clearFrameCallbacks();

    /**
     * Adds an animation to be updated each frame by this RenderDelegate. Duplicates are not filtered.
     *
     * @param animation {@link Animation} to be added.
     */
    @RequiresRenderTask
    void addAnimation(@NonNull Animation animation);

    /**
     * Removes (the first occurrence of) an animation from frame updates by this RenderDelegate. If the a
     * nimation has not been added, nothing will happen.
     *
     * @param animation {@link Animation} to be removed.
     */
    @RequiresRenderTask
    void removeAnimation(@NonNull Animation animation);

    /**
     * Replaces an {@link Animation} previously added to this RenderDelegate with a new one.
     *
     * @param oldAnim {@link Animation} the old animation.
     * @param newAnim {@link Animation} the new animation.
     */
    @RequiresRenderTask
    void replaceAnimation(@NonNull final Animation oldAnim, @NonNull final Animation newAnim);

    /**
     * Adds a {@link Collection} of {@link Animation} objects to this RenderDelegate.
     * Duplicates are not filtered.
     *
     * @param animations {@link Collection} containing the {@link Animation} objects to be added.
     */
    @RequiresRenderTask
    void addAnimations(@NonNull final Collection<Animation> animations);

    /**
     * Removes all {@link Animation} objects from frame updates by this RenderDelegate.
     */
    @RequiresRenderTask
    void clearAnimations();
}
