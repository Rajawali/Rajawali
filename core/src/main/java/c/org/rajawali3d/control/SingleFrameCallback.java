package c.org.rajawali3d.control;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;

/**
 * Special {@link SimpleFrameCallback} implementation which will automatically remove itself after the frame
 * completes. If subclasses override  {@link #onFrameEnd(double)}, they should call
 * {@link super#onFrameEnd(double)} AFTER they do any  post frame work. Subclasses cannot override
 * {@link #callFrameEnd()} as it must return {@code true} for this class to function properly.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class SingleFrameCallback extends SimpleFrameCallback {

    @NonNull
    private final RenderDelegate frameDelegate;

    /**
     * Constructs a new {@link SingleFrameCallback} for the provided {@link RenderDelegate}.
     *
     * @param frameDelegate The {@link RenderDelegate} to automatically remove from.
     */
    public SingleFrameCallback(@NonNull RenderDelegate frameDelegate) {
        this.frameDelegate = frameDelegate;
    }

    @RenderThread
    @Override
    @CallSuper
    public void onFrameEnd(double deltaTime) {
        frameDelegate.removeFrameCallback(this);
    }

    @Override
    public final boolean callFrameEnd() {
        return true;
    }
}
