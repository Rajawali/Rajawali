package c.org.rajawali3d.engine;

import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;

/**
 * Interface to be implemented by models that are to be displayed in RenderViews managed by the Engine
 *
 * Author: Randy Picolet
 */

public interface RenderModel {

    /**
     * Sets the active {@link Engine} this {@link RenderModel} is registered with, or clears it if
     * {@code null} is provided. There is no guarantee of this method being called on the GL thread.
     *
     * @param engine The active {@link Engine} or {@code null}.
     */
    void setEngine(@Nullable Engine engine);

    /**
     *
     * @param deltaTime
     *
     * @throws InterruptedException
     */
    @GLThread
    void onFrameStart(final double deltaTime) throws InterruptedException;

    /**
     *
     * @throws InterruptedException
     */
    @GLThread
    void onFrameEnd(final double deltaTime) throws InterruptedException;

    /**
     *
     */
    @GLThread
    void restoreForNewContextIfNeeded();

}
