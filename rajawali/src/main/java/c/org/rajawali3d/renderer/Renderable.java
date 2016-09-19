package c.org.rajawali3d.renderer;

import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Renderable {

    /**
     * Sets the active {@link Renderer} this {@link Renderable} is registered with, or clears it if {@code null} is
     * provided. There is no guarantee of this method being called on the GL thread.
     *
     * @param renderer The active {@link Renderer} or {@code null}.
     */
    void setRenderer(@Nullable Renderer renderer);

    /**
     * Notifies this {@link Renderable} object that the render surface dimensions have changed.
     *
     * @throws IllegalStateException Thrown if this {@link Renderable} does not know about it's {@link Renderer}.
     */
    void onRenderSurfaceSizeChanged() throws IllegalStateException;

    void clearOverrideViewportDimensions();

    void setOverrideViewportDimensions(int width, int height);

    int getOverrideViewportWidth();

    int getOverrideViewportHeight();

    int getViewportWidth();

    int getViewportHeight();

    @GLThread
    void render(final long ellapsedRealtime, final double deltaTime) throws InterruptedException;

    @GLThread
    void restoreForNewContextIfNeeded();
}
