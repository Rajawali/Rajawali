package c.org.rajawali3d.scene;

import c.org.rajawali3d.annotations.GLThread;

/**
 * Simple implementation of {@link SceneFrameCallback} in which all frame methods are non-op and no methods are
 * registered. This allows user code an easy extension if only a single callback method is desired.
 */
public class SimpleSceneFrameCallback implements SceneFrameCallback {

    @GLThread
    @Override
    public void onPreFrame(long sceneTime, double deltaTime) {

    }

    @GLThread
    @Override
    public void onPreDraw(long sceneTime, double deltaTime) {

    }

    @GLThread
    @Override
    public void onPostFrame(long sceneTime, double deltaTime) {

    }

    @Override
    public boolean callPreFrame() {
        return false;
    }

    @Override
    public boolean callPreDraw() {
        return false;
    }

    @Override
    public boolean callPostFrame() {
        return false;
    }
}
