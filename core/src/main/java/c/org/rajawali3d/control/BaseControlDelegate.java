package c.org.rajawali3d.control;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.BaseScene;
import c.org.rajawali3d.scene.SceneControl;
import c.org.rajawali3d.sceneview.BaseSceneView;
import c.org.rajawali3d.sceneview.SceneViewControl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.jcip.annotations.GuardedBy;
import org.rajawali3d.animation.Animation;



/**
 * Abstract base class for {@link BaseScene} and {@link BaseSceneView}
 *
 * @author Randy Picolet
 */

public abstract class BaseControlDelegate extends BaseControl implements ControlDelegate {

    @GuardedBy("frameStartCallbacks")
    private final List<FrameCallback> frameStartCallbacks;

    @GuardedBy("frameEndCallbacks")
    private final List<FrameCallback> frameEndCallbacks;

    @GuardedBy("animations")
    private final List<Animation> animations;

    protected RenderStatus renderStatus;

    private volatile boolean isInitialized;
    private volatile boolean needRestoreForNewContext;
    private volatile boolean isEnabled;

    public BaseControlDelegate() {
        super();
        // TODO Why is animations CopyOnWrite but the others aren't? Does syncList make this moot?
        frameStartCallbacks = Collections.synchronizedList(new ArrayList<FrameCallback>());
        frameEndCallbacks = Collections.synchronizedList(new ArrayList<FrameCallback>());
        animations = Collections.synchronizedList(new CopyOnWriteArrayList<Animation>());
    }

    //
    // ControlDelegate methods
    //

    /**
     * Call this from {@link SceneDelegate#onAddToSceneControl(SceneControl)} and
     * {@link SceneViewDelegate#onAddToSceneViewControl(SceneViewControl)} methods
     */
    @RenderThread
    protected void onAddToRenderControl(RenderStatus renderStatus) {
        this.renderStatus = renderStatus;

        if (!isInitialized) {
            initialize();
            isInitialized = true;
        }
        if (needRestoreForNewContext) {
            restore();
            needRestoreForNewContext = false;
        }
    }

    /**
     * Populates client-side elements and acquires server-side (graphic-system) resources needed for the next
     * frame render; invoked at the first opportunity after the new render thread/context is creaeted and this
     * {@link ControlDelegate} has been added to the {@link RenderControl}.
     * <p>
     * To be implemented by the client's child class, to initialize the model content when first added to the
     * {@link RenderControl}. Provides the opportunity to create, populate, and configure content elements, and
     * to allocate and configure any needed resources from the render context, needed for the next frame.
     * </p>
     */
    @RenderThread
    protected abstract void initialize();

    /**
     * Re-acquires server-side graphic-system resources corresponding to the current population of client-side
     * elements after loss/re-creation of the underlying render context; invoked when re-added to the RenderControl
     *
     * TODO is there any need for client implementation/extension?
     */
    @RenderThread
    protected abstract void restore();

    /**
     * Releases graphic-system resources; invoked when this ControlDelegate has been removed
     * from the {@link RenderControl}
     *
     * TODO clarify relationship between init/restore/destroy, add to/remove from RenderControl, and
     * acquiring/losing context; i.e. formal lifecycles!
     */
    @RenderThread
    protected abstract void destroy();

    @RenderThread
    @CallSuper
    public void onRemoveFromRenderControl() {
        renderStatus = null;
        synchronized (frameStartCallbacks) {
            frameStartCallbacks.clear();
        }
        synchronized (frameEndCallbacks) {
            frameEndCallbacks.clear();
        }
        synchronized (animations) {
            animations.clear();
        }
        // Mark the context dirty
        needRestoreForNewContext = true;
    }

    @Override
    public final void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public final boolean isEnabled() {
        return isEnabled;
    }

    /*
    private class AddFrameCallbackTask extends RenderTask {
        final FrameCallback callback;
        AddFrameCallbackTask(FrameCallback callback) {
            this.callback = callback;
        }
        @Override
        protected void doTask() {
            if (callback.callFrameStart()) {
                frameStartCallbacks.add(callback);
            }
            if (callback.callFrameEnd()) {
                frameEndCallbacks.add(callback);
            }
        }
    }
    */

    @Override
    public void addFrameCallback(@NonNull final FrameCallback callback) {
        if (callback.callFrameStart()) {
            frameStartCallbacks.add(callback);
        }
        if (callback.callFrameEnd()) {
            frameEndCallbacks.add(callback);
        }
        //return executeRenderTask(new AddFrameCallbackTask(callback));
    }

    /*
    private class RemoveFrameCallbackTask extends RenderTask {
        final FrameCallback callback;
        RemoveFrameCallbackTask(FrameCallback callback) {
            this.callback = callback;
        }
        @Override
        protected void doTask() {
            if (callback.callFrameStart()) {
                frameStartCallbacks.remove(callback);
            }
            if (callback.callFrameEnd()) {
                frameEndCallbacks.remove(callback);
            }
        }
    }
    */

    @Override
    public void removeFrameCallback(@NonNull final FrameCallback callback) {
        if (callback.callFrameStart()) {
            frameStartCallbacks.remove(callback);
        }
        if (callback.callFrameEnd()) {
            frameEndCallbacks.remove(callback);
        }
        //return executeRenderTask(new RemoveFrameCallbackTask(callback));
    }

    /*
    private class ClearFrameCallbacksTask extends RenderTask {
        @Override
        protected void doTask() {
            frameStartCallbacks.clear();
            frameEndCallbacks.clear();
        }
    }
    */

    @Override
    public void clearFrameCallbacks() {
        frameStartCallbacks.clear();
        frameEndCallbacks.clear();
        //return executeRenderTask(new ClearFrameCallbacksTask());
    }

    /*
    private class AddAnimationTask extends RenderTask {
        final Animation animation;
        AddAnimationTask(Animation animation) {
            this.animation = animation;
        }
        @Override
        protected void doTask() {
            animations.add(animation);
        }
    }
    */

    @Override
    public void addAnimation(final Animation animation) {
        animations.add(animation);
        //return executeRenderTask(new AddAnimationTask(animation));
    }

    /*
    private class RemoveAnimationTask extends RenderTask {
        final Animation animation;
        RemoveAnimationTask(Animation animation) {
            this.animation = animation;
        }
        @Override
        protected void doTask() {
            animations.remove(animation);
        }
    }
    */

    @Override
    public void removeAnimation(final Animation animation) {
        animations.remove(animation);
        //return executeRenderTask(new RemoveAnimationTask(animation));
    }

    /*
    private class ReplaceAnimationTask extends RenderTask {
        final Animation oldAnimation;
        final Animation newAnimation;
        ReplaceAnimationTask(Animation oldAnimation, Animation newAnimation) {
            this.oldAnimation = oldAnimation;
            this.newAnimation = newAnimation;
        }
        @Override
        protected void doTask() {
            animations.set(animations.indexOf(oldAnimation), newAnimation);
        }
    }
    */

    @Override
    public void replaceAnimation(final Animation oldAnimation, final Animation newAnimation) {
        animations.set(animations.indexOf(oldAnimation), newAnimation);
        //return executeRenderTask(new ReplaceAnimationTask(oldAnimation, newAnimation));
    }

    /*
    private class AddAnimationsTask extends RenderTask {
        final Collection<Animation> animations;
        AddAnimationsTask(Collection<Animation> animations) {
            this.animations = animations;
        }
        @Override
        protected void doTask() {
            BaseControlDelegate.this.animations.addAll(animations);
        }
    }
    */

    @Override
    public void addAnimations(final Collection<Animation> animations) {
        BaseControlDelegate.this.animations.addAll(animations);
        //return executeRenderTask(new AddAnimationsTask(animations));
    }

    /*
    private class ClearAnimationsTask extends RenderTask {
        @Override
        protected void doTask() {
            animations.clear();
        }
    }
    */

    @Override
    public void clearAnimations() {
        animations.clear();
        //return executeRenderTask(new ClearAnimationsTask());
    }

    //
    // BaseControl method overrides
    //

    @RenderThread
    @Override
    @CallSuper
    public void onFrameStart(double deltaTime) {
        if (!isEnabled) {
            return;
        }
        // Propagate to any client callbacks
        synchronized (frameStartCallbacks) {
            for (int i = 0, j = frameStartCallbacks.size(); i < j; i++) {
                frameStartCallbacks.get(i).onFrameStart(deltaTime);
            }
        }
        // Update any animations
        synchronized (animations) {
            for (int i = 0, j = animations.size(); i < j; ++i) {
                Animation anim = animations.get(i);
                if (anim.isPlaying())
                    anim.update(deltaTime);
            }
        }
    }

    @RenderThread
    @Override
    @CallSuper
    public void onFrameEnd(double deltaTime) {
        if (!isEnabled) {
            return;
        }
        // Propagate to any client callbacks
        synchronized (frameEndCallbacks) {
            for (int i = 0, j = frameEndCallbacks.size(); i < j; i++) {
                frameEndCallbacks.get(i).onFrameEnd(deltaTime);
            }
        }
    }
}
