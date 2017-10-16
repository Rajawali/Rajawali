package c.org.rajawali3d.control;

import c.org.rajawali3d.annotations.RenderThread;

/**
 * Simple implementation of {@link FrameCallback} in which all frame methods are non-op and no methods are
 * registered. This allows user code an easy extension if only one callback method is desired.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class SimpleFrameCallback implements FrameCallback {

    @RenderThread
    @Override
    public void onFrameStart(double deltaTime) {

    }

    @RenderThread
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
