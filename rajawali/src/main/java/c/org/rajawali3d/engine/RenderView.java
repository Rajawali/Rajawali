package c.org.rajawali3d.engine;

import android.graphics.Rect;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;

/**
 * Interface to be implemented by views that are to be managed by the Engine. A RenderView represents
 * a viewport on the render surface
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface RenderView {

    /**
     * Sets the active {@link Engine} this {@link RenderView} is registered with, or clears it if
     * {@code null} is provided. There is no guarantee of this method being called on the GL thread.
     *
     * @param engine The active {@link Engine} or {@code null}.
     */
    void setEngine(@Nullable Engine engine);

    /**
     *
     * @return the {@link RenderModel} instance presented in this view, if any
     */
    @Nullable
    RenderModel getRenderModel();

    void setViewport(Rect rect);

    Rect getViewport();

    int getViewportWidth();

    int getViewportHeight();

    void isDisbled(boolean disabled);

    boolean isDisabled();

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
    void onRenderView() throws InterruptedException;

    /**
     *
     * @param deltaTime
     *
     * @throws InterruptedException
     */
    @GLThread
    void onFrameEnd(final double deltaTime) throws InterruptedException;
}
