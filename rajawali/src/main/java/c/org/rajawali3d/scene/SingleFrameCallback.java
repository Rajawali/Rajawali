package c.org.rajawali3d.scene;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.GLThread;

/**
 * Special {@link SimpleSceneFrameCallback} implementation which will automatically remove itself after the frame
 * completes. If subclasses override  {@link #onFrameEnd(double)}, they should call
 * {@link super#onFrameEnd(double)} AFTER they do any  post frame work. Subclasses should not override
 * {@link #callFrameEnd()} as it must return {@code true} for this class to function properly.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class SingleFrameCallback extends SimpleSceneFrameCallback {

    @NonNull
    protected final Scene scene;

    /**
     * Constructs a new {@link SingleFrameCallback} for the provided {@link Scene}.
     *
     * @param scene The {@link Scene} to automatically remove from.
     */
    public SingleFrameCallback(@NonNull Scene scene) {
        this.scene = scene;
    }

    @GLThread
    @Override
    public void onFrameEnd(double deltaTime) {
        super.onFrameEnd(deltaTime);
        scene.unregisterFrameCallback(this);
    }

    @Override
    public boolean callFrameEnd() {
        return true;
    }
}
