package c.org.rajawali3d.core;

/**
 * External client interface provided by core engine components.
 *
 * @author Randy Picolet
 */
public interface CoreComponent {

    /**
     * Checks whether the calling thread is the render thread.
     *
     * @return {@code true} if the calling thread is the render thread.
     */
    boolean isRenderThread();
}
