package c.org.rajawali3d.renderer;

import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.scene.Scene;
import org.rajawali3d.materials.Material;

/**
 * Defines a set of methods which allows for hooking into the render process of {@link Renderer} implementations. A
 * typical example is {@link Scene}.
 *
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

    /**
     * Performs a render pass on this {@link Renderable}.
     *
     * @param ellapsedRealtime {@code long} The total elapsed rendering time in nanoseconds.
     * @param deltaTime {@code double} TThe time passed since the last frame, in seconds.
     *
     * @throws InterruptedException Thrown if the internal threading process is interrupted.
     */
    @GLThread
    void render(final long ellapsedRealtime, final double deltaTime) throws InterruptedException;

    /**
     * Performs render context restoration such as {@link Material} compilation and VBO pushes, if necessary for a
     * new render context.
     */
    @GLThread
    void restoreForNewContextIfNeeded();
}
