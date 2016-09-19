package c.org.rajawali3d.renderer;

import android.support.annotation.NonNull;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Renderer {

    /**
     * Initiates frame render callbacks.
     */
    void startRendering();

    /**
     * Stop all rendering actions.
     *
     * @return {@code true} if rendering was stopped, {@code false} if rendering was already stopped (no action taken)
     */
    boolean stopRendering();

    /**
     * Fetches the Open GL ES major version of the EGL surface.
     *
     * @return {@code int} containing the major version number.
     */
    int getGLMajorVersion();

    /**
     * Fetches the Open GL ES minor version of the EGL surface.
     *
     * @return {@code int} containing the minor version number.
     */
    int getGLMinorVersion();

    /**
     * Retrieves the default viewport width in pixels. Typically this will be the dimension of the view the
     * implementation is attached to.
     *
     * @return {@code int} The default viewport width in pixels.
     */
    int getDefaultViewportWidth();

    /**
     * Retrieves the default viewport height in pixels. Typically this will be the dimension of the view the
     * implementation is attached to.
     *
     * @return {@code int} The default viewport height in pixels.
     */
    int getDefaultViewportHeight();

    /**
     * Checks if the calling thread is the GL thread this {@link Renderer} is associated with.
     *
     * @return {@code true} if the calling thread is the GL thread.
     */
    boolean isGLThread();

    /**
     * Selects the active {@link Renderable} which will be rendered. If the {@link Renderable} has not been
     * registered with this {@link Renderer} yet, it will be prior to the switch.
     *
     * @param renderable The {@link Renderable} to set as active.
     */
    void setCurrentRenderable(@NonNull Renderable renderable);

    /**
     * Adds a {@link Renderable} object to this {@link Renderer}. The {@link Renderable} will be notified of the new
     * context and is expected to take any actions it needs to. There is no guarantee this notification will occur on
     * the GL thread so GL context tasks must be queued by the {@link Renderable}.
     *
     * @param renderable The {@link Renderable} object to add.
     */
    void addRenderable(@NonNull Renderable renderable);

    /**
     * Removes a {@link Renderable} object from this {@link Renderer}. The {@link Renderable} will be notified of the
     * removal and is expected to take any actions it needs to. There is no guarantee this notification will occur on
     * the GL thread so GL context tasks must be queued by the {@link Renderable}.
     *
     * @param renderable The {@link Renderable} object to remove.
     */
    void removeRenderable(@NonNull Renderable renderable);
}
