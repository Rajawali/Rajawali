package c.org.rajawali3d.control;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.logging.LoggingComponent;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.jcip.annotations.GuardedBy;

/**
 * Abstract base class for {@link Scene} and {@link SceneView}
 *
 * @author Randy Picolet
 */

public abstract class BaseRenderDelegate extends LoggingComponent implements RenderDelegate, RenderDelegateInternal {

    @NonNull
    private RenderControlInternal renderControl;

    @GuardedBy("frameStartCallbacks")
    private final List<FrameCallback> frameStartCallbacks;

    @GuardedBy("frameEndCallbacks")
    private final List<FrameCallback> frameEndCallbacks;

    private volatile boolean isInitialized;
    private volatile boolean needRestoreForNewContext;
    private volatile boolean isEnabled;

    public BaseRenderDelegate() {
        super();
        frameStartCallbacks = Collections.synchronizedList(new ArrayList<FrameCallback>());
        frameEndCallbacks = Collections.synchronizedList(new ArrayList<FrameCallback>());
    }

    //
    // RenderDelegate methods
    //

    @RenderThread
    @Override
    @CallSuper
    public void onAddToRenderControl(@NonNull RenderControlInternal renderControl) {
        this.renderControl = renderControl;
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
     * {@link RenderDelegate} has been added to the {@link RenderControl}.
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
     * Releases graphic-system resources; invoked when this RenderDelegate has been removed
     * from the {@link RenderControl}
     *
     * TODO clarify relationship between init/restore/destroy, add to/remove from RenderControl, and
     * acquiring/losing context; i.e. formal lifecycles!
     */
    @RenderThread
    protected abstract void destroy();

    @RenderThread
    @Override
    @CallSuper
    public void onRemoveFromRenderControl() {
        synchronized (frameStartCallbacks) {
            frameStartCallbacks.clear();
        }
        synchronized (frameEndCallbacks) {
            frameEndCallbacks.clear();
        }
        // Mark the context dirty
        needRestoreForNewContext = true;
        renderControl = null;
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
    public void addFrameCallback(@NonNull final FrameCallback callback) {
        if (callback.callFrameStart()) {
            frameStartCallbacks.add(callback);
        }
        if (callback.callFrameEnd()) {
            frameEndCallbacks.add(callback);
        }
    }

    @Override
    public void removeFrameCallback(@NonNull final FrameCallback callback) {
        if (callback.callFrameStart()) {
            frameStartCallbacks.remove(callback);
        }
        if (callback.callFrameEnd()) {
            frameEndCallbacks.remove(callback);
        }
    }

    @Override
    public void clearFrameCallbacks() {
        frameStartCallbacks.clear();
        frameEndCallbacks.clear();
        //return executeRenderTask(new ClearFrameCallbacksTask());
    }

    //
    // RenderDelegateInternal method overrides
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
