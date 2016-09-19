package c.org.rajawali3d.scene;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.GLThread;

/**
 * Special {@link SimpleSceneFrameCallback} implementation which will automatically remove itself after the frame
 * completes. If subclasses override  {@link #onPostFrame(long, double)}, they should call
 * {@link super#onPostFrame(long, double)} AFTER they do any  post frame work. Subclasses should not override
 * {@link #callPostFrame()} as it must return {@code true} for this class to function properly.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class SingleFrameCallback extends SimpleSceneFrameCallback {

    @NonNull
    protected final Scene scene;

    public SingleFrameCallback(@NonNull Scene scene) {
        this.scene = scene;
    }

    @GLThread
    @Override
    public void onPostFrame(long sceneTime, double deltaTime) {
        super.onPostFrame(sceneTime, deltaTime);
        scene.unregisterFrameCallback(this);
    }

    @Override
    public boolean callPostFrame() {
        return true;
    }
}
