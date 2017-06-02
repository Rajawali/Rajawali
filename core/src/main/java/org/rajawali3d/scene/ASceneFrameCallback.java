package org.rajawali3d.scene;

/**
 * Abstract class for receiving frame callbacks from {@link Scene}. The timing
 * of this interface assumes that the rendering time does not affect the timing of
 * operations before and after the frame. Pre- and Post- operations are provided
 * because of how these tie in with the animation system. Pre- tasks will be executed
 * prior to animation updates. Post- tasks will be executed after all drawing has occurred.
 *
 * {@link #callPreFrame()} and {@link #callPostFrame()} frame exist to simplify interfacing
 * to Rajawali's frame task system. By default they both return {@code false}, signalling that
 * the callback should be ignored by the scene. Implementing classes must override these methods
 * to return {@code true} as appropriate for the tasks they are handling.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public abstract class ASceneFrameCallback {

    /**
     * Pre frame handling callback. This will be called prior to any camera or animation updates in the scene.
     *
     * @param sceneTime {@code long} Rendering elapsed time in nanoseconds.
     * @param deltaTime {@code double} Time passed since last frame in seconds.
     */
    public abstract void onPreFrame(long sceneTime, double deltaTime);

    /**
     * Pre frame render callback. This will be called after any camera or animation updates in the scene but
     * before rendering.
     *
     * @param sceneTime {@code long} Rendering elapsed time in nanoseconds.
     * @param deltaTime {@code double} Time passed since last frame in seconds.
     */
    public abstract void onPreDraw(long sceneTime, double deltaTime);

    /**
     * Post frame render callback. Called after all frame drawing has completed, including plugins.
     *
     * @param sceneTime {@code long} Rendering elapsed time in nanoseconds.
     * @param deltaTime {@code double} Time passed since last frame in seconds.
     */
    public abstract void onPostFrame(long sceneTime, double deltaTime);

    /**
     * Should this be registered as a pre-frame callback.
     *
     * @return {@code boolean} True if this is a pre-frame callback implementation.
     */
    public boolean callPreFrame() {
        return false;
    }

    /**
     * Should this be registered as a pre-draw callback.
     *
     * @return {@code boolean} True if this is a pre-draw callback implementation.
     */
    public boolean callPreDraw() {
        return false;
    }

    /**
     * Should this be registered as a post-frame callback.
     *
     * @return {@code boolean} True if this is a post-frame callback implementation.
     */
    public boolean callPostFrame() {
        return false;
    }
}
