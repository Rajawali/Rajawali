package c.org.rajawali3d.renderer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Renderer {

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

    int getDefaultViewportWidth();

    int getDefaultViewportHeight();
}
