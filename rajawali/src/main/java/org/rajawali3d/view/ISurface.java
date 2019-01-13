package org.rajawali3d.view;

import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.renderer.Renderer;

/**
 * Interface which all rendering surfaces must implement so that {@link Renderer} may send
 * the few control signals it needs.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public interface ISurface {

    /**
     * Enum of available anti-aliasing configurations.
     */
    enum ANTI_ALIASING_CONFIG {
        NONE, MULTISAMPLING, COVERAGE;

        public static ANTI_ALIASING_CONFIG fromInteger(int i) {
            switch (i) {
                case 0:
                    return NONE;
                case 1:
                    return MULTISAMPLING;
                case 2:
                    return COVERAGE;
            }
            return NONE;
        }
    }

    /**
     * The renderer only renders
     * when the surface is created, or when {@link #requestRenderUpdate()} is called.
     *
     * @see #getRenderMode()
     * @see #setRenderMode(int)
     * @see #requestRenderUpdate()
     */
    int RENDERMODE_WHEN_DIRTY = 0;
    /**
     * The renderer is called
     * continuously to re-render the scene.
     *
     * @see #getRenderMode()
     * @see #setRenderMode(int)
     */
    int RENDERMODE_CONTINUOUSLY = 1;

    /**
     * Sets the target frame rate in frames per second.
     *
     * @param rate {@code double} The target rate.
     */
    void setFrameRate(double rate);

    /**
     * Gets the current rendering mode.
     *
     * @return {@code int} The current rendering mode.
     */
    int getRenderMode();

    /**
     * Sets the desired rendering mode
     *
     * @param mode {@code int} The desired rendering mode.
     */
    void setRenderMode(int mode);

    /**
     * Called to enable/disable multisampling on this surface.
     * Must be called before {@link #setSurfaceRenderer(ISurfaceRenderer)}.
     *
     * @param config {@link ANTI_ALIASING_CONFIG} The desired anti aliasing configuration.
     */
    void setAntiAliasingMode(ANTI_ALIASING_CONFIG config);

    /**
     * Sets the sample count to use. Only applies if multisample antialiasing is active.
     *
     * @param count {@code int} The sample count.
     */
    void setSampleCount(int count);

    /**
     * Called to set the {@link ISurfaceRenderer} which will render on this surface.
     *
     * @param renderer {@link ISurfaceRenderer} instance.
     * @throws IllegalStateException Thrown if a renderer has already been set.
     */
    void setSurfaceRenderer(ISurfaceRenderer renderer) throws IllegalStateException ;

    /**
     * Called when a render request should be made.
     */
    void requestRenderUpdate();
}
