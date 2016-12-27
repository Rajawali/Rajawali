package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;

import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;

import net.jcip.annotations.GuardedBy;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Base class for all core components; enables common render thread tasking
 *
 * @author Randy Picolet
 */

abstract class ACoreComponent implements CoreComponent {

    private Thread renderThread;

    @GuardedBy("frameTaskQueue")
    private final Queue<RenderTask> frameTaskQueue;

    ACoreComponent() {
        frameTaskQueue = new LinkedList<>();
    }

    //
    // CoreComponent implementation
    //

    @Override
    public final boolean isRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    // TODO add "engine-only/internal-only" annotations (type- and/or method-levels) for general use?
    // And/or a public/client API annotation? Maybe adopt a policy of using separate/dedicated interfaces for
    // client APIs where possible? Or some combination?

    //
    // Internal RenderThread methods
    //

    /**
     * Sets the render thread for this component; call at the first opportunity after thread creation
     */
    @RenderThread
    final void setRenderThread() {
        if (renderThread == null) {
            renderThread = Thread.currentThread();
        } else {
            // TODO log?
        }
    }

    /**
     * Clears the render thread for this component; call at the last opportunity before thread destruction
     */
    @RenderThread
    final void unsetRenderThread() {
        if (isRenderThreadSet()) {
            renderThread = null;
        } else {
            // TODO log?
        }
    }

    final boolean isRenderThreadSet() {
        return renderThread != null;
    }

    //
    // Internal RenderFrame event handlers
    //

    /**
     * Hook method required by all {@link CoreComponent}s, called from {@link ARenderControl#onRenderFrame()};
     * runs any tasks queued for this component
     *
     * @param deltaTime
     * @throws InterruptedException
     */
    @RenderThread
    @CallSuper
    public void onFrameStart(double deltaTime) throws InterruptedException {
        runQueuedTasks();
    }

    /**
     * Hook method required by all {@link CoreComponent}s, called from {@link ARenderControl#onRenderFrame()};
     * no common implementation
     *
     * @param deltaTime
     * @throws InterruptedException
     */
    @RenderThread
    abstract void onFrameEnd(@FloatRange(from = 0.0) final double deltaTime) throws InterruptedException;

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
    public boolean executeRenderTask(RenderTask renderTask) {
        if (isRenderThread()) {
            renderTask.run();
            return true;
        } else {
            return queueRenderTask(renderTask);
        }
    }

    /**
     * Queues a {@link RenderTask} to on the render thread at the next frame start, regardless of calling thread.
     *
     * @param renderTask {@link RenderTask} to run.
     * @return {@code true} If the task was successfully queued to run.
     */
    @RenderThread
    public boolean queueRenderTask(RenderTask renderTask) {
        if (isRenderThread()) {
            synchronized (frameTaskQueue) {
                return frameTaskQueue.offer(renderTask);
            }
        }
        return false;
    }

    /**
     *  Runs all {@link RenderTask}s queued for this component
     */
    @RenderThread
    private final void runQueuedTasks() {
        synchronized (frameTaskQueue) {
            RenderTask task = frameTaskQueue.poll();
            while (task != null) {
                task.run();
                task = frameTaskQueue.poll();
            }
        }
    }

    /**
     * Clears all queued {@link RenderTask}s for this component
     */
    final void clearQueuedTasks() {
        synchronized (frameTaskQueue) {
            frameTaskQueue.clear();
        }
    }
}
