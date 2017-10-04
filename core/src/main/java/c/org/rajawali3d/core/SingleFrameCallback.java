package c.org.rajawali3d.core;

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
    private final FrameDelegate frameDelegate;

    /**
     * Constructs a new {@link SingleFrameCallback} for the provided {@link FrameDelegate}.
     *
     * @param frameDelegate The {@link FrameDelegate} to automatically remove from.
     */
    public SingleFrameCallback(@NonNull FrameDelegate frameDelegate) {
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
