package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RenderThread;

import android.support.annotation.FloatRange;

/**
 * TODO description needs updating
 *
 * Interface for receiving frame event callbacks from the {@link RenderControl}. The timing of this
 * interface assumes that view rendering does not affect the timing of operations before and after the frame. Pre- and Post- operations are
 * provided because of how these tie in with the animation system. Pre- tasks will be executed prior to animation
 * updates. Post- tasks will be executed after all drawing has occurred.
 *
 * {@link #callFrameStart()} and {@link #callFrameEnd()} frame exist to simplify interfacing to Rajawali's frame task
 * system. By default they both return {@code false}, signalling that the callback should be ignored by the scene.
 * Implementing classes must override these methods to return {@code true} as appropriate for the tasks they are
 * handling.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface FrameCallback {

    /**
     * Frame start callback. This will be called after any queued RenderTasks and prior to any animation updates
     *
     * @param deltaTime {@code double} Time passed since last frame in seconds.
     */
    @RenderThread
    void onFrameStart(@FloatRange(from = 0.0) final double deltaTime);

    /**
     * Frame end callback. Called after all frame drawing has completed.
     *
     * @param deltaTime {@code double} Time passed since last frame in seconds.
     */
    @RenderThread
    void onFrameEnd(@FloatRange(from = 0.0) final double deltaTime);

    /**
     * Should this be registered as a frame start callback.
     *
     * @return {@code boolean} True if this is a frame start callback implementation.
     */
    boolean callFrameStart();

    /**
     * Should this be registered as a frame end callback.
     *
     * @return {@code boolean} True if this is a frame end callback implementation.
     */
    boolean callFrameEnd();
}
