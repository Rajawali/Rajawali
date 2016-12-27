package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;

import org.rajawali3d.util.RajLog;

/**
 * Simple {@link Runnable} wrapper used to execute arbitrary code on the render thread in a protected,
 * crash resistant manner.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class RenderTask implements Runnable {

    /**
     * Implementations will override this method. Exceptions emitted by this method will be caught and logged but
     * not propagated.
     *
     * @throws Exception Thrown by the implementation.
     */
    @RenderThread
    protected abstract void doTask() throws Exception;

    @RenderThread
    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {
            RajLog.e("Execution Failed: " + e.getMessage());
        }
    }
}
