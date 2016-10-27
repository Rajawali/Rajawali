package c.org.rajawali3d.scene;

import c.org.rajawali3d.annotations.GLThread;

/**
 * Simple implementation of {@link SceneFrameCallback} in which all frame methods are non-op and no methods are
 * registered. This allows user code an easy extension if only a single callback method is desired.
 */
public class SimpleSceneFrameCallback implements SceneFrameCallback {

    @GLThread
    @Override
    public void onFrameStart(double deltaTime) {

    }

    @GLThread
    @Override
    public void onFrameEnd(double deltaTime) {

    }

    @Override
    public boolean callFrameStart() {
        return false;
    }

    @Override
    public boolean callFrameEnd() {
        return false;
    }
}
