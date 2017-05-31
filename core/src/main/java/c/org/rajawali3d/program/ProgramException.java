package c.org.rajawali3d.program;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ProgramException extends Exception {

    public ProgramException() {
        super();
    }

    public ProgramException(String message) {
        super(message);
    }

    public ProgramException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProgramException(Throwable cause) {
        super(cause);
    }

    @TargetApi(VERSION_CODES.N)
    protected ProgramException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
