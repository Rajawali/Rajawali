package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.AScene;
import c.org.rajawali3d.sceneview.BaseSceneView;
import org.rajawali3d.animation.Animation;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import net.jcip.annotations.GuardedBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract base class for {@link AScene} and {@link BaseSceneView}
 *
 * @author Randy Picolet
 */

public abstract class BaseFrameDelegate extends ACoreComponent implements FrameDelegate {

    @GuardedBy("frameStartCallbacks")
    private final List<FrameCallback> frameStartCallbacks;

    @GuardedBy("frameEndCallbacks")
    private final List<FrameCallback> frameEndCallbacks;

    @GuardedBy("animations")
    private final List<Animation> animations;

    protected RenderStatus renderStatus;

    private volatile boolean isEnabled;

    public BaseFrameDelegate() {
        super();
        frameStartCallbacks = Collections.synchronizedList(new ArrayList<FrameCallback>());
        frameEndCallbacks = Collections.synchronizedList(new ArrayList<FrameCallback>());
        animations = Collections.synchronizedList(new CopyOnWriteArrayList<Animation>());
    }

    //
    // FrameDelegate methods
    //

    @RenderThread
    @Override
    @CallSuper
    public void onAddToRenderControl(@NonNull RenderStatus renderStatus) {
        this.renderStatus = renderStatus;
        setRenderThread();
    }

    @RenderThread
    @Override
    @CallSuper
    public void onRemoveFromRenderControl() {
        renderStatus = null;
        unsetRenderThread();
        clearQueuedTasks();
        synchronized (frameStartCallbacks) {
            frameStartCallbacks.clear();
        }
        synchronized (frameEndCallbacks) {
            frameEndCallbacks.clear();
        }
        synchronized (animations) {
            animations.clear();
        }
    }

    @Override
    public boolean isAttachedToRenderControl() {
        return renderStatus != null;
    }

    @Override
    public final void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public final boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean addFrameCallback(@NonNull final FrameCallback callback) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (callback.callFrameStart()) {
                    frameStartCallbacks.add(callback);
                }
                if (callback.callFrameEnd()) {
                    frameEndCallbacks.add(callback);
                }
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean removeFrameCallback(@NonNull final FrameCallback callback) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (callback.callFrameStart()) {
                    frameStartCallbacks.remove(callback);
                }
                if (callback.callFrameEnd()) {
                    frameEndCallbacks.remove(callback);
                }
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean clearFrameCallbacks() {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                frameStartCallbacks.clear();
                frameEndCallbacks.clear();
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean addAnimation(final Animation anim) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                animations.add(anim);
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean removeAnimation(final Animation anim) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                animations.remove(anim);
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean replaceAnimation(final Animation oldAnim, final Animation newAnim) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                animations.set(animations.indexOf(oldAnim), newAnim);
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean addAnimations(final Collection<Animation> anims) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                animations.addAll(anims);
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean clearAnimations() {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                animations.clear();
            }
        };
        return executeRenderTask(task);
    }

    //
    // ACoreComponent method overrides
    //

    @RenderThread
    @Override
    @CallSuper
    public void onFrameStart(double deltaTime) throws InterruptedException {
        if (!isEnabled) {
            return;
        }
        // Run any queued tasks
        super.onFrameStart(deltaTime);
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
    public void onFrameEnd(double deltaTime) throws InterruptedException {
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
