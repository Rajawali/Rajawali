package rajawali.parser;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class ParsingException extends Exception {

	private static final long serialVersionUID = 3732833696361901287L;

	public ParsingException() {
		super();
	}

	public ParsingException(final String msg) {
		super(msg);
	}

	public ParsingException(final Throwable throwable) {
		super(throwable);
	}

	public ParsingException(final String msg, final Throwable throwable) {
		super(msg, throwable);
	}

}
