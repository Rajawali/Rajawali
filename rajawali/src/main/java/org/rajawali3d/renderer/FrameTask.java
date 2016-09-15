package org.rajawali3d.renderer;

import org.rajawali3d.util.RajLog;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class FrameTask implements Runnable {

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
