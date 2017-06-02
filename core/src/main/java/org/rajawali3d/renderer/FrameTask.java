package org.rajawali3d.renderer;

import c.org.rajawali3d.scene.Scene;
import org.rajawali3d.util.RajLog;

/**
 * Simple {@link Runnable} wrapper used for passing arbitrary code to a {@link Scene} for execution at the start of a
 * frame in a protected, crash resistant manner.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class FrameTask implements Runnable {

    /**
     * Implementations will override this method. This method is called automatically by the {@link Scene} at the
     * start of a frame. Exceptions emitted by this method will be caught and logged but not propigated.
     *
     * @throws Exception Thrown by the implementation.
     */
    protected abstract void doTask() throws Exception;

    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {
            RajLog.e("Execution Failed: " + e.getMessage());
        }
    }
}
