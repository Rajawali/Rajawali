package c.org.rajawali3d.object.renderers;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class UnsupportedRenderTypeException extends RuntimeException {

    private static final long serialVersionUID = -1L;

    public UnsupportedRenderTypeException() {
        super();
    }

    public UnsupportedRenderTypeException(String detailMessage) {
        super(detailMessage);
    }

    public UnsupportedRenderTypeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnsupportedRenderTypeException(Throwable throwable) {
        super(throwable);
    }
}
