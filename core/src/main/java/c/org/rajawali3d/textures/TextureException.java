package c.org.rajawali3d.textures;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class TextureException extends Exception {
    private static final long serialVersionUID = -4218033240897223177L;

    public TextureException() {
        super();
    }

    public TextureException(final String msg) {
        super(msg);
    }

    public TextureException(final Throwable throwable) {
        super(throwable);
    }

    public TextureException(final String msg, final Throwable throwable) {
        super(msg, throwable);
    }
}
