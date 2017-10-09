package c.org.rajawali3d.control;

/**
 * External client interface provided by core engine components.
 *
 * @author Randy Picolet
 */

public interface ControlComponent {

    /**
     * Checks whether the calling thread is the render thread.
     *
     * @return {@code true} if the calling thread is the render thread.
     */
    boolean isRenderThread();
}
