package c.org.rajawali3d.core;

import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import c.org.rajawali3d.annotations.RenderThread;
import java.util.LinkedList;
import java.util.Queue;
import net.jcip.annotations.GuardedBy;

/**
 * Base class for all core components; enables common render thread tasking
 *
 * @author Randy Picolet
 */

abstract class BaseComponent implements CoreComponent {

    @GuardedBy("frameTaskQueue")
    private final Queue<RenderTask> frameTaskQueue;

    BaseComponent() {
        frameTaskQueue = new LinkedList<>();
    }

    //
    // Internal RenderFrame event handlers
    //

    /**
     * Hook method required by all {@link CoreComponent}s, called from {@link CoreControl#onRenderFrame()};
     * runs any tasks queued for this component
     *
     * @param deltaTime
     * @throws InterruptedException
     */
    @RenderThread
    @CallSuper
    protected void onFrameStart(@FloatRange(from = 0.0) final double deltaTime) throws InterruptedException {
        runQueuedTasks();
    }

    /**
     * Hook method required by all {@link CoreComponent}s, called from {@link CoreControl#onRenderFrame()};
     * no common implementation
     *
     * @param deltaTime
     * @throws InterruptedException
     */
    @RenderThread
    protected abstract void onFrameEnd(@FloatRange(from = 0.0) final double deltaTime) throws InterruptedException;

    //
    // Internal RenderTask methods
    //

    /**
     * Executes a {@link RenderTask} on the render thread. If the calling thread is the render thread, the task
     * will be run immediately, otherwise it will be queued to run at the next frame start.
     *
     * @param renderTask {@link RenderTask} to execute.
     * @return {@code true} If the task was successfully run or queued for execution.
     */
    /*
    public boolean executeRenderTask(RenderTask renderTask) {
        if (isRenderThread()) {
            renderTask.run();
            return true;
        } else {
            return queueRenderTask(renderTask);
        }
    }
    */

    // TODO Do we actually need separate renderTask queues for each core component?
    // TODO Maybe just use the render-thread message queue for all renderTasks to reduce frame window processing?

    /**
     * Queues a {@link RenderTask} to on the render thread at the next frame start, regardless of calling thread.
     *
     * @param renderTask {@link RenderTask} to run.
     * @return {@code true} If the task was successfully queued to run.
     */
    /*
    @RenderThread
    public boolean queueRenderTask(RenderTask renderTask) {
        if (isRenderThread()) {
            synchronized (frameTaskQueue) {
                return frameTaskQueue.offer(renderTask);
            }
        }
        return false;
    }
    */

    /**
     *  Runs all {@link RenderTask}s queued for this component
     */
    /*
    @RenderThread
    private final void runQueuedTasks() {
        synchronized (frameTaskQueue) {
            RenderTask task = null;
            while (!frameTaskQueue.isEmpty()) {
                task = frameTaskQueue.remove();
                task.run();
            }
        }
    }
    */

    /**
     * Clears all queued {@link RenderTask}s for this component
     */
    /*
    final void clearQueuedTasks() {
        synchronized (frameTaskQueue) {
            frameTaskQueue.clear();
        }
    }
    */
}
