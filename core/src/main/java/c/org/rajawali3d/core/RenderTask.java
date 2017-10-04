package c.org.rajawali3d.core;

import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RenderThread;

/**
 * Enables transactional read/write access to the overall render control state. Clients should extend this
 * class to implement {@link RenderTask#doTask()} to perform the minimum set of create, read, update, and/or
 * delete operations necessary to ensure application consistency. Instances should then be passed directly to
 * {@link RenderControl#queueRenderTask(RenderTask)} for execution. The operations will be performed atomically
 * on the render thread, isolated from all other rendering events (frame draws, surface changes, loss of context,
 * other tasks, etc.).
 *s
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */
public abstract class RenderTask {

    /**
     *
     */
    interface RenderTaskCallback {
        /**
         *
         * @param renderTask
         */
        void onTaskComplete(RenderTask renderTask);
    }

    /**
     *
     */
    @Nullable
    protected final RenderTaskCallback callback;

    /**
     *
     */
    protected final boolean queueToMainThread;

    /**
     *
     * @param callback
     */
    public RenderTask(RenderTaskCallback callback) {
        this(callback, true);
    }

    /**
     *
     * @param callback
     * @param queueToMainThread
     */
    public RenderTask(RenderTaskCallback callback, boolean queueToMainThread) {
        this.callback = callback;
        this.queueToMainThread = queueToMainThread;
    }

    /**
     * Performs the actual operations of this {@link RenderTask}.
     *
     * Exceptions emitted by this method will be caught and logged by the {@link RenderControl}, but not propagated.
     *
     * @throws Exception Thrown by the implementation.
     */
    @RenderThread
    public abstract void doTask() throws Exception;

    /**
     * For use by {@link RenderControl} ({@link CoreControl}) only
     */
    @RenderThread
    final void notifyComplete(RenderControl renderControl) {
        if (callback != null) {
            if (queueToMainThread) {
                renderControl.queueToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onTaskComplete(RenderTask.this);
                    }
                });
            } else {
                // Thread sync is client responsibility
                callback.onTaskComplete(this);
            }
        }
    }
}
