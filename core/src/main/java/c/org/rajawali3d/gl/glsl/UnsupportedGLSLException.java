package c.org.rajawali3d.gl.glsl;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */

public class UnsupportedGLSLException extends RuntimeException {

    /**
     * Constructs a new {@code UnsupportedGLSLException} that includes the current stack trace.
     */
    public UnsupportedGLSLException() {
    }

    /**
     * Constructs a new {@code UnsupportedGLSLException} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage The detail message for this exception.
     */
    public UnsupportedGLSLException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code UnsupportedGLSLException} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param detailMessage The detail message for this exception.
     * @param throwable     The cause of this exception.
     */
    public UnsupportedGLSLException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code UnsupportedGLSLException} with the current stack trace and the
     * specified cause.
     *
     * @param throwable The cause of this exception.
     */
    public UnsupportedGLSLException(Throwable throwable) {
        super(throwable);
    }
}
